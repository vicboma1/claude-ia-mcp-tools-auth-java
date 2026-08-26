# MCP Users Server with OAuth2 (Java)

MCP (Model Context Protocol) server for user management with OAuth2 authentication and JWT token validation. Clean layered architecture with API client, business logic, authentication, and MCP protocol handlers. Supports both local (stdio) and remote (WebSocket) deployments with secure token-based access control.

## Architecture

### Layered Design with OAuth2

```
┌─────────────────────────────────────┐
│  Transport Layer                    │
├─────────────────┬───────────────────┤
│ McpServer       │ McpWebSocketServer│
│ (stdio)         │ (WebSocket)       │
└────────┬────────┴────────┬──────────┘
         │                 │
┌────────▼─────────────────▼──────────┐
│  OAuth2 Authentication Layer        │
│  OAuthMiddleware, JwtTokenProvider  │
│  Token validation on every request  │
└────────┬──────────────────────────┬─┘
         │                          │
┌────────▼─────────────────▼──────────┐
│  MCP Protocol Handler               │
│  ToolRegistry, request validation   │
└────────┬──────────────────────────┬─┘
         │                          │
┌────────▼──────────────────────────▼┐
│  Business Logic Layer               │
│  UserService (reusable)             │
└────────┬──────────────────────────┬─┘
         │                          │
┌────────▼──────────────────────────▼┐
│  API Client Layer                   │
│  ApiClient (HTTP, no logic)         │
└─────────────────────────────────────┘
```

### Code Structure

```
src/main/java/com/example/
├── api/
│   └── ApiClient.java           // HTTP client (no business logic)
├── auth/                        // OAuth2 Authentication
│   ├── OAuthConfig.java         // Configuration from env vars
│   ├── OAuthService.java        // OAuth2 authorization flow
│   ├── OAuthToken.java          // Token model
│   ├── OAuthMiddleware.java     // Request validation middleware
│   └── JwtTokenProvider.java    // JWT generation and validation
├── business/
│   └── UserService.java         // Validation, normalization, business rules
├── mcp/
│   ├── McpServer.java           // JSON-RPC protocol handler (stdio)
│   ├── McpWebSocketServer.java  // WebSocket server for remote (Railway)
│   └── ToolRegistry.java        // Tool definitions and schemas
```

## OAuth2 Authentication

All MCP tools are protected with OAuth2 authentication using JWT tokens.

### Quick Setup

1. Configure OAuth provider (Google, GitHub, etc.):
```bash
export OAUTH_CLIENT_ID="your_client_id"
export OAUTH_CLIENT_SECRET="your_client_secret"
export JWT_SECRET="your_jwt_secret_key"
```

2. Get authorization URL and exchange code for token
3. Include JWT token in Authorization header for all requests

See [OAUTH2.md](OAUTH2.md) for complete setup and usage guide.

### Token Usage

Include JWT in Authorization header:
```bash
curl -H "Authorization: Bearer <jwt_token>" \
  ws://localhost:8080
```

## Tools Exposed

All tools require valid OAuth2 JWT token:

1. **get_user** - Get one user by ID (requires: user_id)
2. **list_users** - List all users (no parameters)
3. **create_user** - Create a new user (requires: name, email)
4. **update_user** - Update user name/email (requires: user_id; optional: name, email)
5. **delete_user** - Delete a user (requires: user_id)

## Requirements

- Java 11+
- Maven 3.6+

## Setup & Build

```bash
# Install dependencies and build
mvn clean package -DskipTests

# Run all tests
mvn test

# With coverage report
mvn test jacoco:report
```

## Deployment Modes

### Local Deployment (stdio)

Use this for Claude Desktop or local testing:

```bash
java -cp target/mcp-users-server-*.jar com.example.mcp.McpServer
```

Or with Maven:

```bash
mvn exec:java -Dexec.mainClass="com.example.mcp.McpServer"
```

Claude Desktop configuration:

```json
{
  "mcpServers": {
    "users": {
      "command": "java",
      "args": ["-cp", "path/to/mcp-users-server-1.0.0.jar", "com.example.mcp.McpServer"]
    }
  }
}
```

### Remote Deployment (WebSocket)

Use this for Railway or other cloud platforms:

```bash
java -jar target/mcp-users-server-*.jar
```

Server will:
- Listen on WebSocket at port (default: 8080)
- Listen on HTTP health checks at port+1 (default: 8081)
- Log startup information with endpoint details

For Railway deployment, see [RAILWAY.md](RAILWAY.md) for full configuration.

## Testing

### Unit Tests (JUnit 5)

```bash
# Run all unit tests (188+ tests)
mvn test

# Run with coverage report
mvn test jacoco:report
open target/site/jacoco/index.html
```

### Local Integration Tests (stdio)

Test MCP server locally without OAuth2:
```bash
bash test-local.sh
```

Shows 7 tests:
- Initialize Server
- List Tools
- Get User
- List Users  
- Create User
- Update User
- Delete User

### Local OAuth2 Tests (stdio)

Test MCP server locally with JWT token generation:
```bash
bash test-local-oauth.sh
```

Features:
- ✅ JWT token generation (HS256)
- ✅ Full JSON-RPC response validation
- ✅ OAuth2 implementation verification
- ✅ 4 core tests with token context

### Remote Tests (Railway WebSocket)

Test deployed server on Railway:
```bash
bash test-railway.sh
```

Features:
- ✅ Auto-installs websocat (multiple platform support)
- ✅ WebSocket connection validation
- ✅ 7 MCP tool tests
- ✅ Full JSON-RPC response validation
- ✅ Timeout protection (10s per request)

### Remote OAuth2 Tests (Railway WebSocket)

Test deployed server with OAuth2:
```bash
bash test-railway-oauth.sh
```

Features:
- ✅ JWT token generation for Railway
- ✅ OAuth2 flow validation
- ✅ 7 tests with clean JSON-RPC
- ✅ No malformed header injection
- ✅ Full response validation

**Automatic Validation (Recommended)**
- GitHub Actions automatically validates after each successful Railway deployment
- Executes: `install-ci-deps.sh` → `validate-deployment.sh` → `test-railway.sh`
- View results: GitHub → Actions → "Post-Deploy Validation"

See [POST_DEPLOY_VALIDATION.md](POST_DEPLOY_VALIDATION.md) and [install-ci-deps.sh](install-ci-deps.sh) for details.

### Manual Testing

```bash
# stdio mode
echo '{"jsonrpc":"2.0","method":"initialize","id":1}' | \
  java -cp target/mcp-users-server-*.jar com.example.mcp.McpServer

# WebSocket mode (requires websocat)
echo '{"jsonrpc":"2.0","method":"initialize","id":1}' | \
  websocat ws://localhost:8080
```

### Response Validation

All test scripts validate responses with:
- ✅ Full JSON-RPC format validation
- ✅ jq parsing verification
- ✅ Timeout protection
- ✅ Error reporting with raw response on failures

## Test Coverage

- 139 test cases total
- 70+ test cases for business logic
- 40+ corner cases (empty strings, invalid emails, boundary IDs)
- Mocked API client for unit tests
- Coverage: statement, branch, and method coverage tracked

Run coverage report:

```bash
mvn test jacoco:report
open target/site/jacoco/index.html
```

## Dependencies

### Core
- **OkHttp 4.11.0** - Resilient HTTP client with 10-second timeouts
- **Gson 2.10.1** - JSON serialization/deserialization
- **Java-WebSocket 1.5.4** - WebSocket server for remote deployment

### Authentication & Security
- **JJWT 0.11.5** - JWT token generation and validation (HS256)
- **Commons Codec 1.16.0** - Security utilities
- **H2 Database 2.2.224** - In-memory token storage
- **HikariCP 5.1.0** - Connection pooling

### Logging
- **SLF4J 2.0.7** - Logging facade
- **Logback 1.4.8** - Structured logging to stderr

### Testing
- **JUnit 5.10.0** - Testing framework
- **Mockito 5.3.1** - Mocking for unit tests

### CLI & Deployment
- **websocat 1.12.0** - WebSocket client for testing
  - Auto-installed by `install-ci-deps.sh`
  - Supports: Linux, macOS, Windows (Git Bash/Cygwin)

## CI/CD Pipeline

GitHub Actions workflows for:
- **CI** (ci.yml) - Build, test, coverage reporting
- **Lint** (lint.yml) - Code quality and security scanning
- **Release** (release.yml) - Automated releases on git tags
- **Deploy** (deploy.yml) - Automatic deployment to Railway
- **Post-Deploy Validation** (post-deploy-validation.yml) - Automatic validation after successful deploy
- **Auto-merge** (auto-merge-dependabot.yml) - Dependency updates

### Post-Deploy Validation

After each successful Railway deployment, GitHub Actions automatically:
1. Waits 60 seconds for server startup
2. Runs `validate-deployment.sh` (infrastructure checks)
3. Runs `test-railway.sh` (MCP functional tests)
4. Reports results in Actions tab

View validation logs: GitHub → Actions → "Post-Deploy Validation"

**Robust dependency installation:**
- Script: `install-ci-deps.sh`
- Installs: jq, websocat
- Fallback chain: apt-get → cargo → precompiled binary
- Handles CI environments reliably

## Recent Fixes (v1.0.0)

### JSON-RPC Response Validation
- ✅ Fixed `EOFException: End of input at line 2 column 1` in test scripts
- ✅ Removed `head -1` truncation that was breaking WebSocket responses
- ✅ Added full response capture with timeout protection (10s)
- ✅ Implemented proper jq validation for all responses

### OAuth2 Test Scripts
- ✅ Fixed malformed header injection in `test-railway-oauth.sh`
- ✅ Removed corrupted JSON-RPC format from `'. + {headers: {Authorization: $auth}}'`
- ✅ WebSocket sends clean JSON-RPC commands only (no HTTP headers in body)
- ✅ Both test-railway.sh and test-railway-oauth.sh now working correctly

### JJWT Compatibility
- ✅ Downgraded from 0.12.3 to 0.11.5 for API compatibility
- ✅ Changed `parserBuilder()` to `parser()` API
- ✅ JWT token generation and validation working correctly

## Documentation

- [OAUTH2.md](OAUTH2.md) - OAuth2 authentication setup and usage
- [SETUP.md](SETUP.md) - Local development setup
- [TESTS.md](TESTS.md) - Detailed test documentation
- [CI_CD.md](CI_CD.md) - CI/CD pipeline guide
- [RAILWAY.md](RAILWAY.md) - Railway deployment guide
- [POST_DEPLOY_VALIDATION.md](POST_DEPLOY_VALIDATION.md) - Post-deployment validation
