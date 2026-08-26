#!/bin/bash

# MCP Users Server Local Test Script
# Tests all 5 tools of the MCP server

set -e

echo "================================================"
echo "MCP Users Server - Local Test"
echo "================================================"
echo ""

# Find JAR file (prefer main target dir, not temp archives)
JAR_FILE=$(find target -maxdepth 1 -name "mcp-users-server-*.jar" -type f | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: JAR file not found in target directory."
    echo "Run: mvn clean package -DskipTests"
    exit 1
fi

echo "JAR File: $JAR_FILE"
echo "Size: $(du -h "$JAR_FILE" | cut -f1)"
echo ""

# Create temp file for MCP communication
TEMP_FILE=$(mktemp)
trap "rm -f $TEMP_FILE" EXIT

# Function to send command and get response
test_tool() {
    local name=$1
    local command=$2

    echo "================================================"
    echo "Test: $name"
    echo "================================================"
    echo "Request:"
    echo "$command" | jq '.' 2>/dev/null || echo "$command"
    echo ""

    # Send command to MCP server and capture response
    response=$(echo -e "$command" | timeout 5 java -jar "$JAR_FILE" 2>&1 | grep '^\{' | head -1)

    if [ -z "$response" ]; then
        echo "Response: (no response received)"
    else
        echo "Response:"
        echo "$response" | jq '.' 2>/dev/null || echo "$response"
    fi
    echo ""
}

# Test 1: Initialize
test_tool "Initialize Server" \
    '{"jsonrpc":"2.0","method":"initialize","id":1}'

# Test 2: List Tools
test_tool "List Tools" \
    '{"jsonrpc":"2.0","method":"tools/list","id":2}'

# Test 3: Get User
test_tool "Get User (ID: 1)" \
    '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"get_user","arguments":{"user_id":1}},"id":3}'

# Test 4: List Users
test_tool "List Users" \
    '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"list_users","arguments":{}},"id":4}'

# Test 5: Create User
test_tool "Create User (Test User)" \
    '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"create_user","arguments":{"name":"Test User","email":"test@example.com"}},"id":5}'

# Test 6: Update User
test_tool "Update User (ID: 1)" \
    '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"update_user","arguments":{"user_id":1,"name":"Updated User"}},"id":6}'

# Test 7: Delete User
test_tool "Delete User (ID: 1)" \
    '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"delete_user","arguments":{"user_id":1}},"id":7}'

echo "================================================"
echo "All tests completed!"
echo "================================================"
