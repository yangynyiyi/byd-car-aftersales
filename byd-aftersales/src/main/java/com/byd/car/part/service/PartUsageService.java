package com.byd.car.part.service;

import com.byd.car.part.dto.PartUsageCreateRequest;
import com.byd.car.part.entity.PartUsage;

import java.util.List;

public interface PartUsageService {

    PartUsage apply(PartUsageCreateRequest request);

    List<PartUsage> listByWorkOrder(Long workOrderId);

    PartUsage approve(Long usageId, Long approvedBy);

    PartUsage reject(Long usageId, Long approvedBy);
}
