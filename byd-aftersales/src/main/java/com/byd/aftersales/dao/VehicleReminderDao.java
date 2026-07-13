package com.byd.aftersales.dao;

import com.byd.aftersales.domain.VehicleReminder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;

@Component
public class VehicleReminderDao extends BaseJdbcDao {

    private final RowMapper<VehicleReminder> rowMapper = (rs, rowNum) -> {
        VehicleReminder reminder = new VehicleReminder();
        reminder.setReminderId(rs.getLong("reminder_id"));
        reminder.setReminderNo(rs.getString("reminder_no"));
        reminder.setVin(rs.getString("vin"));
        reminder.setOwnerId(rs.getLong("owner_id"));
        reminder.setReminderType(rs.getString("reminder_type"));
        reminder.setLevel(rs.getString("level"));
        reminder.setTitle(rs.getString("title"));
        reminder.setContent(rs.getString("content"));
        Timestamp dueTime = rs.getTimestamp("due_time");
        reminder.setDueTime(dueTime == null ? null : dueTime.toLocalDateTime());
        reminder.setStatus(rs.getString("status"));
        reminder.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        reminder.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        reminder.setDeleted(rs.getInt("deleted"));
        return reminder;
    };

    public VehicleReminderDao(DataSource dataSource) {
        super(dataSource);
    }

    public int insert(VehicleReminder reminder) {
        return jdbc().update("""
                INSERT INTO vehicle_reminder
                    (reminder_no, vin, owner_id, reminder_type, level, title, content, due_time, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, reminder.getReminderNo(), reminder.getVin(), reminder.getOwnerId(),
                reminder.getReminderType(), reminder.getLevel(), reminder.getTitle(), reminder.getContent(),
                reminder.getDueTime(), reminder.getStatus());
    }

    public boolean existsOpen(String vin, String reminderType) {
        Long count = jdbc().queryForObject("""
                SELECT COUNT(*) FROM vehicle_reminder
                WHERE vin = ? AND reminder_type = ? AND status <> 'DONE' AND deleted = 0
                """, Long.class, vin, reminderType);
        return count != null && count > 0;
    }

    public List<VehicleReminder> findByOwnerId(Long ownerId) {
        return jdbc().query("""
                SELECT * FROM vehicle_reminder
                WHERE owner_id = ? AND deleted = 0
                ORDER BY
                    CASE level WHEN 'DANGER' THEN 0 WHEN 'WARNING' THEN 1 ELSE 2 END,
                    COALESCE(due_time, created_at) ASC
                """, rowMapper, ownerId);
    }

    public int markRead(Long reminderId, Long ownerId) {
        return jdbc().update("""
                UPDATE vehicle_reminder
                SET status = 'READ'
                WHERE reminder_id = ? AND owner_id = ? AND deleted = 0
                """, reminderId, ownerId);
    }
}
