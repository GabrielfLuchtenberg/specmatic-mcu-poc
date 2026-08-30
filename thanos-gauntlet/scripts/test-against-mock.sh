#!/usr/bin/env bash
# Runs consumer integration tests against a Specmatic mock built from the
# single contract at the repository root.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

cleanup() {
  docker compose down --volumes --remove-orphans
}
trap cleanup EXIT

docker compose up --detach specmatic-mock

for _ in $(seq 1 30); do
  if curl --fail --silent http://localhost:9000/heroes/1 >/dev/null; then
    AVENGERS_HQ_URL=http://localhost:9000 cargo test --test contract_client -- --ignored
    exit 0
  fi
  sleep 1
done

docker compose logs
echo "Specmatic mock did not become ready" >&2
exit 1
