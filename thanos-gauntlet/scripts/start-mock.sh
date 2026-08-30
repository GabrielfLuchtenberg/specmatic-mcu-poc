#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "$ROOT/.." && pwd)"

echo "Starting Specmatic mock for Avengers HQ on http://localhost:9000"
if command -v specmatic >/dev/null 2>&1; then
  exec specmatic mock "$REPO_ROOT/contracts/heroes.yaml" --port 9000
fi

docker run --rm -p 9000:9000 \
  -v "$REPO_ROOT:/workspace" \
  -w /workspace \
  specmatic/specmatic mock /workspace/contracts/heroes.yaml --port 9000
