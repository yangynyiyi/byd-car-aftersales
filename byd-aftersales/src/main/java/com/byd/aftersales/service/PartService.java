package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.PartDao;
import com.byd.aftersales.domain.Part;
import com.byd.aftersales.dto.PartCreateRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PartService {

    private final PartDao partDao;

    public PartService(PartDao partDao) {
        this.partDao = partDao;
    }

    public Part create(PartCreateRequest request) {
        if (request.getPartNo() == null || request.getPartNo().isBlank()) {
            throw new BusinessException("备件编号不能为空");
        }
        if (request.getPartName() == null || request.getPartName().isBlank()) {
            throw new BusinessException("备件名称不能为空");
        }
        if (request.getStockQuantity() != null && request.getStockQuantity() < 0) {
            throw new BusinessException("初始库存不能为负数");
        }
        if (request.getPurchasePrice() != null && request.getPurchasePrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessException("采购价不能为负数");
        }
        if (request.getSellingPrice() != null && request.getSellingPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessException("销售价不能为负数");
        }
        Part part = new Part();
        part.setPartNo(request.getPartNo());
        part.setPartName(request.getPartName());
        part.setCategory(request.getCategory() != null ? request.getCategory() : "GENERAL");
        part.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        part.setWarningThreshold(request.getWarningThreshold() != null ? request.getWarningThreshold() : 10);
        part.setUnit(request.getUnit() != null && !request.getUnit().isBlank() ? request.getUnit() : "个");
        part.setPurchasePrice(request.getPurchasePrice() != null ? request.getPurchasePrice() : java.math.BigDecimal.ZERO);
        part.setSellingPrice(request.getSellingPrice() != null ? request.getSellingPrice() : java.math.BigDecimal.ZERO);
        part.setStatus("ENABLED");
        Long id = partDao.insert(part);
        return partDao.findById(id).orElseThrow(() -> new BusinessException("备件创建失败"));
    }

    public Part getById(Long partId) {
        return partDao.findById(partId).orElseThrow(() -> new BusinessException("备件不存在"));
    }

    public List<Part> listAll() {
        return partDao.findAll();
    }

    public Part update(Long partId, PartCreateRequest request) {
        Part part = getById(partId);
        part.setPartName(request.getPartName());
        part.setCategory(request.getCategory());
        part.setWarningThreshold(request.getWarningThreshold());
        part.setUnit(request.getUnit() != null && !request.getUnit().isBlank() ? request.getUnit() : part.getUnit());
        part.setPurchasePrice(request.getPurchasePrice() != null ? request.getPurchasePrice() : part.getPurchasePrice());
        part.setSellingPrice(request.getSellingPrice() != null ? request.getSellingPrice() : part.getSellingPrice());
        partDao.update(part);
        return partDao.findById(partId).orElseThrow(() -> new BusinessException("备件不存在"));
    }

    public void delete(Long partId) {
        getById(partId);
        partDao.deleteById(partId);
    }

    public Part addStock(Long partId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("补货数量必须大于 0");
        }
        getById(partId);
        partDao.addStock(partId, quantity);
        return partDao.findById(partId).orElseThrow(() -> new BusinessException("备件不存在"));
    }

    public List<String> getLowStockAlerts() {
        List<String> alerts = new ArrayList<>();
        for (Part part : partDao.findLowStock()) {
            alerts.add("partId=" + part.getPartId() + ", name=" + part.getPartName()
                    + ", stock=" + part.getStockQuantity());
        }
        return alerts;
    }
}
