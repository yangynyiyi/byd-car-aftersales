package com.byd.aftersales.dao;

import com.byd.aftersales.domain.Appointment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Component
public class AppointmentDao extends BaseJdbcDao {

    private final RowMapper<Appointment> rowMapper = (rs, rowNum) -> {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getLong("appointment_id"));
        appointment.setAppointmentNo(rs.getString("appointment_no"));
        appointment.setVin(rs.getString("vin"));
        appointment.setOwnerId(rs.getLong("owner_id"));
        appointment.setCenterId(rs.getLong("center_id"));
        appointment.setAppointmentTime(rs.getTimestamp("appointment_time").toLocalDateTime());
        appointment.setServiceType(rs.getString("service_type"));
        appointment.setProblemDescription(rs.getString("problem_description"));
        appointment.setStatus(rs.getString("status"));
        appointment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        appointment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        appointment.setDeleted(rs.getInt("deleted"));
        return appointment;
    };

    public AppointmentDao(DataSource dataSource) {
        super(dataSource);
    }

    public int insert(Appointment appointment) {
        String sql = """
                INSERT INTO appointment
                (appointment_no, vin, owner_id, center_id, appointment_time, service_type, problem_description, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        return jdbc().update(sql, appointment.getAppointmentNo(), appointment.getVin(), appointment.getOwnerId(),
                appointment.getCenterId(), appointment.getAppointmentTime(), appointment.getServiceType(),
                appointment.getProblemDescription(), appointment.getStatus());
    }

    public int updateStatus(String appointmentNo, String status) {
        return jdbc().update("UPDATE appointment SET status = ? WHERE appointment_no = ? AND deleted = 0",
                status, appointmentNo);
    }

    public int softDelete(String appointmentNo) {
        return jdbc().update("UPDATE appointment SET deleted = 1 WHERE appointment_no = ?", appointmentNo);
    }

    public Optional<Appointment> findByNo(String appointmentNo) {
        List<Appointment> appointments = jdbc().query("""
                SELECT * FROM appointment WHERE appointment_no = ? AND deleted = 0
                """, rowMapper, appointmentNo);
        return appointments.stream().findFirst();
    }

    public List<Appointment> findByVin(String vin) {
        return jdbc().query("""
                SELECT * FROM appointment WHERE vin = ? AND deleted = 0 ORDER BY appointment_time DESC
                """, rowMapper, vin);
    }

    public List<Appointment> findAll() {
        return jdbc().query("SELECT * FROM appointment WHERE deleted = 0 ORDER BY appointment_time DESC", rowMapper);
    }
}
