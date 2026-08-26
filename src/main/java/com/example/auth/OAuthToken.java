package com.example.auth;

import java.io.Serializable;
import java.time.LocalDateTime;

public class OAuthToken implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String accessToken;
    private String refreshToken;
    private String provider;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean active;

    public OAuthToken() {
    }

    public OAuthToken(String userId, String accessToken, String refreshToken, String provider, String email) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.provider = provider;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24);
        this.active = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
}
