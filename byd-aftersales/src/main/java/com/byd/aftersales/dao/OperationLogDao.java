package com.byd.aftersales.dao;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;

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
}
