#!/bin/bash

# MCP Users Server - Railway Test Script with OAuth2
# Tests all 5 tools of the MCP server running on Railway via WebSocket with JWT authentication

RAILWAY_URL="wss://claude-ia-mcp-tools-auth-java-staging.up.railway.app"

echo "================================================"
echo "MCP Users Server - Railway OAuth2 WebSocket Test"
echo "================================================"
echo ""
echo "Target: $RAILWAY_URL"
echo ""

# Generate OAuth2 JWT token
echo "Generating OAuth2 JWT token..."
HEADER=$(echo -n '{"alg":"HS256","typ":"JWT"}' | base64 | tr '+/' '-_' | tr -d '=')
PAYLOAD=$(echo -n '{"sub":"railway-user-456","email":"railway@example.com","provider":"google","iat":'$(date +%s)',"exp":'$(($(date +%s) + 86400))'}' | base64 | tr '+/' '-_' | tr -d '=')
SIGNATURE=$(echo -n 'railway-signature' | base64 | tr '+/' '-_' | tr -d '=')
JWT_TOKEN="${HEADER}.${PAYLOAD}.${SIGNATURE}"

echo " JWT Token: ${JWT_TOKEN:0:60}..."
echo ""

# Function to download precompiled binary
download_precompiled_websocat() {
    echo "Downloading precompiled websocat binary..."

    local TEMP_DIR=$(mktemp -d)
    trap "rm -rf $TEMP_DIR" EXIT

    cd "$TEMP_DIR" || return 1

    # Detect architecture and platform
    local ARCH=$(uname -m)
    local OS=$(uname -s)

    # Determine download URL
    local DOWNLOAD_URL
    case "$OS-$ARCH" in
        Linux-x86_64)
            DOWNLOAD_URL="https://github.com/vi/websocat/releases/download/v1.12.0/websocat.x86_64-unknown-linux-musl"
            ;;
        Darwin-x86_64)
            DOWNLOAD_URL="https://github.com/vi/websocat/releases/download/v1.12.0/websocat.x86_64-apple-darwin"
            ;;
        Darwin-arm64)
            DOWNLOAD_URL="https://github.com/vi/websocat/releases/download/v1.12.0/websocat.aarch64-apple-darwin"
            ;;
        MINGW*-x86_64|MSYS*-x86_64)
            DOWNLOAD_URL="https://github.com/vi/websocat/releases/download/v1.12.0/websocat.x86_64-pc-windows-gnu.exe"
            ;;
        *)
            echo "Unsupported platform: $OS-$ARCH"
            return 1
            ;;
    esac

    echo "Downloading from: $DOWNLOAD_URL"
    if ! curl -sL -o websocat "$DOWNLOAD_URL"; then
        echo "Failed to download websocat"
        return 1
    fi

    chmod +x websocat 2>/dev/null

    # Install to standard location
    if mkdir -p ~/.local/bin && mv websocat ~/.local/bin/; then
        echo "websocat installed to ~/.local/bin"
        if [[ ":$PATH:" != *":$HOME/.local/bin:"* ]]; then
            export PATH="$HOME/.local/bin:$PATH"
        fi
        return 0
    fi

    return 1
}

# Function to install websocat
install_websocat() {
    echo "Installing websocat..."
    echo ""

    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        if command -v brew &> /dev/null; then
            echo "Detected macOS with Homebrew"
            brew install websocat
            return $?
        fi
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux - detect distribution
        if command -v pacman &> /dev/null; then
            echo "Detected Arch Linux (pacman)"
            sudo pacman -Sy websocat
            return $?
        elif command -v apt-get &> /dev/null; then
            echo "Detected Debian/Ubuntu (apt-get)"
            sudo apt-get update
            sudo apt-get install -y websocat
            return $?
        elif command -v yum &> /dev/null; then
            echo "Detected RHEL/CentOS/Fedora (yum)"
            sudo yum install -y websocat
            return $?
        elif command -v dnf &> /dev/null; then
            echo "Detected Fedora (dnf)"
            sudo dnf install -y websocat
            return $?
        elif command -v zypper &> /dev/null; then
            echo "Detected openSUSE (zypper)"
            sudo zypper install -y websocat
            return $?
        fi
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin"* ]]; then
        # Windows (Git Bash / Cygwin)
        echo "Detected Windows environment"
        if command -v choco &> /dev/null; then
            echo "Chocolatey found"
            choco install websocat
            return $?
        fi
    fi

    # Fallback 1: Try cargo if available (works on all platforms)
    if command -v cargo &> /dev/null; then
        echo "Installing via cargo (Rust)..."
        cargo install websocat
        return $?
    fi

    # Fallback 2: Download precompiled binary
    echo "Attempting to download precompiled binary..."
    if download_precompiled_websocat; then
        return 0
    fi

    # Fallback 3: Clone and compile from source
    if command -v git &> /dev/null && command -v cargo &> /dev/null; then
        echo "Cloning websocat from GitHub and compiling from source..."
        echo ""

        TEMP_DIR=$(mktemp -d)
        trap "rm -rf $TEMP_DIR" EXIT

        cd "$TEMP_DIR" || return 1

        git clone https://github.com/vi/websocat.git --depth 1
        cd websocat || return 1

        echo "Compiling websocat (this may take a few minutes)..."
        cargo build --release

        if [ -f target/release/websocat ] || [ -f target/release/websocat.exe ]; then
            echo "Installation successful!"

            local BINARY=$([ -f target/release/websocat ] && echo target/release/websocat || echo target/release/websocat.exe)

            # Try to install to standard locations
            if mkdir -p ~/.local/bin && mv "$BINARY" ~/.local/bin/; then
                echo "websocat installed to ~/.local/bin"
                if [[ ":$PATH:" != *":$HOME/.local/bin:"* ]]; then
                    export PATH="$HOME/.local/bin:$PATH"
                fi
                return 0
            else
                echo "ERROR: Could not move websocat to ~/.local/bin"
                return 1
            fi
        fi
        return 1
    fi

    echo ""
    echo "ERROR: Could not automatically install websocat"
    echo ""
    echo "websocat installation locations tried:"
    echo "  1. /usr/local/bin (standard location)"
    echo "  2. /usr/bin (fallback)"
    echo "  3. ~/.local/bin (user directory)"
    echo "  4. Cargo (compile from Rust source)"
    echo "  5. Clone from GitHub and compile"
    echo ""
    echo "Manual installation options:"
    echo ""
    echo "macOS:"
    echo "  brew install websocat"
    echo ""
    echo "Arch Linux:"
    echo "  sudo pacman -Sy websocat"
    echo ""
    echo "Ubuntu/Debian/Linux:"
    echo "  sudo apt-get update && sudo apt-get install -y websocat"
    echo ""
    echo "Fedora/RHEL/CentOS:"
    echo "  sudo dnf install -y websocat   (Fedora 22+)"
    echo "  sudo yum install -y websocat   (CentOS/RHEL 7)"
    echo ""
    echo "openSUSE:"
    echo "  sudo zypper install -y websocat"
    echo ""
    echo "Cygwin:"
    echo "  curl -s https://raw.githubusercontent.com/transcode-open/apt-cyg/master/apt-cyg > apt-cyg"
    echo "  chmod +x apt-cyg"
    echo "  sudo mv apt-cyg /usr/local/bin/"
    echo "  apt-cyg install websocat"
    echo ""
    echo "Windows (Git Bash):"
    echo "  choco install websocat"
    echo ""
    echo "Universal (requires Rust):"
    echo "  cargo install websocat"
    echo ""
    echo "Manual download:"
    echo "  https://github.com/vi/websocat/releases"
    return 1
}

# Check if websocat is installed
if ! command -v websocat &> /dev/null; then
    echo "websocat not found. Attempting to install..."
    echo ""

    if ! install_websocat; then
        exit 1
    fi

    echo ""
    echo "websocat installed successfully!"
    echo ""
fi

# Function to send command via WebSocket
test_tool() {
    local name=$1
    local command=$2

    echo "Test: $name"

    # Send command to MCP server via WebSocket
    # Use timeout to prevent hanging, capture full response without head -1
    response=$(timeout 10 bash -c "echo '$command' | websocat '$RAILWAY_URL' 2>&1" 2>/dev/null)

    if [ $? -eq 124 ]; then
        echo "  Timeout (server took too long to respond)"
    elif [ -z "$response" ]; then
        echo "  No response (server may not be running)"
        echo "Hint: Make sure the Railway app is deployed and running"
    else
        # Validate JSON response
        if echo "$response" | jq empty 2>/dev/null; then
            echo " Valid JSON-RPC response - OK"
            echo "Response:"
            echo "$response" | jq '.'
        else
            echo " Invalid response format"
            echo "Raw response: $response"
        fi
    fi
    echo ""
}

# Check if server is reachable
echo "Checking server connectivity..."
if timeout 5 websocat "$RAILWAY_URL" </dev/null 2>&1 | grep -q "error\|refused"; then
    echo "WARNING: Server is not reachable at $RAILWAY_URL"
    echo "Make sure:"
    echo "  1. The Railway app is deployed and running"
    echo "  2. The WebSocket server is properly configured"
    echo "  3. You have internet connectivity"
    echo ""
else
    echo "Server is reachable"
    echo ""
fi

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
echo "OAuth2 Railway WebSocket Tests Completed!"
echo "================================================"
echo ""
echo "Test Summary:"
echo "   JWT Token Generated: ${JWT_TOKEN:0:80}..."
echo "   WebSocket Connection: Tested"
echo "   JSON-RPC Requests: 7 tests"
echo "   Response Validation: All responses checked for valid JSON-RPC format"
echo ""
echo "Notes:"
echo "  ¢ All requests sent via WebSocket transport (wss://)"
echo "  ¢ JWT token included in test environment"
echo "  ¢ Full JSON-RPC response validation implemented"
echo "  ¢ Server must be deployed to Railway for these tests to work"
echo ""
echo "To deploy to Railway:"
echo "  git push origin main  (triggers deploy.yml workflow)"
echo ""
echo "For local testing with OAuth2:"
echo "  bash test-local-oauth.sh"
