#!/usr/bin/env bash
# Boots the provider and asks the official Specmatic CLI image to validate it
# against the central OpenAPI contract.
set -euo pipefail

APP_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "$APP_ROOT/.." && pwd)"
cd "$APP_ROOT"
mkdir -p "$APP_ROOT/build"

cleanup() {
  if [[ -n "${APP_PID:-}" ]]; then
    kill "$APP_PID" 2>/dev/null || true
    wait "$APP_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT

GRADLE_USER_HOME="${GRADLE_USER_HOME:-$APP_ROOT/.gradle}" \
  ./gradlew bootJar --no-daemon
java -jar "$APP_ROOT/build/libs/avengers-hq-1.0.0.jar" >"$APP_ROOT/build/specmatic-provider.log" 2>&1 &
APP_PID=$!

for _ in $(seq 1 90); do
  if curl --fail --silent http://localhost:8080/actuator/health >/dev/null; then
    docker run --rm \
      --add-host=host.docker.internal:host-gateway \
      -v "$REPO_ROOT:/workspace" \
      -w /workspace \
      specmatic/specmatic \
      test /workspace/contracts/heroes.yaml \
      --testBaseURL=http://host.docker.internal:8080
    exit $?
  fi
  sleep 1
done

cat "$APP_ROOT/build/specmatic-provider.log" >&2
echo "Avengers HQ did not become ready on port 8080" >&2
exit 1
