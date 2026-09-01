#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <compose-service>" >&2
  exit 2
fi

cd "$REPO_ROOT"
cleanup() { docker compose down --volumes --remove-orphans; }
trap cleanup EXIT
docker compose run --build --rm "$1"
