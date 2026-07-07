package com.byd.agent.dao;

import com.byd.agent.model.WorkOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class WorkOrderDao extends JdbcDaoSupport {

    @Autowired
    public void setDataSourceInternal(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public List<WorkOrder> selectRecentByVin(String vin, int limit) {
        String sql = "SELECT wo.* FROM work_order wo " +
                "JOIN fault_record f ON wo.fault_id = f.fault_id " +
                "WHERE f.vin = ? AND wo.deleted = 0 " +
                "ORDER BY wo.created_at DESC LIMIT ?";
        return getJdbcTemplate().query(sql,
                (rs, rowNum) -> {
                    WorkOrder w = new WorkOrder();
                    w.setWorkOrderId(rs.getLong("work_order_id"));
                    w.setWorkOrderNo(rs.getString("work_order_no"));
                    w.setFaultId(rs.getLong("fault_id"));
                    w.setDiagnosisId(rs.getObject("diagnosis_id") != null ? rs.getLong("diagnosis_id") : null);
                    w.setTechnicianId(rs.getObject("technician_id") != null ? rs.getLong("technician_id") : null);
                    w.setStatus(rs.getString("status"));
                    w.setLaborCost(rs.getBigDecimal("labor_cost"));
                    w.setRepairResult(rs.getString("repair_result"));
                    w.setStartedAt(rs.getTimestamp("started_at") != null
                            ? rs.getTimestamp("started_at").toLocalDateTime() : null);
                    w.setFinishedAt(rs.getTimestamp("finished_at") != null
                            ? rs.getTimestamp("finished_at").toLocalDateTime() : null);
                    w.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    w.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    w.setDeleted(rs.getInt("deleted"));
                    return w;
                },
                vin, limit);
    }
}
