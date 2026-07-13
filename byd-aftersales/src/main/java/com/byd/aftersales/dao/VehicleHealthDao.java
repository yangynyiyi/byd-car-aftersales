package com.byd.aftersales.dao;

import com.byd.aftersales.domain.VehicleHealthItem;
import com.byd.aftersales.domain.VehicleHealthSnapshot;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class VehicleHealthDao extends BaseJdbcDao {

    private final RowMapper<VehicleHealthSnapshot> snapshotMapper = (rs, rowNum) -> {
        VehicleHealthSnapshot snapshot = new VehicleHealthSnapshot();
        snapshot.setSnapshotId(rs.getLong("snapshot_id"));
        snapshot.setVin(rs.getString("vin"));
        snapshot.setHealthScore(rs.getInt("health_score"));
        snapshot.setOverallLevel(rs.getString("overall_level"));
        snapshot.setSummary(rs.getString("summary"));
        snapshot.setSuggestion(rs.getString("suggestion"));
        Timestamp detectTime = rs.getTimestamp("detect_time");
        snapshot.setDetectTime(detectTime == null ? null : detectTime.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        snapshot.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return snapshot;
    };

    private final RowMapper<VehicleHealthItem> itemMapper = (rs, rowNum) -> {
        VehicleHealthItem item = new VehicleHealthItem();
        item.setItemId(rs.getLong("item_id"));
        item.setSnapshotId(rs.getLong("snapshot_id"));
        item.setItemType(rs.getString("item_type"));
        item.setItemName(rs.getString("item_name"));
        item.setLevel(rs.getString("level"));
        item.setMetricValue(rs.getString("metric_value"));
        item.setDescription(rs.getString("description"));
        item.setActionSuggestion(rs.getString("action_suggestion"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        item.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return item;
    };

    public VehicleHealthDao(DataSource dataSource) {
        super(dataSource);
    }

    public Long insertSnapshot(VehicleHealthSnapshot snapshot) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO vehicle_health_snapshot
                        (vin, health_score, overall_level, summary, suggestion, detect_time)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, snapshot.getVin());
            ps.setInt(2, snapshot.getHealthScore());
            ps.setString(3, snapshot.getOverallLevel());
            ps.setString(4, snapshot.getSummary());
            ps.setString(5, snapshot.getSuggestion());
            ps.setTimestamp(6, Timestamp.valueOf(snapshot.getDetectTime()));
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public int insertItem(Long snapshotId, VehicleHealthItem item) {
        return jdbc().update("""
                INSERT INTO vehicle_health_item
                    (snapshot_id, item_type, item_name, level, metric_value, description, action_suggestion)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, snapshotId, item.getItemType(), item.getItemName(), item.getLevel(),
                item.getMetricValue(), item.getDescription(), item.getActionSuggestion());
    }

    public Optional<VehicleHealthSnapshot> findLatestByVin(String vin) {
        List<VehicleHealthSnapshot> snapshots = jdbc().query("""
                SELECT * FROM vehicle_health_snapshot
                WHERE vin = ?
                ORDER BY detect_time DESC, snapshot_id DESC
                LIMIT 1
                """, snapshotMapper, vin);
        return snapshots.stream().findFirst();
    }

    public List<VehicleHealthSnapshot> findByVin(String vin) {
        return jdbc().query("""
                SELECT * FROM vehicle_health_snapshot
                WHERE vin = ?
                ORDER BY detect_time DESC, snapshot_id DESC
                """, snapshotMapper, vin);
    }

    public List<VehicleHealthItem> findItemsBySnapshotId(Long snapshotId) {
        return jdbc().query("""
                SELECT * FROM vehicle_health_item
                WHERE snapshot_id = ?
                ORDER BY item_id ASC
                """, itemMapper, snapshotId);
    }
}
