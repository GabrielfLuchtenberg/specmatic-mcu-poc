#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "$ROOT/.." && pwd)"
cd "$REPO_ROOT"
echo "Starting Specmatic mock inside Compose (service DNS: http://specmatic-mock:9000)"
exec docker compose up --build specmatic-mock
