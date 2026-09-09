#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "==> Building JTrac Docker image from context: .."
docker build -f Dockerfile -t jtrac:latest ..
echo "==> Build complete! Image: jtrac:latest"
