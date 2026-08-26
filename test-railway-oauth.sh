#!/bin/bash

# MCP Users Server Railway Test with OAuth2
# Tests WebSocket with JWT token authentication

RAILWAY_URL="wss://claude-ia-mcp-tools-auth-java-staging.up.railway.app"

echo "================================================"
echo "MCP Users Server - Railway OAuth2 Test"
echo "================================================"
echo ""
echo "Target: $RAILWAY_URL"
echo ""

# Check websocat installation
if ! command -v websocat &> /dev/null; then
    echo "Installing websocat..."
    bash install-ci-deps.sh
fi

echo "✅ Websocat ready"
echo ""

# Generate test JWT token
echo "Generating test JWT token..."
HEADER=$(echo -n '{"alg":"HS256","typ":"JWT"}' | base64 | tr '+/' '-_' | tr -d '=')
PAYLOAD=$(echo -n '{"sub":"railway-user","email":"railway@test.com","provider":"test","iat":'$(date +%s)',"exp":'$(($(date +%s) + 86400))'}' | base64 | tr '+/' '-_' | tr -d '=')
SIGNATURE=$(echo -n 'railway-signature' | base64 | tr '+/' '-_' | tr -d '=')
JWT_TOKEN="${HEADER}.${PAYLOAD}.${SIGNATURE}"

echo "JWT Token: ${JWT_TOKEN:0:60}..."
echo ""

# Function to send command with OAuth
test_tool_oauth() {
    local name=$1
    local command=$2

    echo "================================================"
    echo "Test: $name"
    echo "================================================"
    echo "Request:"
    echo "$command" | jq '.' 2>/dev/null || echo "$command"
    echo ""

    # Add Authorization header
    REQUEST=$(echo "$command" | jq --arg auth "Bearer $JWT_TOKEN" '. + {headers: {Authorization: $auth}}')

    response=$(echo "$REQUEST" | websocat "$RAILWAY_URL" 2>&1 | head -1)

    if [ -z "$response" ]; then
        echo "Response: (no response - server may not be running)"
    else
        echo "Response:"
        echo "$response" | jq '.' 2>/dev/null || echo "$response"
    fi
    echo ""
}

echo "Testing OAuth2 with JWT tokens..."
echo ""

# Run tests with OAuth
test_tool_oauth "Initialize with OAuth" \
    '{"jsonrpc":"2.0","method":"initialize","id":1}'

test_tool_oauth "List Tools with OAuth" \
    '{"jsonrpc":"2.0","method":"tools/list","id":2}'

test_tool_oauth "Get User with OAuth" \
    '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"get_user","arguments":{"user_id":1}},"id":3}'

test_tool_oauth "List Users with OAuth" \
    '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"list_users","arguments":{}},"id":4}'

test_tool_oauth "Create User with OAuth" \
    '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"create_user","arguments":{"name":"Railway OAuth User","email":"railway-oauth@test.com"}},"id":5}'

echo "================================================"
echo "OAuth2 Railway tests completed!"
echo "================================================"
echo ""
echo "✅ All requests included JWT Authorization header"
echo "✅ Token format: Bearer <JWT>"
echo ""
echo "To use real OAuth2:"
echo "1. Set environment variables (OAUTH_CLIENT_ID, etc.)"
echo "2. Use OAuthService.exchangeCodeForToken()"
echo "3. Generate JWT with OAuthService.generateJwtToken()"
