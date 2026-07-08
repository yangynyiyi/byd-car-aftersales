package com.byd.aftersales.service;

import com.byd.aftersales.auth.AuthUser;
import com.byd.aftersales.auth.TokenStore;
import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.SysUserDao;
import com.byd.aftersales.domain.SysUser;
import com.byd.aftersales.dto.LoginResponse;
import com.byd.aftersales.dto.RegisterRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserService {

    private final SysUserDao sysUserDao;
    private final TokenStore tokenStore;

    public SysUserService(SysUserDao sysUserDao, TokenStore tokenStore) {
        this.sysUserDao = sysUserDao;
        this.tokenStore = tokenStore;
    }

    public void create(SysUser user) {
        requireText(user.getUsername(), "用户名不能为空");
        requireText(user.getPassword(), "密码不能为空");
        requireText(user.getRealName(), "真实姓名不能为空");
        requireText(user.getPhone(), "手机号不能为空");
        requireText(user.getRole(), "角色不能为空");
        if (user.getStatus() == null || user.getStatus().isBlank()) {
            user.setStatus("ENABLED");
        }
        sysUserDao.insert(user);
    }

    public void update(Long userId, SysUser user) {
        user.setUserId(userId);
        if (sysUserDao.update(user) == 0) {
            throw new BusinessException("用户不存在");
        }
    }

    public void delete(Long userId) {
        if (sysUserDao.softDelete(userId) == 0) {
            throw new BusinessException("用户不存在");
        }
    }

    public SysUser findById(Long userId) {
        return sysUserDao.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
    }

    public List<SysUser> findAll() {
        return sysUserDao.findAll();
    }

    public LoginResponse login(String username, String password) {
        requireText(username, "用户名不能为空");
        requireText(password, "密码不能为空");
        SysUser user = sysUserDao.findByUsername(username)
                .orElseThrow(() -> new BusinessException("账号或密码错误"));
        if (!"ENABLED".equals(user.getStatus())) {
            throw new BusinessException("账号已禁用");
        }
        if (!password.equals(user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        String role = normalizeRole(user.getRole());
        Long ownerId = "OWNER".equals(role) ? user.getUserId() : null;
        AuthUser authUser = new AuthUser(user.getUserId(), user.getUsername(), user.getRealName(), role, ownerId);
        LoginResponse response = new LoginResponse();
        response.setToken(tokenStore.issue(authUser));
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(role);
        response.setOwnerId(ownerId);
        return response;
    }

    public LoginResponse currentUser(AuthUser authUser) {
        LoginResponse response = new LoginResponse();
        response.setUserId(authUser.getUserId());
        response.setUsername(authUser.getUsername());
        response.setRealName(authUser.getRealName());
        response.setRole(authUser.getRole());
        response.setOwnerId(authUser.getOwnerId());
        return response;
    }

    public void logout(String token) {
        tokenStore.revoke(token);
    }

    public LoginResponse register(RegisterRequest request) {
        requireText(request.getUsername(), "用户名不能为空");
        requireText(request.getPassword(), "密码不能为空");
        requireText(request.getRealName(), "真实姓名不能为空");
        requireText(request.getPhone(), "手机号不能为空");
        if (request.getPassword().length() < 6) {
            throw new BusinessException("密码至少 6 位");
        }
        if (sysUserDao.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName().trim());
        user.setPhone(request.getPhone().trim());
        user.setRole("OWNER");
        user.setStatus("ENABLED");
        sysUserDao.insert(user);
        return login(user.getUsername(), request.getPassword());
    }

    private String normalizeRole(String role) {
        if ("MANAGER".equals(role)) {
            return "SERVICE_MANAGER";
        }
        return role;
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
    }
}
