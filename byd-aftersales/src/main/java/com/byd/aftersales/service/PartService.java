package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.PartDao;
import com.byd.aftersales.domain.Part;
import com.byd.aftersales.dto.PartCreateRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        if (request.getWarningThreshold() != null && request.getWarningThreshold() < 0) {
            throw new BusinessException("预警阈值不能为负数");
        }
        if (request.getPurchasePrice() != null && request.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("采购价不能为负数");
        }
        if (request.getSellingPrice() != null && request.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("销售价不能为负数");
        }
        Part part = new Part();
        part.setPartNo(request.getPartNo());
        part.setPartName(request.getPartName());
        part.setCategory(request.getCategory() != null ? request.getCategory() : "GENERAL");
        part.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        part.setWarningThreshold(request.getWarningThreshold() != null ? request.getWarningThreshold() : 10);
        part.setUnit(request.getUnit() != null && !request.getUnit().isBlank() ? request.getUnit() : "个");
        part.setPurchasePrice(request.getPurchasePrice() != null ? request.getPurchasePrice() : BigDecimal.ZERO);
        part.setSellingPrice(request.getSellingPrice() != null ? request.getSellingPrice() : BigDecimal.ZERO);
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
        if (request.getPartName() != null && !request.getPartName().isBlank()) {
            part.setPartName(request.getPartName());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            part.setCategory(request.getCategory());
        }
        if (request.getWarningThreshold() != null) {
            if (request.getWarningThreshold() < 0) {
                throw new BusinessException("预警阈值不能为负数");
            }
            part.setWarningThreshold(request.getWarningThreshold());
        }
        part.setUnit(request.getUnit() != null && !request.getUnit().isBlank() ? request.getUnit() : part.getUnit());
        if (request.getPurchasePrice() != null) {
            if (request.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("采购价不能为负数");
            }
            part.setPurchasePrice(request.getPurchasePrice());
        }
        if (request.getSellingPrice() != null) {
            if (request.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("销售价不能为负数");
            }
            part.setSellingPrice(request.getSellingPrice());
        }
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
