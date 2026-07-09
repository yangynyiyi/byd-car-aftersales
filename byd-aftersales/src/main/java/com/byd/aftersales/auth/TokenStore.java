package com.byd.aftersales.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {

    private final Map<String, AuthUser> tokens = new ConcurrentHashMap<>();

    public String issue(AuthUser user) {
        String token = "token-" + UUID.randomUUID();
        tokens.put(token, user);
        return token;
    }

    public Optional<AuthUser> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokens.get(token));
    }

    public void revoke(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }
}
