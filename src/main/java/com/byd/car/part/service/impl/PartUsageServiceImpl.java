package com.byd.car.part.service.impl;

import com.byd.car.common.exception.BusinessException;
import com.byd.car.part.dao.PartDao;
import com.byd.car.part.dao.PartUsageDao;
import com.byd.car.part.dto.PartUsageCreateRequest;
import com.byd.car.part.entity.Part;
import com.byd.car.part.entity.PartUsage;
import com.byd.car.part.service.PartUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartUsageServiceImpl implements PartUsageService {

    private final PartUsageDao partUsageDao;
    private final PartDao partDao;

    @Override
    public PartUsage apply(PartUsageCreateRequest request) {
        Part part = partDao.findById(request.getPartId());
        if (part == null || "DISABLED".equals(part.getStatus())) {
            throw new BusinessException("备件不存在或已禁用");
        }
        if (request.getQuantity() <= 0) {
            throw new BusinessException("申请数量必须大于 0");
        }

        PartUsage usage = new PartUsage();
        usage.setWorkOrderId(request.getWorkOrderId());
        usage.setPartId(request.getPartId());
        usage.setQuantity(request.getQuantity());
        usage.setUnitPrice(part.getSellingPrice());
        usage.setTechnicianId(request.getTechnicianId());

        Long id = partUsageDao.insert(usage);
        return partUsageDao.findById(id);
    }

    @Override
    public List<PartUsage> listByWorkOrder(Long workOrderId) {
        return partUsageDao.findByWorkOrderId(workOrderId);
    }

    @Override
    public PartUsage approve(Long usageId, Long approvedBy) {
        PartUsage usage = getUsageOrThrow(usageId);
        if (!"APPLIED".equals(usage.getStatus())) {
            throw new BusinessException("只有待审批状态才能审批");
        }
        int rows = partUsageDao.approve(usageId, approvedBy);
        if (rows == 0) {
            throw new BusinessException("审批失败，请刷新后重试");
        }
        return partUsageDao.findById(usageId);
    }

    @Override
    public PartUsage reject(Long usageId, Long approvedBy) {
        PartUsage usage = getUsageOrThrow(usageId);
        if (!"APPLIED".equals(usage.getStatus())) {
            throw new BusinessException("只有待审批状态才能拒绝");
        }
        int rows = partUsageDao.reject(usageId, approvedBy);
        if (rows == 0) {
            throw new BusinessException("操作失败，请刷新后重试");
        }
        return partUsageDao.findById(usageId);
    }

    private PartUsage getUsageOrThrow(Long usageId) {
        PartUsage usage = partUsageDao.findById(usageId);
        if (usage == null) {
            throw new BusinessException(404, "备件申请记录不存在");
        }
        return usage;
    }
}
