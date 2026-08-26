package com.example.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OAuth Config Tests")
class OAuthConfigTest {

    @Test
    @DisplayName("Get default client ID when env var not set")
    void testGetDefaultClientId() {
        String clientId = OAuthConfig.getClientId();
        assertNotNull(clientId);
        assertFalse(clientId.isEmpty());
    }

    @Test
    @DisplayName("Get default client secret when env var not set")
    void testGetDefaultClientSecret() {
        String secret = OAuthConfig.getClientSecret();
        assertNotNull(secret);
        assertFalse(secret.isEmpty());
    }

    @Test
    @DisplayName("Get default redirect URI")
    void testGetDefaultRedirectUri() {
        String uri = OAuthConfig.getRedirectUri();
        assertNotNull(uri);
        assertTrue(uri.contains("localhost"));
    }

    @Test
    @DisplayName("Get default authorize URL")
    void testGetDefaultAuthorizeUrl() {
        String url = OAuthConfig.getAuthorizeUrl();
        assertNotNull(url);
        assertTrue(url.contains("google.com"));
    }

    @Test
    @DisplayName("Get default token URL")
    void testGetDefaultTokenUrl() {
        String url = OAuthConfig.getTokenUrl();
        assertNotNull(url);
        assertTrue(url.contains("oauth2.googleapis.com"));
    }

    @Test
    @DisplayName("Get JWT secret")
    void testGetJwtSecret() {
        String secret = OAuthConfig.getJwtSecret();
        assertNotNull(secret);
        assertFalse(secret.isEmpty());
    }

    @Test
    @DisplayName("Configuration defaults are sensible")
    void testConfigurationDefaults() {
        String clientId = OAuthConfig.getClientId();
        String secret = OAuthConfig.getClientSecret();
        String redirectUri = OAuthConfig.getRedirectUri();

        assertNotNull(clientId);
        assertNotNull(secret);
        assertNotNull(redirectUri);
    }
}
