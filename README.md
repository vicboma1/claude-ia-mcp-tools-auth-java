# MCP Users Server (Java)

MCP (Model Context Protocol) server for user management with clean separation of concerns: API client layer, business logic, and MCP protocol handlers. Supports both local (stdio) and remote (WebSocket) deployments.

## Architecture

### Layered Design

```
┌─────────────────────────────────────┐
│  Transport Layer                    │
├─────────────────┬───────────────────┤
│ McpServer       │ McpWebSocketServer│
│ (stdio)         │ (WebSocket)       │
└────────┬────────┴────────┬──────────┘
         │                 │
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
├── business/
│   └── UserService.java         // Validation, normalization, business rules
├── mcp/
│   ├── McpServer.java           // JSON-RPC protocol handler (stdio)
│   ├── McpWebSocketServer.java  // WebSocket server for remote (Railway)
│   └── ToolRegistry.java        // Tool definitions and schemas
```

## Tools Exposed

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

### Local Tests

```bash
# Run unit tests
mvn test

# Run integration tests with test script
bash test-local.sh
```

### Remote Tests (Railway)

Manual testing:
```bash
# Requires websocat: brew install websocat (or uses install-ci-deps.sh)
bash test-railway.sh
```

**Automatic Validation (Recommended)**
- GitHub Actions automatically validates after each successful Railway deployment
- Executes: `install-ci-deps.sh` → `validate-deployment.sh` → `test-railway.sh`
- View results: GitHub → Actions → "Post-Deploy Validation"

See [POST_DEPLOY_VALIDATION.md](POST_DEPLOY_VALIDATION.md) and [install-ci-deps.sh](install-ci-deps.sh) for details.

### Manual Testing

```bash
# stdio
echo '{"jsonrpc":"2.0","method":"initialize","id":1}' | \
  java -cp target/mcp-users-server-*.jar com.example.mcp.McpServer

# WebSocket
echo '{"jsonrpc":"2.0","method":"initialize","id":1}' | \
  websocat ws://localhost:8080
```

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

- **OkHttp 4.11.0** - Resilient HTTP client with timeouts
- **Gson 2.10.1** - JSON serialization/deserialization
- **Java-WebSocket 1.5.4** - WebSocket server for remote deployment
- **SLF4J 2.0.7 + Logback 1.4.8** - Structured logging to stderr
- **JUnit 5.10.0** - Testing framework
- **Mockito 5.3.1** - Mocking for unit tests

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

## Documentation

- [SETUP.md](SETUP.md) - Local development setup
- [TESTS.md](TESTS.md) - Detailed test documentation
- [CI_CD.md](CI_CD.md) - CI/CD pipeline guide
- [RAILWAY.md](RAILWAY.md) - Railway deployment guide
