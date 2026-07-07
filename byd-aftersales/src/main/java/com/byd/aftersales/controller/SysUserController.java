package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.SysUser;
import com.byd.aftersales.service.SysUserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody SysUser user) {
        sysUserService.create(user);
        return ApiResponse.ok();
    }

    @PutMapping("/{userId}")
    public ApiResponse<Void> update(@PathVariable Long userId, @RequestBody SysUser user) {
        sysUserService.update(userId, user);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> delete(@PathVariable Long userId) {
        sysUserService.delete(userId);
        return ApiResponse.ok();
    }

    @GetMapping("/{userId}")
    public ApiResponse<SysUser> findById(@PathVariable Long userId) {
        return ApiResponse.ok(sysUserService.findById(userId));
    }

    @GetMapping
    public ApiResponse<List<SysUser>> findAll() {
        return ApiResponse.ok(sysUserService.findAll());
    }
}
