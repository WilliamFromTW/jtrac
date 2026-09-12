#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "==> Building JTrac Docker image from context: .."
docker build -f Dockerfile -t jtrac:latest -t jtrac:2.3.3-2.1.0-beta ..
docker build -f Dockerfile -t jtrac:latest -t jtrac:latest ..
echo "==> Build complete! Images: jtrac:latest, jtrac:2.3.3-2.1.0-beta"
