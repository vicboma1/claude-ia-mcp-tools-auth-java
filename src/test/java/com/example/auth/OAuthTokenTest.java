package com.example.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OAuth Token Tests")
class OAuthTokenTest {
    private OAuthToken token;

    @BeforeEach
    void setUp() {
        token = new OAuthToken("user123", "access_token_123", "refresh_token_456", "google", "user@example.com");
    }

    @Test
    @DisplayName("Create OAuth token with valid data")
    void testCreateToken() {
        assertNotNull(token);
        assertEquals("user123", token.getUserId());
        assertEquals("access_token_123", token.getAccessToken());
        assertEquals("refresh_token_456", token.getRefreshToken());
        assertEquals("google", token.getProvider());
        assertEquals("user@example.com", token.getEmail());
        assertTrue(token.isActive());
    }

    @Test
    @DisplayName("Token has creation timestamp")
    void testTokenCreationTimestamp() {
        assertNotNull(token.getCreatedAt());
        assertTrue(token.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Token has expiration timestamp")
    void testTokenExpirationTimestamp() {
        assertNotNull(token.getExpiresAt());
        assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Fresh token is not expired")
    void testFreshTokenNotExpired() {
        assertFalse(token.isExpired());
    }

    @Test
    @DisplayName("Token is active by default")
    void testTokenActiveByDefault() {
        assertTrue(token.isActive());
    }

    @Test
    @DisplayName("Can revoke token")
    void testRevokeToken() {
        token.setActive(false);
        assertFalse(token.isActive());
    }

    @Test
    @DisplayName("Can set and get token ID")
    void testTokenId() {
        token.setId("token-id-123");
        assertEquals("token-id-123", token.getId());
    }

    @Test
    @DisplayName("Can update token values")
    void testUpdateTokenValues() {
        token.setAccessToken("new_access_token");
        token.setRefreshToken("new_refresh_token");
        token.setEmail("new@example.com");

        assertEquals("new_access_token", token.getAccessToken());
        assertEquals("new_refresh_token", token.getRefreshToken());
        assertEquals("new@example.com", token.getEmail());
    }

    @Test
    @DisplayName("Expired token detection")
    void testExpiredTokenDetection() {
        token.setExpiresAt(LocalDateTime.now().minusHours(1));
        assertTrue(token.isExpired());
    }

    @Test
    @DisplayName("Token serialization")
    void testTokenSerialization() {
        OAuthToken newToken = new OAuthToken();
        newToken.setId(token.getId());
        newToken.setUserId(token.getUserId());
        newToken.setAccessToken(token.getAccessToken());

        assertEquals(token.getUserId(), newToken.getUserId());
        assertEquals(token.getAccessToken(), newToken.getAccessToken());
    }
}
