#!/usr/bin/env bash
# Thin local/CI wrapper around the root Compose validation model.
set -euo pipefail

APP_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
exec "$APP_ROOT/../scripts/compose-run.sh" specmatic-provider-test
