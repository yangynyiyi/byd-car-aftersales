package com.byd.aftersales.dao;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;

@Component
public class TechnicianRateDao extends BaseJdbcDao {

    public TechnicianRateDao(DataSource dataSource) {
        super(dataSource);
    }

    public BigDecimal getDefaultLaborCost(Long technicianId) {
        if (technicianId == null) {
            return new BigDecimal("200.00");
        }
        BigDecimal rate = jdbc().query(
                "SELECT daily_rate FROM technician_rate WHERE technician_id = ? AND status = 'ENABLED'",
                rs -> rs.next() ? rs.getBigDecimal("daily_rate") : null,
                technicianId);
        return rate != null ? rate : new BigDecimal("200.00");
    }
}
