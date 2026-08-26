#!/bin/bash

# Validate MCP Deployment Configuration

RAILWAY_URL="${1:-https://claude-ia-mcp-tools-java-staging.up.railway.app}"

echo "================================================"
echo "MCP Deployment Configuration Validator"
echo "================================================"
echo ""
echo "Target: $RAILWAY_URL"
echo ""

# Function to test endpoint
test_endpoint() {
    local url=$1
    local method=$2
    local type=$3

    echo "Testing $type..."
    echo "URL: $url"

    response=$(curl -s -i -X "$method" "$url" 2>&1 | head -20)

    echo "$response"
    echo ""
}

echo "1. Testing HTTP GET (health check)"
echo "   Railway expects HTTP listener on PORT"
test_endpoint "$RAILWAY_URL" "GET" "HTTP GET"

echo "2. Testing HTTP POST with JSON-RPC"
echo "   MCP over HTTP (if supported)"
test_endpoint "$RAILWAY_URL" "POST" "HTTP POST"

echo "3. Testing WebSocket upgrade"
echo "   Checking for WebSocket support"
curl -s -i -N \
  -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  -H "Sec-WebSocket-Key: SGVsbG8sIHdvcmxkIQ==" \
  -H "Sec-WebSocket-Version: 13" \
  "$RAILWAY_URL" 2>&1 | head -15
echo ""

echo "4. Checking server logs"
echo "   Run: railway logs"
echo ""

echo "================================================"
echo "Validation Summary"
echo "================================================"
echo ""
echo "Railroad uses HTTPS and expects:"
echo "  ✓ Server listening on PORT environment variable"
echo "  ✓ HTTP listener for health checks"
echo "  ✓ Can use WebSocket if HTTP listener is present"
echo ""
echo "Current configuration:"
echo "  - Procfile: web: java -cp ... McpWebSocketServer \${PORT:-8080}"
echo "  - Protocol: WebSocket (Java-WebSocket library)"
echo "  - Port: Dynamic (from \$PORT env var)"
echo ""
echo "To check actual deployment:"
echo "  1. Run: railway logs"
echo "  2. Look for 'MCP WebSocket Server' startup message"
echo "  3. If port is not listening, Railway will fail to deploy"
echo ""
