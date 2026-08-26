package com.example.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OAuthService {
    private static final Logger logger = LoggerFactory.getLogger(OAuthService.class);
    private final OkHttpClient httpClient;
    private final JwtTokenProvider jwtProvider;
    private final Gson gson = new Gson();

    public OAuthService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        this.jwtProvider = new JwtTokenProvider();
    }

    public String getAuthorizationUrl(String state) {
        return OAuthConfig.getAuthorizeUrl() + "?" +
                "client_id=" + OAuthConfig.getClientId() +
                "&redirect_uri=" + OAuthConfig.getRedirectUri() +
                "&response_type=code" +
                "&scope=openid%20profile%20email" +
                "&state=" + state;
    }

    public OAuthToken exchangeCodeForToken(String code) {
        try {
            FormBody formBody = new FormBody.Builder()
                    .add("client_id", OAuthConfig.getClientId())
                    .add("client_secret", OAuthConfig.getClientSecret())
                    .add("code", code)
                    .add("grant_type", "authorization_code")
                    .add("redirect_uri", OAuthConfig.getRedirectUri())
                    .build();

            Request request = new Request.Builder()
                    .url(OAuthConfig.getTokenUrl())
                    .post(formBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Failed to exchange code for token: {}", response.message());
                    return null;
                }

                String responseBody = response.body().string();
                JsonObject tokenResponse = gson.fromJson(responseBody, JsonObject.class);

                String accessToken = tokenResponse.get("access_token").getAsString();
                String refreshToken = tokenResponse.has("refresh_token") ?
                        tokenResponse.get("refresh_token").getAsString() : null;

                String userInfo = getUserInfo(accessToken);
                if (userInfo == null) return null;

                JsonObject userJson = gson.fromJson(userInfo, JsonObject.class);
                String userId = userJson.get("sub").getAsString();
                String email = userJson.get("email").getAsString();

                OAuthToken oauthToken = new OAuthToken(userId, accessToken, refreshToken, "google", email);
                logger.info("Successfully exchanged code for token for user: {}", email);
                return oauthToken;
            }
        } catch (IOException e) {
            logger.error("Error exchanging code for token: {}", e.getMessage(), e);
            return null;
        }
    }

    public String getUserInfo(String accessToken) {
        try {
            Request request = new Request.Builder()
                    .url("https://openidconnect.googleapis.com/v1/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Failed to get user info: {}", response.message());
                    return null;
                }
                return response.body().string();
            }
        } catch (IOException e) {
            logger.error("Error getting user info: {}", e.getMessage(), e);
            return null;
        }
    }

    public String generateJwtToken(OAuthToken oauthToken) {
        return jwtProvider.generateToken(oauthToken.getUserId(), oauthToken.getEmail(), oauthToken.getProvider());
    }

    public boolean validateJwtToken(String token) {
        return jwtProvider.validateToken(token);
    }

    public String getUserIdFromJwt(String token) {
        return jwtProvider.getUserIdFromToken(token);
    }
}
