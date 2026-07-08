package com.byd.aftersales.service;

import com.byd.aftersales.auth.AuthContext;
import com.byd.aftersales.auth.AuthUser;
import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.common.IdGenerator;
import com.byd.aftersales.dao.AppointmentDao;
import com.byd.aftersales.dao.ServiceCenterDao;
import com.byd.aftersales.dao.SysUserDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.domain.Appointment;
import com.byd.aftersales.domain.Vehicle;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {

    private static final Set<String> ALLOWED_STATUS = Set.of("PENDING", "CONFIRMED", "CANCELLED", "ARRIVED", "COMPLETED");
    private static final Set<String> ALLOWED_SERVICE_TYPE = Set.of(
            "SCHEDULED_MAINTENANCE", "ANNUAL_INSPECTION", "FAULT_REPAIR", "EMERGENCY_RESCUE");

    private final AppointmentDao appointmentDao;
    private final VehicleDao vehicleDao;
    private final SysUserDao sysUserDao;
    private final ServiceCenterDao serviceCenterDao;

    public AppointmentService(AppointmentDao appointmentDao,
                              VehicleDao vehicleDao,
                              SysUserDao sysUserDao,
                              ServiceCenterDao serviceCenterDao) {
        this.appointmentDao = appointmentDao;
        this.vehicleDao = vehicleDao;
        this.sysUserDao = sysUserDao;
        this.serviceCenterDao = serviceCenterDao;
    }

    public void create(Appointment appointment) {
        if (appointment.getAppointmentNo() == null || appointment.getAppointmentNo().isBlank()) {
            appointment.setAppointmentNo(IdGenerator.generate("AP"));
        }
        if (appointment.getStatus() == null || appointment.getStatus().isBlank()) {
            appointment.setStatus("PENDING");
        }
        if (appointment.getServiceType() == null || appointment.getServiceType().isBlank()) {
            appointment.setServiceType("FAULT_REPAIR");
        }
        Vehicle vehicle = vehicleDao.findByVin(appointment.getVin()).orElseThrow(() -> new BusinessException("车辆不存在"));
        fillOwnerId(appointment, vehicle);
        validate(appointment);
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
        Appointment appointment = appointmentDao.findByNo(appointmentNo)
                .orElseThrow(() -> new BusinessException("预约记录不存在"));
        enrichDisplayFields(appointment);
        return appointment;
    }

    public List<Appointment> findByVin(String vin) {
        return enrichDisplayFields(appointmentDao.findByVin(vin));
    }

    public List<Appointment> findAll() {
        return enrichDisplayFields(appointmentDao.findAll());
    }

    private List<Appointment> enrichDisplayFields(List<Appointment> appointments) {
        appointments.forEach(this::enrichDisplayFields);
        return appointments;
    }

    private void enrichDisplayFields(Appointment appointment) {
        if (appointment.getOwnerId() != null) {
            sysUserDao.findById(appointment.getOwnerId())
                    .ifPresent(user -> appointment.setOwnerName(user.getRealName()));
        }
        if (appointment.getCenterId() != null) {
            serviceCenterDao.findById(appointment.getCenterId())
                    .ifPresent(center -> appointment.setCenterName(center.getCenterName()));
        }
    }

    private void fillOwnerId(Appointment appointment, Vehicle vehicle) {
        if (appointment.getOwnerId() != null && appointment.getOwnerId() > 0) {
            return;
        }
        AuthUser current = AuthContext.get();
        if (current != null && "OWNER".equals(current.getRole())) {
            appointment.setOwnerId(current.getUserId());
            return;
        }
        appointment.setOwnerId(vehicle.getOwnerId());
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
        if (!ALLOWED_SERVICE_TYPE.contains(appointment.getServiceType())) {
            throw new BusinessException("预约服务类型不合法");
        }
        if (appointment.getProblemDescription() == null || appointment.getProblemDescription().isBlank()) {
            throw new BusinessException("问题描述不能为空");
        }
        if (!ALLOWED_STATUS.contains(appointment.getStatus())) {
            throw new BusinessException("预约状态不合法");
        }
    }
}
