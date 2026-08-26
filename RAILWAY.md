# Railway Deployment Guide

## MCP Server on Railway

This project is configured to deploy the MCP (Model Context Protocol) server to Railway.

### Architecture

**Three communication modes:**

| Mode | Protocol | Use Case |
|------|----------|----------|
| Local | stdio | Running JAR locally with `test-local.sh` |
| Railway WebSocket | WebSocket (wss://) | Remote MCP clients via `test-railway.sh` |
| Health Checks | HTTP | Railway's internal health monitoring |

### How Railway Works

1. **Port Allocation**: Railway provides a `PORT` environment variable
2. **Health Checks**: Railway sends HTTP GET requests to `/health` to verify the app is running
3. **Exposure**: Railway exposes the application via HTTPS at a `.up.railway.app` domain

### Current Implementation

**McpWebSocketServer** (main server):
- Listens on `${PORT}` (default: 8080)
- Accepts WebSocket connections for MCP protocol
- Starts HTTP health check server on `${PORT}+1`

**Procfile**:
```bash
web: java -cp target/mcp-users-server-*.jar com.example.mcp.McpWebSocketServer ${PORT:-8080}
```

### Testing the Deployment

**Option 1: Check logs in Railway**
```bash
railway logs
```

Look for:
- `MCP WebSocket Server started on port X`
- `HTTP Health Check Server listening on port Y`

**Option 2: Test locally**
```bash
# Build
mvn clean package -DskipTests

# Run WebSocket server
java -cp target/mcp-users-server-*.jar com.example.mcp.McpWebSocketServer 8080

# In another terminal, test with websocat
bash test-railway.sh
```

**Option 3: Connect to Railroad instance**
```bash
# Install websocat
brew install websocat  # macOS
# or
cargo install websocat  # Linux/WSL

# Test connection
echo '{"jsonrpc":"2.0","method":"initialize","id":1}' | \
  websocat wss://claude-ia-mcp-tools-java-staging.up.railway.app
```

### Validation

#### Option 1: Automatic Post-Deploy Validation (Recommended)

GitHub Actions automatically validates after each deployment:

1. **Trigger**: After successful Railway deployment
2. **What it does**:
   - Waits 60 seconds for server to start
   - Runs `validate-deployment.sh` (checks infrastructure)
   - Runs `test-railway.sh` (tests all MCP commands)
3. **View results**: GitHub → Actions → "Post-Deploy Validation"

**Workflow file:** `.github/workflows/post-deploy-validation.yml`

#### Option 2: Manual Validation

Run the validation script:
```bash
bash validate-deployment.sh https://claude-ia-mcp-tools-java-staging.up.railway.app
```

This checks:
- HTTP connectivity
- WebSocket upgrade capability
- Server responsiveness

Then test full MCP functionality:
```bash
bash test-railway.sh
```

This validates:
- initialize command
- tools/list command
- get_user, list_users, create_user, update_user, delete_user commands
- JSON-RPC protocol compliance

#### Option 3: View Deployment Logs

```bash
railway logs
```

Look for:
- `MCP WebSocket Server started on port X`
- `HTTP Health Check Server listening on port Y`
- No ERROR messages

### Troubleshooting

**Problem: Application keeps crashing**
- Check `railway logs` for errors
- Verify Java process is listening on PORT
- Ensure dependencies are included in JAR

**Problem: WebSocket connection refused**
- Application may still be starting
- Check if HTTP health checks are passing
- Verify firewall/security settings allow WebSocket

**Problem: Timeout connecting to server**
- Give app time to start (may take 30+ seconds)
- Check if Railway deployment is still in progress
- Verify the domain name is correct

### Performance Notes

- WebSocket connections are persistent and efficient for MCP
- HTTP health checks are lightweight (sent every ~10 seconds)
- Multiple concurrent WebSocket connections supported

### Customizing Post-Deploy Validation

**Edit the workflow** (`.github/workflows/post-deploy-validation.yml`):

Change Railway URL:
```yaml
- name: Run deployment validation
  run: |
    bash validate-deployment.sh https://YOUR-URL.up.railway.app
```

Adjust startup wait time:
```yaml
- name: Wait for Railway deployment (60s)
  run: sleep 120  # Change from 60 to 120 seconds
```

Add custom validations:
```yaml
- name: Custom health check
  run: |
    curl -s https://YOUR-URL.up.railway.app/health | jq .
```

For more details, see [POST_DEPLOY_VALIDATION.md](POST_DEPLOY_VALIDATION.md).

### Future Improvements

1. **HTTP fallback**: Add HTTP endpoint for tools in addition to WebSocket
2. **Metrics**: Add Prometheus endpoints for monitoring
3. **Scaling**: Use connection pooling for API client
4. **Security**: Add authentication/authorization layer
