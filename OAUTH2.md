# OAuth2 Integration Guide

This MCP server now supports OAuth2 authentication with JWT token validation.

## Architecture

```
┌─────────────────────────────────────┐
│  OAuth2 Flow                        │
├─────────────────────────────────────┤
│  1. User → Authorization URL        │
│  2. OAuth Provider → Auth Code      │
│  3. Server → Exchange Code for Token│
│  4. Server → Generate JWT Token     │
│  5. Client → Include JWT in header  │
│  6. OAuthMiddleware → Validate Token│
└─────────────────────────────────────┘
```

## Components

### OAuthConfig
- Reads OAuth2 settings from environment variables
- Configurable for Google, GitHub, or any OAuth2 provider

### JwtTokenProvider
- Generates JWT tokens using JJWT library
- Validates and extracts claims from tokens
- 24-hour token expiration by default

### OAuthService
- Handles OAuth2 authorization flow
- Exchanges authorization codes for tokens
- Fetches user information from OAuth provider
- Generates JWT tokens for MCP requests

### OAuthMiddleware
- Validates JWT tokens on each MCP request
- Extracts user ID from valid tokens
- Returns 401 Unauthorized for invalid tokens

## Setup

### Environment Variables

```bash
export OAUTH_CLIENT_ID="your_client_id"
export OAUTH_CLIENT_SECRET="your_client_secret"
export OAUTH_REDIRECT_URI="http://localhost:8080/oauth/callback"
export OAUTH_AUTHORIZE_URL="https://accounts.google.com/o/oauth2/v2/auth"
export OAUTH_TOKEN_URL="https://oauth2.googleapis.com/token"
export JWT_SECRET="your_jwt_secret_key"
```

### Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create OAuth 2.0 credentials (Web Application)
3. Add authorized redirect URIs
4. Copy Client ID and Client Secret
5. Set environment variables above

## Usage

### Get Authorization URL

```bash
# MCP Request
{
  "jsonrpc": "2.0",
  "method": "oauth/authorize",
  "params": {
    "state": "unique_state_string"
  },
  "id": 1
}
```

### Exchange Authorization Code

```bash
# After user authorizes, exchange code for JWT
{
  "jsonrpc": "2.0",
  "method": "oauth/callback",
  "params": {
    "code": "auth_code_from_provider"
  },
  "id": 2
}

# Response includes JWT token
{
  "jsonrpc": "2.0",
  "result": {
    "access_token": "eyJhbGc...",
    "token_type": "Bearer",
    "expires_in": 86400
  },
  "id": 2
}
```

### Use JWT Token in Requests

```bash
# Include JWT in Authorization header
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "params": {
    "name": "get_user",
    "arguments": {"user_id": 1}
  },
  "id": 3,
  "headers": {
    "Authorization": "Bearer eyJhbGc..."
  }
}
```

## Security Features

- ✅ JWT tokens with HMAC-SHA256 signature
- ✅ Token expiration (24 hours by default)
- ✅ Middleware validation on every request
- ✅ OAuth2 provider integration
- ✅ User ID extraction from token claims

## Testing

```bash
# Test without authentication (if OAuth is not configured)
bash test-local.sh

# Test with OAuth2 (requires env vars)
bash test-oauth.sh
```

## Optional: Disable OAuth for Development

If `OAUTH_CLIENT_ID` and `OAUTH_CLIENT_SECRET` are not set, OAuth middleware will skip validation (warning: not for production).

```bash
unset OAUTH_CLIENT_ID
unset OAUTH_CLIENT_SECRET
# OAuth middleware will not validate tokens
```

## Production Deployment

1. Set all OAuth environment variables
2. Use HTTPS for all endpoints
3. Use strong JWT_SECRET (min 32 characters)
4. Implement token refresh mechanism
5. Add rate limiting to /oauth/callback
6. Monitor token usage and revocations
