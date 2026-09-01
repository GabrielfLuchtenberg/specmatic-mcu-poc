#!/usr/bin/env bash
# Demonstrates two ways Specmatic blocks a breaking change locally.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "$ROOT/.." && pwd)"
cd "$REPO_ROOT"
set -a
source "$REPO_ROOT/.env"
set +a

CONTRACT="$REPO_ROOT/contracts/heroes.yaml"
BACKUP="$(mktemp)"
cp "$CONTRACT" "$BACKUP"
cleanup() { cp "$BACKUP" "$CONTRACT"; rm -f "$BACKUP"; }
trap cleanup EXIT

echo "==> Scenario A: rename powerLevel -> strength in the OpenAPI spec"
echo "    Specmatic backward-compatibility-check should FAIL (exit 1)."
sed -i.bak 's/powerLevel/strength/g' "$CONTRACT"
rm -f "$CONTRACT.bak"

docker run --rm \
  -v "$ROOT:/usr/src/app" \
  -w /usr/src/app \
  "specmatic/specmatic:$SPECMATIC_VERSION" \
  backward-compatibility-check --target-path contracts || true

echo
echo "==> Restoring the contract (trap on EXIT)."
echo "==> Scenario B is in README: rename the Kotlin field without updating the spec,"
echo "    then run ./gradlew test — ContractTest fails because the JSON no longer matches."
