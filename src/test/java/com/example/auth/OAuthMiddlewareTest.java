package com.example.auth;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OAuth Middleware Tests")
class OAuthMiddlewareTest {
    private OAuthMiddleware middleware;
    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        middleware = new OAuthMiddleware();
        tokenProvider = new JwtTokenProvider();
    }

    @Test
    @DisplayName("Validate request with valid JWT token")
    void testValidateRequestWithValidToken() {
        String token = tokenProvider.generateToken("user123", "user@example.com", "google");
        JsonObject request = createRequestWithAuth(token);
        assertTrue(middleware.validateRequest(request));
    }

    @Test
    @DisplayName("Validate request with invalid JWT token")
    void testValidateRequestWithInvalidToken() {
        JsonObject request = createRequestWithAuth("invalid.token.here");
        assertFalse(middleware.validateRequest(request));
    }

    @Test
    @DisplayName("Extract userId from valid token")
    void testExtractUserIdFromValidToken() {
        String userId = "user123";
        String token = tokenProvider.generateToken(userId, "user@example.com", "google");
        JsonObject request = createRequestWithAuth(token);
        assertEquals(userId, middleware.extractUserId(request));
    }

    @Test
    @DisplayName("Extract userId returns null for invalid token")
    void testExtractUserIdFromInvalidToken() {
        JsonObject request = createRequestWithAuth("invalid.token");
        assertNull(middleware.extractUserId(request));
    }

    @Test
    @DisplayName("Extract userId returns null for request without auth header")
    void testExtractUserIdWithoutAuthHeader() {
        JsonObject request = new JsonObject();
        assertNull(middleware.extractUserId(request));
    }

    @Test
    @DisplayName("Create auth error response")
    void testCreateAuthErrorResponse() {
        JsonObject response = middleware.createAuthErrorResponse(1);
        assertTrue(response.has("error"));
        assertEquals(-32001, response.getAsJsonObject("error").get("code").getAsInt());
        assertTrue(response.getAsJsonObject("error").get("message").getAsString().contains("Unauthorized"));
    }

    @Test
    @DisplayName("Validate request without auth header when OAuth is not configured")
    void testValidateRequestWithoutAuthHeader() {
        JsonObject request = new JsonObject();
        boolean result = middleware.validateRequest(request);
        assertTrue(result || false);
    }

    private JsonObject createRequestWithAuth(String token) {
        JsonObject request = new JsonObject();
        JsonObject headers = new JsonObject();
        headers.addProperty("Authorization", "Bearer " + token);
        request.add("headers", headers);
        return request;
    }
}
