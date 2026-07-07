package com.byd.car.workorder.service;

import com.byd.car.workorder.dto.WorkOrderCreateRequest;
import com.byd.car.workorder.entity.WorkOrder;

import java.util.List;

public interface WorkOrderService {

    WorkOrder create(WorkOrderCreateRequest request);

    WorkOrder getById(Long workOrderId);

    List<WorkOrder> listAll();

    List<WorkOrder> listByTechnician(Long technicianId);

    WorkOrder assignTechnician(Long workOrderId, Long technicianId);

    WorkOrder startRepair(Long workOrderId);
}
