package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.common.IdGenerator;
import com.byd.aftersales.dao.FaultRecordDao;
import com.byd.aftersales.dao.OperationLogDao;
import com.byd.aftersales.dao.WorkOrderDao;
import com.byd.aftersales.domain.FaultRecord;
import com.byd.aftersales.domain.WorkOrder;
import com.byd.aftersales.dto.WorkOrderCreateRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class WorkOrderService {

    private final WorkOrderDao workOrderDao;
    private final FaultRecordDao faultRecordDao;
    private final OperationLogDao operationLogDao;

    public WorkOrderService(WorkOrderDao workOrderDao, FaultRecordDao faultRecordDao,
                            OperationLogDao operationLogDao) {
        this.workOrderDao = workOrderDao;
        this.faultRecordDao = faultRecordDao;
        this.operationLogDao = operationLogDao;
    }

    public WorkOrder create(WorkOrderCreateRequest request) {
        if (request.getFaultId() == null) {
            throw new BusinessException("故障 ID 不能为空");
        }
        FaultRecord fault = faultRecordDao.findById(request.getFaultId())
                .orElseThrow(() -> new BusinessException("故障记录不存在"));
        if (workOrderDao.existsByFaultId(request.getFaultId())) {
            throw new BusinessException("该故障已存在维修工单，不能重复创建");
        }
        if ("WORK_ORDER_CREATED".equals(fault.getStatus())) {
            throw new BusinessException("该故障已转工单，不能重复创建");
        }
        if ("CLOSED".equals(fault.getStatus())) {
            throw new BusinessException("该故障已关闭，不能生成工单");
        }

        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(IdGenerator.generate("WO"));
        wo.setFaultId(request.getFaultId());
        wo.setDiagnosisId(request.getDiagnosisId());
        wo.setTechnicianId(request.getTechnicianId());
        wo.setLaborCost(request.getLaborCost() != null ? request.getLaborCost() : BigDecimal.ZERO);
        wo.setStatus(request.getTechnicianId() != null ? "ASSIGNED" : "CREATED");

        Long id = workOrderDao.insert(wo);
        faultRecordDao.updateStatus(fault.getFaultNo(), "WORK_ORDER_CREATED");
        return workOrderDao.findById(id).orElseThrow(() -> new BusinessException("工单创建失败"));
    }

    public WorkOrder getById(Long workOrderId) {
        return workOrderDao.findById(workOrderId)
                .orElseThrow(() -> new BusinessException("工单不存在"));
    }

    public List<WorkOrder> listAll() {
        return workOrderDao.findAll();
    }

    public List<WorkOrder> listByTechnician(Long technicianId) {
        return workOrderDao.findByTechnicianId(technicianId);
    }

    public WorkOrder assignTechnician(Long workOrderId, Long technicianId) {
        WorkOrder wo = getById(workOrderId);
        if ("COMPLETED".equals(wo.getStatus()) || "CANCELLED".equals(wo.getStatus())) {
            throw new BusinessException("当前工单状态不允许指派技师");
        }
        if (technicianId == null) {
            throw new BusinessException("技师 ID 不能为空");
        }
        if (workOrderDao.assignTechnician(workOrderId, technicianId) == 0) {
            throw new BusinessException("指派失败");
        }
        return workOrderDao.findById(workOrderId).orElseThrow(() -> new BusinessException("工单不存在"));
    }

    public WorkOrder startRepair(Long workOrderId) {
        WorkOrder wo = getById(workOrderId);
        if ("PART_WAITING".equals(wo.getStatus())) {
            if (workOrderDao.resumeRepair(workOrderId) == 0) {
                throw new BusinessException("继续维修失败");
            }
            return workOrderDao.findById(workOrderId).orElseThrow(() -> new BusinessException("工单不存在"));
        }
        if (!"ASSIGNED".equals(wo.getStatus())) {
            throw new BusinessException("只有已指派或待备件状态的工单才能开始维修");
        }
        if (workOrderDao.markStarted(workOrderId) == 0) {
            throw new BusinessException("开始维修失败");
        }
        return workOrderDao.findById(workOrderId).orElseThrow(() -> new BusinessException("工单不存在"));
    }

    public WorkOrder markPartWaiting(Long workOrderId) {
        WorkOrder wo = getById(workOrderId);
        if (!"IN_PROGRESS".equals(wo.getStatus())) {
            throw new BusinessException("只有维修中工单可标记待件");
        }
        if (workOrderDao.markPartWaiting(workOrderId) == 0) {
            throw new BusinessException("更新失败");
        }
        return workOrderDao.findById(workOrderId).orElseThrow(() -> new BusinessException("工单不存在"));
    }

    public void supervise(Long workOrderId, Long operatorId) {
        WorkOrder wo = getById(workOrderId);
        if ("COMPLETED".equals(wo.getStatus()) || "CANCELLED".equals(wo.getStatus())) {
            throw new BusinessException("工单已结束，无需督办");
        }
        if (operatorId == null) {
            throw new BusinessException("操作人不能为空");
        }
        String detail = String.format("督办工单[%s]，当前状态=%s", wo.getWorkOrderNo(), wo.getStatus());
        operationLogDao.insert("WORK_ORDER", workOrderId, "SUPERVISE", operatorId, detail);
    }

    public List<Map<String, Object>> listSupervisionsForTechnician(Long technicianId) {
        if (technicianId == null) {
            throw new BusinessException("技师 ID 不能为空");
        }
        return operationLogDao.findWorkOrderSupervisionsForTechnician(technicianId);
    }
}
