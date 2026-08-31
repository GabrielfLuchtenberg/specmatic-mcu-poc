#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUTPUT_DIR="${1:-$ROOT_DIR/build/generated-contracts}"
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"
for contract in "$ROOT_DIR"/contracts/*.yaml; do
  cp "$contract" "$OUTPUT_DIR/$(basename "$contract")"
done
echo "Materialized OpenAPI contracts in $OUTPUT_DIR"
