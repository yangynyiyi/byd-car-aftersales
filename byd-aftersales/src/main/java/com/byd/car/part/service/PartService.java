package com.byd.car.part.service;

import com.byd.car.part.dto.PartCreateRequest;
import com.byd.car.part.entity.Part;

import java.util.List;

public interface PartService {

    Part create(PartCreateRequest request);

    Part getById(Long partId);

    List<Part> listAll();

    Part update(Long partId, PartCreateRequest request);

    void delete(Long partId);

    /** 补货 */
    Part addStock(Long partId, int quantity);

    /** 从 Redis 读取低库存预警备件列表 */
    List<String> getLowStockAlerts();
}
