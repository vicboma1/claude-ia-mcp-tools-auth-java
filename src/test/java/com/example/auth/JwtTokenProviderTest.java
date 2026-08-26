package com.example.auth;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Token Provider Tests")
class JwtTokenProviderTest {
    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
    }

    @Test
    @DisplayName("Generate valid JWT token")
    void testGenerateToken() {
        String token = provider.generateToken("user123", "user@example.com", "google");
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    @DisplayName("Validate valid JWT token")
    void testValidateValidToken() {
        String token = provider.generateToken("user123", "user@example.com", "google");
        assertTrue(provider.validateToken(token));
    }

    @Test
    @DisplayName("Extract userId from valid token")
    void testGetUserIdFromToken() {
        String userId = "user123";
        String token = provider.generateToken(userId, "user@example.com", "google");
        assertEquals(userId, provider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("Extract claims from token")
    void testGetClaimsFromToken() {
        String token = provider.generateToken("user123", "user@example.com", "google");
        Claims claims = provider.getClaimsFromToken(token);
        assertNotNull(claims);
        assertEquals("google", claims.get("provider"));
        assertEquals("user@example.com", claims.get("email"));
    }

    @Test
    @DisplayName("Invalid token returns false on validation")
    void testValidateInvalidToken() {
        assertFalse(provider.validateToken("invalid.token.here"));
    }

    @Test
    @DisplayName("getUserIdFromToken returns null for invalid token")
    void testGetUserIdFromInvalidToken() {
        assertNull(provider.getUserIdFromToken("invalid.token.here"));
    }

    @Test
    @DisplayName("Token contains correct subject")
    void testTokenSubject() {
        String userId = "user456";
        String token = provider.generateToken(userId, "test@test.com", "github");
        Claims claims = provider.getClaimsFromToken(token);
        assertEquals(userId, claims.getSubject());
    }

    @Test
    @DisplayName("Token has expiration date")
    void testTokenExpiration() {
        String token = provider.generateToken("user123", "user@example.com", "google");
        Claims claims = provider.getClaimsFromToken(token);
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis());
    }
}
