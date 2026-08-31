#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "$ROOT/.." && pwd)"
CONTRACTS_DIR="${CONTRACTS_DIR:-$REPO_ROOT/contracts}"

echo "Starting Specmatic mock for Avengers HQ on http://localhost:9000"
if command -v specmatic >/dev/null 2>&1; then
  exec specmatic mock "$CONTRACTS_DIR/heroes.yaml" --port 9000
fi

docker run --rm -p 9000:9000 \
  -v "$CONTRACTS_DIR:/contracts:ro" \
  specmatic/specmatic mock /contracts/heroes.yaml --port 9000
