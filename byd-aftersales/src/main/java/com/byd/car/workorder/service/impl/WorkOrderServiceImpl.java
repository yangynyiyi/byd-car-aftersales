package com.byd.car.workorder.service.impl;

import com.byd.car.common.exception.BusinessException;
import com.byd.car.workorder.dao.WorkOrderDao;
import com.byd.car.workorder.dto.WorkOrderCreateRequest;
import com.byd.car.workorder.entity.WorkOrder;
import com.byd.car.workorder.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderDao workOrderDao;

    @Override
    public WorkOrder create(WorkOrderCreateRequest request) {
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNo(generateWorkOrderNo());
        wo.setFaultId(request.getFaultId());
        wo.setDiagnosisId(request.getDiagnosisId());
        wo.setTechnicianId(request.getTechnicianId());
        wo.setLaborCost(request.getLaborCost() != null ? request.getLaborCost() : BigDecimal.ZERO);
        wo.setStatus(request.getTechnicianId() != null ? "ASSIGNED" : "CREATED");

        Long id = workOrderDao.insert(wo);
        return workOrderDao.findById(id);
    }

    @Override
    public WorkOrder getById(Long workOrderId) {
        WorkOrder wo = workOrderDao.findById(workOrderId);
        if (wo == null) {
            throw new BusinessException(404, "工单不存在");
        }
        return wo;
    }

    @Override
    public List<WorkOrder> listAll() {
        return workOrderDao.findAll();
    }

    @Override
    public List<WorkOrder> listByTechnician(Long technicianId) {
        return workOrderDao.findByTechnicianId(technicianId);
    }

    @Override
    public WorkOrder assignTechnician(Long workOrderId, Long technicianId) {
        WorkOrder wo = getById(workOrderId);
        if ("COMPLETED".equals(wo.getStatus()) || "CANCELLED".equals(wo.getStatus())) {
            throw new BusinessException("当前工单状态不允许指派技师");
        }
        workOrderDao.assignTechnician(workOrderId, technicianId);
        return workOrderDao.findById(workOrderId);
    }

    @Override
    public WorkOrder startRepair(Long workOrderId) {
        WorkOrder wo = getById(workOrderId);
        if (!"ASSIGNED".equals(wo.getStatus())) {
            throw new BusinessException("只有已指派状态的工单才能开始维修");
        }
        workOrderDao.markStarted(workOrderId);
        return workOrderDao.findById(workOrderId);
    }

    private String generateWorkOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "WO" + date + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
