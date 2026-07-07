package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.common.IdGenerator;
import com.byd.aftersales.dao.AppointmentDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.domain.Appointment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {

    private static final Set<String> ALLOWED_STATUS = Set.of("PENDING", "CONFIRMED", "CANCELLED", "ARRIVED", "COMPLETED");

    private final AppointmentDao appointmentDao;
    private final VehicleDao vehicleDao;

    public AppointmentService(AppointmentDao appointmentDao, VehicleDao vehicleDao) {
        this.appointmentDao = appointmentDao;
        this.vehicleDao = vehicleDao;
    }

    public void create(Appointment appointment) {
        if (appointment.getAppointmentNo() == null || appointment.getAppointmentNo().isBlank()) {
            appointment.setAppointmentNo(IdGenerator.generate("AP"));
        }
        if (appointment.getStatus() == null || appointment.getStatus().isBlank()) {
            appointment.setStatus("PENDING");
        }
        validate(appointment);
        vehicleDao.findByVin(appointment.getVin()).orElseThrow(() -> new BusinessException("车辆不存在"));
        appointmentDao.insert(appointment);
    }

    public void updateStatus(String appointmentNo, String status) {
        if (!ALLOWED_STATUS.contains(status)) {
            throw new BusinessException("预约状态不合法");
        }
        if (appointmentDao.updateStatus(appointmentNo, status) == 0) {
            throw new BusinessException("预约记录不存在");
        }
    }

    public void delete(String appointmentNo) {
        if (appointmentDao.softDelete(appointmentNo) == 0) {
            throw new BusinessException("预约记录不存在");
        }
    }

    public Appointment findByNo(String appointmentNo) {
        return appointmentDao.findByNo(appointmentNo)
                .orElseThrow(() -> new BusinessException("预约记录不存在"));
    }

    public List<Appointment> findByVin(String vin) {
        return appointmentDao.findByVin(vin);
    }

    public List<Appointment> findAll() {
        return appointmentDao.findAll();
    }

    private void validate(Appointment appointment) {
        if (appointment.getVin() == null || appointment.getVin().isBlank()) {
            throw new BusinessException("VIN 不能为空");
        }
        if (appointment.getOwnerId() == null) {
            throw new BusinessException("车主 ID 不能为空");
        }
        if (appointment.getCenterId() == null) {
            throw new BusinessException("服务中心 ID 不能为空");
        }
        if (appointment.getAppointmentTime() == null) {
            throw new BusinessException("预约时间不能为空");
        }
        if (appointment.getProblemDescription() == null || appointment.getProblemDescription().isBlank()) {
            throw new BusinessException("问题描述不能为空");
        }
        if (!ALLOWED_STATUS.contains(appointment.getStatus())) {
            throw new BusinessException("预约状态不合法");
        }
    }
}
