package com.byd.car.transaction;

import com.byd.car.common.exception.BusinessException;
import com.byd.car.log.entity.OperationLog;
import com.byd.car.log.repository.OperationLogRepository;
import com.byd.car.part.dao.PartDao;
import com.byd.car.part.dao.PartUsageDao;
import com.byd.car.part.entity.Part;
import com.byd.car.part.entity.PartUsage;
import com.byd.car.settlement.dao.SettlementDao;
import com.byd.car.settlement.entity.Settlement;
import com.byd.car.workorder.dao.WorkOrderDao;
import com.byd.car.workorder.entity.WorkOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 工单完工事务服务。
 *
 * 完工时原子执行以下步骤，任意失败全部回滚（MySQL 事务）：
 *   1. 扣减每条已审批备件的库存
 *   2. 将 part_usage.status 更新为 USED
 *   3. 生成结算单（labor + parts - warranty = total）
 *   4. 更新 work_order.status = COMPLETED
 *
 * 事务提交后（afterCommit）写入 MongoDB 操作日志，Redis 低库存预警同样在 afterCommit 写入，
 * 保证缓存不早于数据库状态。
 */
@Slf4j
@Service
public class WorkOrderCompletionService {

    private static final String LOW_STOCK_KEY_PREFIX = "part:low_stock:";

    private final WorkOrderDao workOrderDao;
    private final PartDao partDao;
    private final PartUsageDao partUsageDao;
    private final SettlementDao settlementDao;
    private final StringRedisTemplate redisTemplate;

    @Autowired(required = false)
    private OperationLogRepository operationLogRepository;

    public WorkOrderCompletionService(WorkOrderDao workOrderDao, PartDao partDao,
                                       PartUsageDao partUsageDao, SettlementDao settlementDao,
                                       StringRedisTemplate redisTemplate) {
        this.workOrderDao = workOrderDao;
        this.partDao = partDao;
        this.partUsageDao = partUsageDao;
        this.settlementDao = settlementDao;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 完工主入口，带 @Transactional 保证 MySQL 操作的原子性。
     *
     * @param workOrderId    工单 ID
     * @param repairResult   维修结果描述
     * @param warrantyAmount 质保抵扣金额（可为 0）
     * @param operatorId     操作人（技师）ID
     * @return 生成的结算单
     */
    @Transactional(rollbackFor = Exception.class)
    public Settlement complete(Long workOrderId, String repairResult,
                               BigDecimal warrantyAmount, Long operatorId) {

        // ── 校验工单状态 ──────────────────────────────────────────────
        WorkOrder workOrder = workOrderDao.findById(workOrderId);
        if (workOrder == null) {
            throw new BusinessException(404, "工单不存在");
        }
        if (!"IN_PROGRESS".equals(workOrder.getStatus())
                && !"PART_WAITING".equals(workOrder.getStatus())) {
            throw new BusinessException("当前工单状态不允许完工，status=" + workOrder.getStatus());
        }

        // ── 1. 扣减库存 + 标记 part_usage 为 USED ────────────────────
        List<PartUsage> approvedUsages = partUsageDao.findApprovedByWorkOrderId(workOrderId);
        BigDecimal partAmount = BigDecimal.ZERO;

        for (PartUsage usage : approvedUsages) {
            int affected = partDao.deductStock(usage.getPartId(), usage.getQuantity());
            if (affected == 0) {
                Part p = partDao.findById(usage.getPartId());
                String name = p != null ? p.getPartName() : "partId=" + usage.getPartId();
                throw new BusinessException("备件【" + name + "】库存不足，扣减失败，事务回滚");
            }
            partUsageDao.markUsed(usage.getUsageId());
            partAmount = partAmount.add(
                    usage.getUnitPrice().multiply(BigDecimal.valueOf(usage.getQuantity())));
        }

        // ── 2. 生成结算单 ─────────────────────────────────────────────
        if (warrantyAmount == null) {
            warrantyAmount = BigDecimal.ZERO;
        }
        BigDecimal totalAmount = workOrder.getLaborCost()
                .add(partAmount)
                .subtract(warrantyAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        Settlement settlement = new Settlement();
        settlement.setSettlementNo(generateSettlementNo());
        settlement.setWorkOrderId(workOrderId);
        settlement.setLaborAmount(workOrder.getLaborCost());
        settlement.setPartAmount(partAmount);
        settlement.setWarrantyAmount(warrantyAmount);
        settlement.setTotalAmount(totalAmount);

        Long settlementId = settlementDao.insert(settlement);

        // ── 3. 更新工单状态为 COMPLETED ───────────────────────────────
        workOrderDao.complete(workOrderId, repairResult);

        // ── 事务提交后：写 Redis 预警 + 写 MongoDB 日志 ──────────────
        final BigDecimal finalPartAmount = partAmount;
        final BigDecimal finalTotal = totalAmount;
        final List<PartUsage> usagesSnapshot = new ArrayList<>(approvedUsages);
        final Long finalSettlementId = settlementId;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 写 Redis 低库存预警
                for (PartUsage usage : usagesSnapshot) {
                    try {
                        Part latest = partDao.findById(usage.getPartId());
                        if (latest != null && latest.getStockQuantity() < latest.getWarningThreshold()) {
                            redisTemplate.opsForValue().set(
                                    LOW_STOCK_KEY_PREFIX + latest.getPartId(),
                                    String.valueOf(latest.getStockQuantity()));
                            log.warn("低库存预警: 备件[{}] 当前库存 {}, 预警阈值 {}",
                                    latest.getPartName(), latest.getStockQuantity(),
                                    latest.getWarningThreshold());
                        }
                    } catch (Exception e) {
                        log.error("写 Redis 低库存预警失败，partId={}", usage.getPartId(), e);
                    }
                }

                // 写 MongoDB 操作日志
                if (operationLogRepository != null) {
                    try {
                        String detail = String.format(
                                "工单[%d]完工，备件费=%.2f，工时费=%.2f，实收=%.2f，结算单ID=%d",
                                workOrderId, finalPartAmount, workOrder.getLaborCost(),
                                finalTotal, finalSettlementId);
                        OperationLog opLog = OperationLog.builder()
                                .businessType("WORK_ORDER")
                                .businessId(workOrderId)
                                .action("COMPLETE")
                                .operatorId(operatorId)
                                .detail(detail)
                                .createdAt(LocalDateTime.now())
                                .build();
                        operationLogRepository.save(opLog);
                    } catch (Exception e) {
                        log.error("写 MongoDB 操作日志失败，workOrderId={}", workOrderId, e);
                    }
                }
            }
        });

        return settlementDao.findById(settlementId);
    }

    private String generateSettlementNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "ST" + date + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
