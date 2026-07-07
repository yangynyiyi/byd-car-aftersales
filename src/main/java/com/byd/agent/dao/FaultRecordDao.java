package com.byd.agent.dao;

import com.byd.agent.model.FaultRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class FaultRecordDao extends JdbcDaoSupport {

    @Autowired
    public void setDataSourceInternal(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public FaultRecord selectById(Long faultId) {
        String sql = "SELECT * FROM fault_record WHERE fault_id = ? AND deleted = 0";
        List<FaultRecord> list = getJdbcTemplate().query(sql,
                (rs, rowNum) -> {
                    FaultRecord f = new FaultRecord();
                    f.setFaultId(rs.getLong("fault_id"));
                    f.setFaultNo(rs.getString("fault_no"));
                    f.setAppointmentId(rs.getLong("appointment_id"));
                    f.setVin(rs.getString("vin"));
                    f.setOwnerId(rs.getLong("owner_id"));
                    f.setAdvisorId(rs.getLong("advisor_id"));
                    f.setFaultDescription(rs.getString("fault_description"));
                    f.setFaultLevel(rs.getString("fault_level"));
                    f.setStatus(rs.getString("status"));
                    f.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    f.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    f.setDeleted(rs.getInt("deleted"));
                    return f;
                },
                faultId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<FaultRecord> selectByVin(String vin) {
        String sql = "SELECT * FROM fault_record WHERE vin = ? AND deleted = 0 ORDER BY created_at DESC";
        return getJdbcTemplate().query(sql,
                (rs, rowNum) -> {
                    FaultRecord f = new FaultRecord();
                    f.setFaultId(rs.getLong("fault_id"));
                    f.setFaultNo(rs.getString("fault_no"));
                    f.setAppointmentId(rs.getObject("appointment_id") != null ? rs.getLong("appointment_id") : null);
                    f.setVin(rs.getString("vin"));
                    f.setOwnerId(rs.getLong("owner_id"));
                    f.setAdvisorId(rs.getObject("advisor_id") != null ? rs.getLong("advisor_id") : null);
                    f.setFaultDescription(rs.getString("fault_description"));
                    f.setFaultLevel(rs.getString("fault_level"));
                    f.setStatus(rs.getString("status"));
                    f.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    f.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    f.setDeleted(rs.getInt("deleted"));
                    return f;
                },
                vin);
    }
}
