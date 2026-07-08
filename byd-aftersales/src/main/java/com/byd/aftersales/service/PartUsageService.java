package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.PartDao;
import com.byd.aftersales.dao.PartUsageDao;
import com.byd.aftersales.domain.Part;
import com.byd.aftersales.domain.PartUsage;
import com.byd.aftersales.dto.PartUsageCreateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartUsageService {

    private final PartUsageDao partUsageDao;
    private final PartDao partDao;

    public PartUsageService(PartUsageDao partUsageDao, PartDao partDao) {
        this.partUsageDao = partUsageDao;
        this.partDao = partDao;
    }

    public PartUsage apply(PartUsageCreateRequest request) {
        Part part = partDao.findById(request.getPartId()).orElse(null);
        if (part == null || "DISABLED".equals(part.getStatus())) {
            throw new BusinessException("备件不存在或已禁用");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("申请数量必须大于 0");
        }

        PartUsage usage = new PartUsage();
        usage.setWorkOrderId(request.getWorkOrderId());
        usage.setPartId(request.getPartId());
        usage.setQuantity(request.getQuantity());
        usage.setUnitPrice(part.getSellingPrice());
        usage.setTechnicianId(request.getTechnicianId());

        Long id = partUsageDao.insert(usage);
        return partUsageDao.findById(id).orElseThrow(() -> new BusinessException("备件申请失败"));
    }

    public List<PartUsage> listByWorkOrder(Long workOrderId) {
        return partUsageDao.findByWorkOrderId(workOrderId);
    }

    public List<PartUsage> listPending() {
        return partUsageDao.findByStatus("APPLIED");
    }

    public PartUsage approve(Long usageId, Long approvedBy) {
        getUsageOrThrow(usageId);
        if (partUsageDao.approve(usageId, approvedBy) == 0) {
            throw new BusinessException("审批失败，请刷新后重试");
        }
        return partUsageDao.findById(usageId).orElseThrow(() -> new BusinessException("记录不存在"));
    }

    public PartUsage reject(Long usageId, Long approvedBy) {
        getUsageOrThrow(usageId);
        if (partUsageDao.reject(usageId, approvedBy) == 0) {
            throw new BusinessException("操作失败，请刷新后重试");
        }
        return partUsageDao.findById(usageId).orElseThrow(() -> new BusinessException("记录不存在"));
    }

    private PartUsage getUsageOrThrow(Long usageId) {
        PartUsage usage = partUsageDao.findById(usageId)
                .orElseThrow(() -> new BusinessException("备件申请记录不存在"));
        if (!"APPLIED".equals(usage.getStatus())) {
            throw new BusinessException("只有待审批状态才能操作");
        }
        return usage;
    }
}
