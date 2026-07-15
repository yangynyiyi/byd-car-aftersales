package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.PartDao;
import com.byd.aftersales.dao.PartUsageDao;
import com.byd.aftersales.dao.WorkOrderDao;
import com.byd.aftersales.domain.Part;
import com.byd.aftersales.domain.PartUsage;
import com.byd.aftersales.domain.WorkOrder;
import com.byd.aftersales.dto.PartUsageCreateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartUsageService {

    private final PartUsageDao partUsageDao;
    private final PartDao partDao;
    private final WorkOrderDao workOrderDao;

    public PartUsageService(PartUsageDao partUsageDao, PartDao partDao, WorkOrderDao workOrderDao) {
        this.partUsageDao = partUsageDao;
        this.partDao = partDao;
        this.workOrderDao = workOrderDao;
    }

    public PartUsage apply(PartUsageCreateRequest request) {
        WorkOrder workOrder = workOrderDao.findById(request.getWorkOrderId())
                .orElseThrow(() -> new BusinessException("关联工单不存在"));
        if (!"IN_PROGRESS".equals(workOrder.getStatus())
                && !"PART_WAITING".equals(workOrder.getStatus())) {
            throw new BusinessException("仅维修中或待备件状态的工单可提交备件申请");
        }

        Part part = partDao.findById(request.getPartId()).orElse(null);
        if (part == null || "DISABLED".equals(part.getStatus())) {
            throw new BusinessException("备件不存在或已禁用");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("申请数量必须大于 0");
        }
        if (partUsageDao.existsPendingByWorkOrderAndPart(request.getWorkOrderId(), request.getPartId())) {
            throw new BusinessException("该工单已有此备件的在途提案，请勿重复提交；如需增加数量请驳回后重新提案");
        }

        PartUsage usage = new PartUsage();
        usage.setWorkOrderId(request.getWorkOrderId());
        usage.setPartId(request.getPartId());
        usage.setQuantity(request.getQuantity());
        usage.setUnitPrice(part.getSellingPrice());
        usage.setTechnicianId(request.getTechnicianId());

        Long id = partUsageDao.insert(usage);
        return partUsageDao.findById(id).orElseThrow(() -> new BusinessException("备件申请提交失败"));
    }

    public List<PartUsage> listByWorkOrder(Long workOrderId) {
        return partUsageDao.findByWorkOrderId(workOrderId);
    }

    public List<PartUsage> listPending() {
        return partUsageDao.findPendingApproval();
    }

    public List<PartUsage> listAll() {
        return partUsageDao.findAll();
    }

    public long countTodayApplications() {
        return partUsageDao.countByDate(java.time.LocalDate.now());
    }

    public PartUsage approve(Long usageId, Long approvedBy) {
        PartUsage usage = getPendingApprovalOrThrow(usageId);
        Part part = partDao.findById(usage.getPartId())
                .orElseThrow(() -> new BusinessException("备件不存在"));
        if (part.getStockQuantity() < usage.getQuantity()) {
            throw new BusinessException("备件【" + part.getPartName() + "】库存不足，当前库存 "
                    + part.getStockQuantity());
        }
        if (partUsageDao.approve(usageId, approvedBy) == 0) {
            throw new BusinessException("审批失败，请刷新后重试");
        }
        return partUsageDao.findById(usageId).orElseThrow(() -> new BusinessException("记录不存在"));
    }

    public PartUsage reject(Long usageId, Long approvedBy) {
        getPendingApprovalOrThrow(usageId);
        if (partUsageDao.reject(usageId, approvedBy) == 0) {
            throw new BusinessException("操作失败，请刷新后重试");
        }
        return partUsageDao.findById(usageId).orElseThrow(() -> new BusinessException("记录不存在"));
    }

    private PartUsage getPendingApprovalOrThrow(Long usageId) {
        PartUsage usage = partUsageDao.findById(usageId)
                .orElseThrow(() -> new BusinessException("备件申请记录不存在"));
        if (!"APPLIED".equals(usage.getStatus()) && !"PROPOSED".equals(usage.getStatus())) {
            throw new BusinessException("只有待备件员审批状态才能操作");
        }
        return usage;
    }
}
