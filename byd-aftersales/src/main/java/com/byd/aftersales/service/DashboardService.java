package com.byd.aftersales.service;

import com.byd.aftersales.dao.BatteryHealthDao;
import com.byd.aftersales.dao.PartDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.dao.WorkOrderDao;
import com.byd.aftersales.dto.DashboardStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;
    private final VehicleDao vehicleDao;
    private final WorkOrderDao workOrderDao;
    private final BatteryHealthDao batteryHealthDao;
    private final PartDao partDao;

    public DashboardService(DataSource dataSource, VehicleDao vehicleDao, WorkOrderDao workOrderDao,
                            BatteryHealthDao batteryHealthDao, PartDao partDao) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.vehicleDao = vehicleDao;
        this.workOrderDao = workOrderDao;
        this.batteryHealthDao = batteryHealthDao;
        this.partDao = partDao;
    }

    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        stats.setVehicleCount(vehicleDao.findAll().size());
        stats.setAppointmentPending(countAppointments("PENDING"));
        stats.setWorkOrderInProgress(
                workOrderDao.countByStatus("IN_PROGRESS") + workOrderDao.countByStatus("PART_WAITING"));
        stats.setBatteryDangerCount(batteryHealthDao.countByWarningLevel("DANGER"));
        stats.setLowStockPartCount(partDao.countLowStock());
        stats.setTechnicianWorkload(loadTechnicianWorkload());
        return stats;
    }

    private long countAppointments(String status) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointment WHERE status = ? AND deleted = 0", Long.class, status);
        return count == null ? 0 : count;
    }

    private List<Map<String, Object>> loadTechnicianWorkload() {
        return jdbcTemplate.queryForList("""
                SELECT u.user_id AS technicianId, u.real_name AS technicianName,
                       COUNT(w.work_order_id) AS activeOrders
                FROM sys_user u
                LEFT JOIN work_order w ON w.technician_id = u.user_id
                    AND w.deleted = 0
                    AND w.status IN ('ASSIGNED', 'IN_PROGRESS', 'PART_WAITING')
                WHERE u.role = 'TECHNICIAN' AND u.deleted = 0
                GROUP BY u.user_id, u.real_name
                ORDER BY activeOrders DESC
                """);
    }
}
