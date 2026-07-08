package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.common.IdGenerator;
import com.byd.aftersales.dao.OperationLogDao;
import com.byd.aftersales.dao.PartDao;
import com.byd.aftersales.dao.PartUsageDao;
import com.byd.aftersales.dao.SettlementDao;
import com.byd.aftersales.dao.WorkOrderDao;
import com.byd.aftersales.domain.Part;
import com.byd.aftersales.domain.PartUsage;
import com.byd.aftersales.domain.Settlement;
import com.byd.aftersales.domain.WorkOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WorkOrderCompletionService {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderCompletionService.class);

    private final WorkOrderDao workOrderDao;
    private final PartDao partDao;
    private final PartUsageDao partUsageDao;
    private final SettlementDao settlementDao;
    private final OperationLogDao operationLogDao;

    public WorkOrderCompletionService(WorkOrderDao workOrderDao, PartDao partDao,
                                      PartUsageDao partUsageDao, SettlementDao settlementDao,
                                      OperationLogDao operationLogDao) {
        this.workOrderDao = workOrderDao;
        this.partDao = partDao;
        this.partUsageDao = partUsageDao;
        this.settlementDao = settlementDao;
        this.operationLogDao = operationLogDao;
    }

    @Transactional(rollbackFor = Exception.class)
    public Settlement complete(Long workOrderId, String repairResult,
                               BigDecimal warrantyAmount, Long operatorId) {
        WorkOrder workOrder = workOrderDao.findById(workOrderId)
                .orElseThrow(() -> new BusinessException("工单不存在"));
        if (!"IN_PROGRESS".equals(workOrder.getStatus())
                && !"PART_WAITING".equals(workOrder.getStatus())) {
            throw new BusinessException("当前工单状态不允许完工，status=" + workOrder.getStatus());
        }

        List<PartUsage> approvedUsages = partUsageDao.findApprovedByWorkOrderId(workOrderId);
        BigDecimal partAmount = BigDecimal.ZERO;

        for (PartUsage usage : approvedUsages) {
            int affected = partDao.deductStock(usage.getPartId(), usage.getQuantity());
            if (affected == 0) {
                Part p = partDao.findById(usage.getPartId()).orElse(null);
                String name = p != null ? p.getPartName() : "partId=" + usage.getPartId();
                throw new BusinessException("备件【" + name + "】库存不足，扣减失败");
            }
            partUsageDao.markUsed(usage.getUsageId());
            partAmount = partAmount.add(
                    usage.getUnitPrice().multiply(BigDecimal.valueOf(usage.getQuantity())));
            partDao.findById(usage.getPartId()).ifPresent(latest -> {
                if (latest.getStockQuantity() < latest.getWarningThreshold()) {
                    log.warn("低库存预警: 备件[{}] 当前库存 {}, 预警阈值 {}",
                            latest.getPartName(), latest.getStockQuantity(), latest.getWarningThreshold());
                }
            });
        }

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
        settlement.setSettlementNo(IdGenerator.generate("ST"));
        settlement.setWorkOrderId(workOrderId);
        settlement.setLaborAmount(workOrder.getLaborCost());
        settlement.setPartAmount(partAmount);
        settlement.setWarrantyAmount(warrantyAmount);
        settlement.setTotalAmount(totalAmount);

        Long settlementId = settlementDao.insert(settlement);
        workOrderDao.complete(workOrderId, repairResult);

        String detail = String.format(
                "工单[%d]完工，备件费=%.2f，工时费=%.2f，实收=%.2f，结算单ID=%d",
                workOrderId, partAmount, workOrder.getLaborCost(), totalAmount, settlementId);
        operationLogDao.insert("WORK_ORDER", workOrderId, "COMPLETE", operatorId, detail);

        return settlementDao.findById(settlementId)
                .orElseThrow(() -> new BusinessException("结算单生成失败"));
    }
}
