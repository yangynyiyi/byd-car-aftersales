package com.byd.aftersales.dao;

import com.byd.aftersales.domain.SysUser;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Component
public class SysUserDao extends BaseJdbcDao {

    private final RowMapper<SysUser> rowMapper = (rs, rowNum) -> {
        SysUser user = new SysUser();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRealName(rs.getString("real_name"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        user.setDeleted(rs.getInt("deleted"));
        return user;
    };

    public SysUserDao(DataSource dataSource) {
        super(dataSource);
    }

    public int insert(SysUser user) {
        String sql = """
                INSERT INTO sys_user (username, password, real_name, phone, role, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        return jdbc().update(sql, user.getUsername(), user.getPassword(), user.getRealName(),
                user.getPhone(), user.getRole(), user.getStatus());
    }

    public int update(SysUser user) {
        String sql = """
                UPDATE sys_user
                SET real_name = ?, phone = ?, role = ?, status = ?
                WHERE user_id = ? AND deleted = 0
                """;
        return jdbc().update(sql, user.getRealName(), user.getPhone(), user.getRole(),
                user.getStatus(), user.getUserId());
    }

    public int softDelete(Long userId) {
        return jdbc().update("UPDATE sys_user SET deleted = 1 WHERE user_id = ?", userId);
    }

    public int resetPassword(Long userId, String password) {
        return jdbc().update(
                "UPDATE sys_user SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ? AND deleted = 0",
                password, userId);
    }

    public Optional<SysUser> findById(Long userId) {
        List<SysUser> users = jdbc().query("SELECT * FROM sys_user WHERE user_id = ? AND deleted = 0",
                rowMapper, userId);
        return users.stream().findFirst();
    }

    public Optional<SysUser> findByUsername(String username) {
        List<SysUser> users = jdbc().query(
                "SELECT * FROM sys_user WHERE username = ? AND deleted = 0",
                rowMapper, username);
        return users.stream().findFirst();
    }

    public Optional<SysUser> findOwnerByPhone(String phone) {
        List<SysUser> users = jdbc().query(
                "SELECT * FROM sys_user WHERE phone = ? AND role = 'OWNER' AND deleted = 0 LIMIT 1",
                rowMapper, phone);
        return users.stream().findFirst();
    }

    public List<SysUser> findAll() {
        return jdbc().query("SELECT * FROM sys_user WHERE deleted = 0 ORDER BY user_id DESC", rowMapper);
    }
}
