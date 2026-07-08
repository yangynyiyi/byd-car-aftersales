package com.byd.aftersales.dao;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Component
public class OperationLogDao extends BaseJdbcDao {

    public OperationLogDao(DataSource dataSource) {
        super(dataSource);
    }

    public void insert(String businessType, Long businessId, String action, Long operatorId, String detail) {
        jdbc().update("""
                INSERT INTO operation_log (business_type, business_id, action, operator_id, detail)
                VALUES (?, ?, ?, ?, ?)
                """, businessType, businessId, action, operatorId, detail);
    }

    public List<Map<String, Object>> findWorkOrderSupervisionsForTechnician(Long technicianId) {
        return jdbc().queryForList("""
                SELECT
                    log.log_id AS logId,
                    log.business_id AS workOrderId,
                    log.operator_id AS operatorId,
                    log.detail,
                    log.created_at AS createdAt
                FROM operation_log log
                JOIN work_order wo ON wo.work_order_id = log.business_id
                WHERE log.business_type = 'WORK_ORDER'
                  AND log.action = 'SUPERVISE'
                  AND wo.deleted = 0
                  AND wo.status NOT IN ('COMPLETED', 'CANCELLED')
                  AND (wo.technician_id = ? OR wo.technician_id IS NULL)
                ORDER BY log.created_at DESC
                """, technicianId);
    }
}
