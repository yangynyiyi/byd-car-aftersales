package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.SysUserDao;
import com.byd.aftersales.domain.SysUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserService {

    private final SysUserDao sysUserDao;

    public SysUserService(SysUserDao sysUserDao) {
        this.sysUserDao = sysUserDao;
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

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
    }
}
