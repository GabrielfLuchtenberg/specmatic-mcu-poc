#!/usr/bin/env bash
# Thin local/CI wrapper around the root Compose validation model.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
exec "$ROOT/../scripts/compose-run.sh" thanos-contract-test
