package com.example.auth;

public class OAuthConfig {
    private static final String CLIENT_ID = System.getenv("OAUTH_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("OAUTH_CLIENT_SECRET");
    private static final String REDIRECT_URI = System.getenv("OAUTH_REDIRECT_URI");
    private static final String AUTHORIZE_URL = System.getenv("OAUTH_AUTHORIZE_URL");
    private static final String TOKEN_URL = System.getenv("OAUTH_TOKEN_URL");
    private static final String JWT_SECRET = System.getenv("JWT_SECRET");

    public static String getClientId() {
        return CLIENT_ID != null ? CLIENT_ID : "default_client_id";
    }

    public static String getClientSecret() {
        return CLIENT_SECRET != null ? CLIENT_SECRET : "default_client_secret";
    }

    public static String getRedirectUri() {
        return REDIRECT_URI != null ? REDIRECT_URI : "http://localhost:8080/oauth/callback";
    }

    public static String getAuthorizeUrl() {
        return AUTHORIZE_URL != null ? AUTHORIZE_URL : "https://accounts.google.com/o/oauth2/v2/auth";
    }

    public static String getTokenUrl() {
        return TOKEN_URL != null ? TOKEN_URL : "https://oauth2.googleapis.com/token";
    }

    public static String getJwtSecret() {
        return JWT_SECRET != null ? JWT_SECRET : "default_jwt_secret_key_change_in_production";
    }

    public static boolean isConfigured() {
        return CLIENT_ID != null && CLIENT_SECRET != null;
    }
}
