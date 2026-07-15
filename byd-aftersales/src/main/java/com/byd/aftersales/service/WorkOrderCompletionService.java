package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.common.IdGenerator;
import com.byd.aftersales.dao.FaultRecordDao;
import com.byd.aftersales.dao.OperationLogDao;
import com.byd.aftersales.dao.PartDao;
import com.byd.aftersales.dao.PartUsageDao;
import com.byd.aftersales.dao.SettlementDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.dao.WorkOrderDao;
import com.byd.aftersales.domain.FaultRecord;
import com.byd.aftersales.domain.Part;
import com.byd.aftersales.domain.PartUsage;
import com.byd.aftersales.domain.Settlement;
import com.byd.aftersales.domain.WorkOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class WorkOrderCompletionService {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderCompletionService.class);

    private final WorkOrderDao workOrderDao;
    private final PartDao partDao;
    private final PartUsageDao partUsageDao;
    private final SettlementDao settlementDao;
    private final OperationLogDao operationLogDao;
    private final FaultRecordDao faultRecordDao;
    private final VehicleDao vehicleDao;
    private final WarrantyService warrantyService;

    public WorkOrderCompletionService(WorkOrderDao workOrderDao, PartDao partDao,
                                      PartUsageDao partUsageDao, SettlementDao settlementDao,
                                      OperationLogDao operationLogDao, FaultRecordDao faultRecordDao,
                                      VehicleDao vehicleDao, WarrantyService warrantyService) {
        this.workOrderDao = workOrderDao;
        this.partDao = partDao;
        this.partUsageDao = partUsageDao;
        this.settlementDao = settlementDao;
        this.operationLogDao = operationLogDao;
        this.faultRecordDao = faultRecordDao;
        this.vehicleDao = vehicleDao;
        this.warrantyService = warrantyService;
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
            if (partUsageDao.markUsed(usage.getUsageId()) == 0) {
                throw new BusinessException("备件领用记录状态更新失败，可能已被处理");
            }
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
        if (warrantyAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("质保减免金额不能为负数");
        }
        var warrantyEstimate = warrantyService.estimateForWorkOrder(workOrderId);
        if (warrantyAmount.compareTo(warrantyEstimate.getGrossAmount()) > 0) {
            throw new BusinessException("质保减免不能超过费用合计 ¥" + warrantyEstimate.getGrossAmount());
        }
        if (warrantyAmount.compareTo(warrantyEstimate.getSuggestedWarrantyAmount()) > 0) {
            throw new BusinessException("质保减免超过系统建议上限 ¥"
                    + warrantyEstimate.getSuggestedWarrantyAmount() + "，请核对三包条件");
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
        if (workOrderDao.complete(workOrderId, repairResult) == 0) {
            throw new BusinessException("工单状态已变化，完工失败，请刷新后重试");
        }
        updateVehicleRepairRecord(workOrder, workOrderId, operatorId);

        String detail = String.format(
                "工单[%d]完工，备件费=%.2f，工时费=%.2f，实收=%.2f，结算单ID=%d",
                workOrderId, partAmount, workOrder.getLaborCost(), totalAmount, settlementId);
        operationLogDao.insert("WORK_ORDER", workOrderId, "COMPLETE", operatorId, detail);

        return settlementDao.findById(settlementId)
                .orElseThrow(() -> new BusinessException("结算单生成失败"));
    }

    private void updateVehicleRepairRecord(WorkOrder workOrder, Long workOrderId, Long operatorId) {
        if (workOrder.getFaultId() == null) {
            return;
        }
        FaultRecord fault = faultRecordDao.findById(workOrder.getFaultId())
                .orElseThrow(() -> new BusinessException("关联故障记录不存在"));
        if (faultRecordDao.updateStatus(fault.getFaultNo(), "CLOSED") == 0) {
            throw new BusinessException("关闭故障单失败");
        }

        if (vehicleDao.findByVin(fault.getVin()).isEmpty()) {
            throw new BusinessException("关联车辆不存在");
        }
        LocalDate today = LocalDate.now();
        LocalDate nextMaintenanceDate = today.plusMonths(6);
        String vehicleStatus = workOrderDao.countActiveByVin(fault.getVin()) == 0 ? "NORMAL" : "REPAIRING";
        if (vehicleDao.updateMaintenanceRecord(fault.getVin(), vehicleStatus, today, nextMaintenanceDate) == 0) {
            throw new BusinessException("更新车辆维修记录失败");
        }

        String vehicleDetail = String.format(
                "车辆[%s]维修记录已更新：状态=%s，上次保养=%s，下次保养=%s，关联工单=%d，故障单=%s已关闭",
                fault.getVin(), vehicleStatus, today, nextMaintenanceDate, workOrderId, fault.getFaultNo());
        operationLogDao.insert("VEHICLE", workOrderId, "REPAIR_RECORD_UPDATED", operatorId, vehicleDetail);
    }
}
