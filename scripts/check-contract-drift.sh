#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
GENERATED_DIR="$(mktemp -d "${TMPDIR:-/tmp}/specmatic-contracts.XXXXXX")"
cleanup() { rm -rf "$GENERATED_DIR"; }
trap cleanup EXIT

"$ROOT_DIR/scripts/generate-contracts.sh" "$GENERATED_DIR"
if ! diff -ru "$ROOT_DIR/contracts" "$GENERATED_DIR"; then
  echo >&2
  echo "Generated OpenAPI contracts have drifted." >&2
  echo "Run ./scripts/generate-contracts.sh contracts and inspect the diff." >&2
  exit 1
fi
echo "Checked-in OpenAPI contracts match provider code."
