#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUTPUT_DIR="${1:-$ROOT_DIR/build/generated-contracts}"
if [[ "$OUTPUT_DIR" != /* ]]; then
  OUTPUT_DIR="$ROOT_DIR/$OUTPUT_DIR"
fi

case "$OUTPUT_DIR" in
  ""|"/"|"$ROOT_DIR"|"$HOME")
    echo "Refusing unsafe output directory: $OUTPUT_DIR" >&2
    exit 2
    ;;
esac

PARENT_DIR="$(dirname "$OUTPUT_DIR")"
mkdir -p "$PARENT_DIR"
STAGING_DIR="$(mktemp -d "$PARENT_DIR/.contracts.XXXXXX")"
cleanup() { rm -rf "$STAGING_DIR"; }
trap cleanup EXIT

"$ROOT_DIR/avengers-hq/gradlew" -p "$ROOT_DIR/avengers-hq" generateOpenApi \
  -PopenApiOutput="$STAGING_DIR/heroes.yaml" --no-daemon
(
  cd "$ROOT_DIR/thanos-gauntlet"
  cargo run --quiet --bin generate-openapi -- "$STAGING_DIR/thanos-gauntlet.yaml"
)

for contract in "$STAGING_DIR"/*.yaml; do
  test -s "$contract" || { echo "Generator produced an empty contract: $contract" >&2; exit 1; }
  grep -q '^openapi:' "$contract" || { echo "Invalid OpenAPI document: $contract" >&2; exit 1; }
done

rm -rf "$OUTPUT_DIR"
mv "$STAGING_DIR" "$OUTPUT_DIR"
trap - EXIT
echo "Generated provider-owned OpenAPI contracts in $OUTPUT_DIR"
