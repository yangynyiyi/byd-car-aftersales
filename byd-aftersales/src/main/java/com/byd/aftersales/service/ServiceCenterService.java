package com.byd.aftersales.service;

import com.byd.aftersales.dao.ServiceCenterDao;
import com.byd.aftersales.domain.ServiceCenter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCenterService {

    private final ServiceCenterDao serviceCenterDao;

    public ServiceCenterService(ServiceCenterDao serviceCenterDao) {
        this.serviceCenterDao = serviceCenterDao;
    }

    public List<ServiceCenter> findAll() {
        return serviceCenterDao.findAll();
    }
}
