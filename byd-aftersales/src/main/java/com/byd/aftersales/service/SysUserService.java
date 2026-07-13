package com.byd.aftersales.service;

import com.byd.aftersales.auth.AuthUser;
import com.byd.aftersales.auth.TokenStore;
import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.SysUserDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.domain.SysUser;
import com.byd.aftersales.domain.Vehicle;
import com.byd.aftersales.dto.LoginResponse;
import com.byd.aftersales.dto.RegisterRequest;
import com.byd.aftersales.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SysUserService {

    private final SysUserDao sysUserDao;
    private final TokenStore tokenStore;
    private final VehicleDao vehicleDao;
    private final VehicleService vehicleService;

    public SysUserService(SysUserDao sysUserDao, TokenStore tokenStore,
                          VehicleDao vehicleDao, VehicleService vehicleService) {
        this.sysUserDao = sysUserDao;
        this.tokenStore = tokenStore;
        this.vehicleDao = vehicleDao;
        this.vehicleService = vehicleService;
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

    public void resetPassword(Long userId) {
        if (sysUserDao.resetPassword(userId, "12345678") == 0) {
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

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        requireText(request.getUsername(), "用户名不能为空");
        requireText(request.getPassword(), "密码不能为空");
        requireText(request.getRealName(), "真实姓名不能为空");
        requireText(request.getPhone(), "手机号不能为空");
        requireText(request.getVin(), "VIN 不能为空");
        requireText(request.getLicensePlate(), "车牌不能为空");
        requireText(request.getModel(), "车型不能为空");
        if (request.getPassword().length() < 6) {
            throw new BusinessException("密码至少 6 位");
        }
        if (sysUserDao.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }

        String normalizedVin = request.getVin().trim().toUpperCase();
        if (vehicleDao.findByVin(normalizedVin).isPresent()) {
            throw new BusinessException("该车辆已登记，请联系门店绑定");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName().trim());
        user.setPhone(request.getPhone().trim());
        user.setRole("OWNER");
        user.setStatus("ENABLED");
        sysUserDao.insert(user);

        SysUser registered = sysUserDao.findByUsername(user.getUsername())
                .orElseThrow(() -> new BusinessException("注册失败，请重试"));

        Vehicle vehicle = new Vehicle();
        vehicle.setVin(normalizedVin);
        vehicle.setOwnerId(registered.getUserId());
        vehicle.setLicensePlate(request.getLicensePlate().trim());
        vehicle.setModel(request.getModel().trim());
        String batteryModel = request.getBatteryModel();
        vehicle.setBatteryModel(batteryModel != null && !batteryModel.isBlank()
                ? batteryModel.trim() : "Blade Battery");
        vehicle.setCurrentMileage(BigDecimal.ZERO);
        vehicle.setVehicleStatus("NORMAL");
        Long defaultAdvisorId = sysUserDao.findAll().stream()
                .filter(u -> "ADVISOR".equals(u.getRole()) && "ENABLED".equals(u.getStatus()))
                .map(SysUser::getUserId)
                .findFirst()
                .orElseThrow(() -> new BusinessException("暂无可用顾问，请联系门店"));
        vehicle.setAdvisorId(defaultAdvisorId);
        vehicleService.create(vehicle, null);

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
