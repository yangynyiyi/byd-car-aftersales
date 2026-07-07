package com.byd.car.part.service.impl;

import com.byd.car.common.exception.BusinessException;
import com.byd.car.part.dao.PartDao;
import com.byd.car.part.dto.PartCreateRequest;
import com.byd.car.part.entity.Part;
import com.byd.car.part.service.PartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PartServiceImpl implements PartService {

    private static final String LOW_STOCK_KEY_PREFIX = "part:low_stock:";

    private final PartDao partDao;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Part create(PartCreateRequest request) {
        Part part = new Part();
        part.setPartNo(request.getPartNo());
        part.setPartName(request.getPartName());
        part.setCategory(request.getCategory());
        part.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        part.setWarningThreshold(request.getWarningThreshold() != null ? request.getWarningThreshold() : 10);
        part.setPurchasePrice(request.getPurchasePrice());
        part.setSellingPrice(request.getSellingPrice());
        part.setStatus("ENABLED");
        Long id = partDao.insert(part);
        return partDao.findById(id);
    }

    @Override
    public Part getById(Long partId) {
        Part part = partDao.findById(partId);
        if (part == null) {
            throw new BusinessException(404, "备件不存在");
        }
        return part;
    }

    @Override
    public List<Part> listAll() {
        return partDao.findAll();
    }

    @Override
    public Part update(Long partId, PartCreateRequest request) {
        Part part = getById(partId);
        part.setPartName(request.getPartName());
        part.setCategory(request.getCategory());
        part.setWarningThreshold(request.getWarningThreshold());
        part.setPurchasePrice(request.getPurchasePrice());
        part.setSellingPrice(request.getSellingPrice());
        partDao.update(part);
        return partDao.findById(partId);
    }

    @Override
    public void delete(Long partId) {
        getById(partId);
        partDao.deleteById(partId);
    }

    @Override
    public Part addStock(Long partId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("补货数量必须大于 0");
        }
        getById(partId);
        partDao.addStock(partId, quantity);
        Part updated = partDao.findById(partId);
        // 补货后若库存恢复，清除预警
        if (updated.getStockQuantity() >= updated.getWarningThreshold()) {
            redisTemplate.delete(LOW_STOCK_KEY_PREFIX + partId);
        }
        return updated;
    }

    @Override
    public List<String> getLowStockAlerts() {
        Set<String> keys = redisTemplate.keys(LOW_STOCK_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<String> alerts = new ArrayList<>();
        for (String key : keys) {
            String value = redisTemplate.opsForValue().get(key);
            alerts.add(key.replace(LOW_STOCK_KEY_PREFIX, "partId=") + ", stock=" + value);
        }
        return alerts;
    }
}
