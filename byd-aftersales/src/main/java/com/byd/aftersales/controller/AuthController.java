package com.byd.aftersales.controller;

import com.byd.aftersales.auth.AuthContext;
import com.byd.aftersales.auth.AuthUser;
import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.common.UnauthorizedException;
import com.byd.aftersales.dto.LoginRequest;
import com.byd.aftersales.dto.LoginResponse;
import com.byd.aftersales.dto.RegisterRequest;
import com.byd.aftersales.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserService sysUserService;

    public AuthController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(sysUserService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.ok(sysUserService.register(request));
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse> me() {
        AuthUser user = AuthContext.get();
        if (user == null) {
            throw new UnauthorizedException("未登录或登录已过期");
        }
        return ApiResponse.ok(sysUserService.currentUser(user));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            sysUserService.logout(authorization.substring("Bearer ".length()).trim());
        }
        return ApiResponse.ok();
    }
}
