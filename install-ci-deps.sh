#!/bin/bash

# Install CI dependencies for GitHub Actions
# Handles websocat installation with fallbacks

set -e

echo "Installing CI dependencies..."

# Update package manager
sudo apt-get update -qq

# Install jq (usually available)
echo "Installing jq..."
sudo apt-get install -y jq

# Try to install websocat via Rust (most reliable in CI)
echo "Installing websocat via cargo..."

# Check if cargo is available
if ! command -v cargo &> /dev/null; then
    echo "Cargo not found, installing Rust..."
    curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y --quiet
    source $HOME/.cargo/env
fi

# Install websocat via cargo
echo "Building websocat from source via cargo..."
cargo install websocat --quiet 2>&1 || {
    echo "Cargo install failed, trying precompiled binary..."

    # Download precompiled binary
    echo "Downloading websocat binary..."
    mkdir -p /tmp/websocat
    cd /tmp/websocat

    WEBSOCAT_VERSION="1.12.0"
    DOWNLOAD_URL="https://github.com/vi/websocat/releases/download/v${WEBSOCAT_VERSION}/websocat.x86_64-unknown-linux-musl"

    if curl -sL -o websocat "$DOWNLOAD_URL"; then
        chmod +x websocat
        sudo mv websocat /usr/local/bin/
        echo "websocat installed from precompiled binary"
    else
        echo "ERROR: Could not install websocat"
        exit 1
    fi
}

# Verify installations
echo ""
echo "Verifying installations..."
jq --version
websocat --version

echo ""
echo "✅ All dependencies installed successfully!"
