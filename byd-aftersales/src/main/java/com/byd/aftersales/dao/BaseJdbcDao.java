package com.byd.aftersales.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import javax.sql.DataSource;
import java.util.Objects;

public abstract class BaseJdbcDao extends JdbcDaoSupport {

    protected BaseJdbcDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    protected JdbcTemplate jdbc() {
        return Objects.requireNonNull(getJdbcTemplate());
    }
}
