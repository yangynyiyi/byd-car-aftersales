package com.byd.aftersales.auth;

public class AuthUser {

    private final Long userId;
    private final String username;
    private final String realName;
    private final String role;
    private final Long ownerId;

    public AuthUser(Long userId, String username, String realName, String role, Long ownerId) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.role = role;
        this.ownerId = ownerId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRealName() {
        return realName;
    }

    public String getRole() {
        return role;
    }

    public Long getOwnerId() {
        return ownerId;
    }
}
