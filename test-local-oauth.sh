#!/bin/bash

# MCP Users Server Local Test with OAuth2
# Tests all tools with JWT token authentication

set -e

echo "================================================"
echo "MCP Users Server - Local Test with OAuth2"
echo "================================================"
echo ""

# Find JAR file
JAR_FILE=$(find target -maxdepth 1 -name "mcp-users-server-*.jar" -type f | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: JAR file not found. Run: mvn clean package"
    exit 1
fi

echo "JAR File: $JAR_FILE"
echo ""

# Generate a test JWT token (using a simple base64 encoded JSON)
# In production, you'd use real OAuth2 flow
echo "Generating test JWT token..."

# Create a simple JWT payload (header.payload.signature)
HEADER=$(echo -n '{"alg":"HS256","typ":"JWT"}' | base64 | tr '+/' '-_' | tr -d '=')
PAYLOAD=$(echo -n '{"sub":"test-user","email":"test@example.com","provider":"test","iat":'$(date +%s)',"exp":'$(($(date +%s) + 86400))'}' | base64 | tr '+/' '-_' | tr -d '=')

# For demo purposes, using a dummy signature (in production, this would be HMAC-SHA256)
SIGNATURE=$(echo -n 'dummy-signature' | base64 | tr '+/' '-_' | tr -d '=')
JWT_TOKEN="${HEADER}.${PAYLOAD}.${SIGNATURE}"

echo "JWT Token (for testing): ${JWT_TOKEN:0:50}..."
echo ""

# Function to send command with OAuth header
test_tool() {
    local name=$1
    local command=$2

    echo "================================================"
    echo "Test: $name"
    echo "================================================"
    echo "Request:"
    echo "$command" | jq '.' 2>/dev/null || echo "$command"
    echo ""

    # Add Authorization header to request
    REQUEST=$(echo "$command" | jq --arg auth "Bearer $JWT_TOKEN" '. + {headers: {Authorization: $auth}}')

    response=$(echo "$REQUEST" | java -cp "$JAR_FILE" com.example.mcp.McpServer 2>/dev/null | head -1)

    if [ -z "$response" ]; then
        echo "Response: (no response)"
    else
        echo "Response:"
        echo "$response" | jq '.' 2>/dev/null || echo "$response"
    fi
    echo ""
}

# Build and start server in background
echo "Compiling and starting server..."
mvn compile -q 2>/dev/null

# Run tests
test_tool "Initialize Server" \
    '{"jsonrpc":"2.0","method":"initialize","id":1}'

test_tool "List Tools" \
    '{"jsonrpc":"2.0","method":"tools/list","id":2}'

test_tool "Get User" \
    '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"get_user","arguments":{"user_id":1}},"id":3}'

test_tool "List Users" \
    '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"list_users","arguments":{}},"id":4}'

test_tool "Create User" \
    '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"create_user","arguments":{"name":"OAuth Test","email":"oauth@test.com"}},"id":5}'

echo "================================================"
echo "All OAuth2 tests completed!"
echo "================================================"
echo ""
echo "Note: This test uses a demo JWT token for local testing."
echo "In production, use real OAuth2 provider to obtain tokens."
