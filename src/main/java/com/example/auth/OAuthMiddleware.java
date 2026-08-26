package com.example.auth;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthMiddleware {
    private static final Logger logger = LoggerFactory.getLogger(OAuthMiddleware.class);
    private final OAuthService oauthService;

    public OAuthMiddleware() {
        this.oauthService = new OAuthService();
    }

    public boolean validateRequest(JsonObject request) {
        String authHeader = extractAuthHeader(request);
        if (authHeader == null) {
            logger.debug("No authorization header found");
            return !OAuthConfig.isConfigured();
        }

        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Invalid authorization header format");
            return false;
        }

        String token = authHeader.substring(7);
        boolean isValid = oauthService.validateJwtToken(token);

        if (isValid) {
            String userId = oauthService.getUserIdFromJwt(token);
            logger.debug("Request validated for user: {}", userId);
        } else {
            logger.warn("Invalid or expired token");
        }

        return isValid;
    }

    public String extractUserId(JsonObject request) {
        String authHeader = extractAuthHeader(request);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        return oauthService.getUserIdFromJwt(token);
    }

    private String extractAuthHeader(JsonObject request) {
        if (request.has("headers") && request.get("headers").isJsonObject()) {
            JsonObject headers = request.getAsJsonObject("headers");
            if (headers.has("Authorization")) {
                return headers.get("Authorization").getAsString();
            }
        }
        return null;
    }

    public JsonObject createAuthErrorResponse(int id) {
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        JsonObject error = new JsonObject();
        error.addProperty("code", -32001);
        error.addProperty("message", "Unauthorized: Invalid or missing token");
        response.add("error", error);
        response.addProperty("id", id);
        return response;
    }

    public JsonObject createOAuthResponse(String code, String state) {
        JsonObject response = new JsonObject();
        response.addProperty("code", code);
        response.addProperty("state", state);
        return response;
    }
}
