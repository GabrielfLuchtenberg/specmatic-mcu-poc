#!/usr/bin/env bash
# Demonstrates two ways Specmatic blocks a breaking change locally.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "$ROOT/.." && pwd)"
cd "$REPO_ROOT"

CONTRACT="$REPO_ROOT/contracts/heroes.yaml"
BACKUP="$(mktemp)"
cp "$CONTRACT" "$BACKUP"
cleanup() { cp "$BACKUP" "$CONTRACT"; rm -f "$BACKUP"; }
trap cleanup EXIT

echo "==> Scenario A: rename powerLevel -> strength in the OpenAPI spec"
echo "    Specmatic backward-compatibility-check should FAIL (exit 1)."
sed -i.bak 's/powerLevel/strength/g' "$CONTRACT"
rm -f "$CONTRACT.bak"

if command -v specmatic >/dev/null 2>&1; then
  specmatic backward-compatibility-check --target-path contracts || true
else
  docker run --rm \
    -v "$ROOT:/usr/src/app" \
    -w /usr/src/app \
    specmatic/specmatic \
    backward-compatibility-check --target-path contracts || true
fi

echo
echo "==> Restoring the contract (trap on EXIT)."
echo "==> Scenario B is in README: rename the Kotlin field without updating the spec,"
echo "    then run ./gradlew test — ContractTest fails because the JSON no longer matches."
