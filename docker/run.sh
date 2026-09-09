#!/usr/bin/env bash
set -e

echo "==> Starting JTrac container on http://localhost:8888"
docker run -d \
  -p 8888:8080 \
  -v jtrac_data:/jtrac-data \
  --name jtrac \
  jtrac:latest
echo "==> JTrac is running! Access it at: http://localhost:8888"
