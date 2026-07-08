package com.byd.aftersales.service;

import com.byd.aftersales.auth.AuthContext;
import com.byd.aftersales.auth.AuthUser;
import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.common.IdGenerator;
import com.byd.aftersales.dao.FaultRecordDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.domain.FaultRecord;
import com.byd.aftersales.domain.Vehicle;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class FaultRecordService {

    private static final Set<String> ALLOWED_STATUS = Set.of("REGISTERED", "DIAGNOSED", "WORK_ORDER_CREATED", "CLOSED");
    private static final Set<String> ALLOWED_LEVEL = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");

    private final FaultRecordDao faultRecordDao;
    private final VehicleDao vehicleDao;

    public FaultRecordService(FaultRecordDao faultRecordDao, VehicleDao vehicleDao) {
        this.faultRecordDao = faultRecordDao;
        this.vehicleDao = vehicleDao;
    }

    public void create(FaultRecord record) {
        if (record.getFaultNo() == null || record.getFaultNo().isBlank()) {
            record.setFaultNo(IdGenerator.generate("FLT"));
        }
        if (record.getFaultLevel() == null || record.getFaultLevel().isBlank()) {
            record.setFaultLevel("LOW");
        }
        if (record.getStatus() == null || record.getStatus().isBlank()) {
            record.setStatus("REGISTERED");
        }
        Vehicle vehicle = vehicleDao.findByVin(record.getVin()).orElseThrow(() -> new BusinessException("车辆不存在"));
        fillActorIds(record, vehicle);
        validate(record);
        faultRecordDao.insert(record);
    }

    public void updateStatus(String faultNo, String status) {
        if (!ALLOWED_STATUS.contains(status)) {
            throw new BusinessException("故障状态不合法");
        }
        if (faultRecordDao.updateStatus(faultNo, status) == 0) {
            throw new BusinessException("故障记录不存在");
        }
    }

    public void update(String faultNo, FaultRecord record) {
        FaultRecord existing = findByNo(faultNo);
        if ("WORK_ORDER_CREATED".equals(existing.getStatus())) {
            throw new BusinessException("已生成工单的故障不可编辑");
        }
        record.setFaultNo(faultNo);
        if (record.getStatus() == null || record.getStatus().isBlank()) {
            record.setStatus(existing.getStatus());
        }
        Vehicle vehicle = vehicleDao.findByVin(record.getVin()).orElseThrow(() -> new BusinessException("车辆不存在"));
        fillActorIds(record, vehicle);
        if (record.getAdvisorId() == null) {
            record.setAdvisorId(existing.getAdvisorId());
        }
        validate(record);
        if (faultRecordDao.update(record) == 0) {
            throw new BusinessException("故障记录不存在");
        }
    }

    public void delete(String faultNo) {
        if (faultRecordDao.softDelete(faultNo) == 0) {
            throw new BusinessException("故障记录不存在");
        }
    }

    public FaultRecord findByNo(String faultNo) {
        return faultRecordDao.findByNo(faultNo)
                .orElseThrow(() -> new BusinessException("故障记录不存在"));
    }

    public List<FaultRecord> findByVin(String vin) {
        return faultRecordDao.findByVin(vin);
    }

    public List<FaultRecord> findAll() {
        return faultRecordDao.findAll();
    }

    private void fillActorIds(FaultRecord record, Vehicle vehicle) {
        if (record.getOwnerId() == null) {
            record.setOwnerId(vehicle.getOwnerId());
        }
        AuthUser current = AuthContext.get();
        if (record.getAdvisorId() == null && current != null && "ADVISOR".equals(current.getRole())) {
            record.setAdvisorId(current.getUserId());
        }
    }

    private void validate(FaultRecord record) {
        if (record.getVin() == null || record.getVin().isBlank()) {
            throw new BusinessException("VIN 不能为空");
        }
        if (record.getOwnerId() == null) {
            throw new BusinessException("车主 ID 不能为空");
        }
        if (record.getAdvisorId() == null) {
            throw new BusinessException("售后顾问 ID 不能为空");
        }
        if (record.getFaultDescription() == null || record.getFaultDescription().isBlank()) {
            throw new BusinessException("故障描述不能为空");
        }
        if (!ALLOWED_LEVEL.contains(record.getFaultLevel())) {
            throw new BusinessException("故障等级不合法");
        }
        if (!ALLOWED_STATUS.contains(record.getStatus())) {
            throw new BusinessException("故障状态不合法");
        }
    }
}
