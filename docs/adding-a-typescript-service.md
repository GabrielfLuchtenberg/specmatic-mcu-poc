# Adding a Node.js/TypeScript service

This guide adds a third service to the current monorepo without duplicating contracts. The existing applications remain:

- `avengers-hq`: Kotlin/Spring Boot provider on port 8080.
- `thanos-gauntlet`: Rust/Axum consumer on port 3000.
- `contracts/heroes.yaml`: shared OpenAPI contract.

The new service will be `sentinel-watch`, a Node.js/TypeScript provider on port 8090 exposing `GET /alerts/{heroId}`. It can consume Avengers HQ or own a separate contract. For this example it owns a new contract file, `contracts/sentinel-watch.yaml`, while reusing the hero id concept. Do not add its API to `heroes.yaml`; one OpenAPI file should describe one service boundary.

## 1. Add the TypeScript service skeleton

Create:

```text
sentinel-watch/
  package.json
  tsconfig.json
  src/server.ts
  src/alerts.ts
  test/alerts.test.ts
```

Use a pinned Node version in `.nvmrc` or `package.json.engines`, and use a lockfile (`package-lock.json`, `pnpm-lock.yaml`, or `yarn.lock`). A minimal `package.json` can use Fastify:

```json
{
  "private": true,
  "scripts": {
    "build": "tsc -p tsconfig.json",
    "start": "node dist/server.js",
    "test": "vitest run"
  },
  "dependencies": { "fastify": "^5.0.0" },
  "devDependencies": { "@types/node": "^22.0.0", "typescript": "^5.0.0", "vitest": "^3.0.0" }
}
```

Language-specific requirements:

- Enable `strict: true` in `tsconfig.json`.
- Keep request/response types in TypeScript, but remember they disappear at runtime; the OpenAPI contract and Specmatic perform runtime HTTP validation.
- Validate untrusted path/query/body data at runtime (Fastify schemas, Zod, or equivalent). A TypeScript interface alone does not validate JSON.
- Use `number` for OpenAPI integer values, and check safe-integer limits if ids can exceed JavaScript’s safe range.
- Return explicit HTTP status codes and JSON content types; do not rely on framework defaults for error shapes.

## 2. Define the service contract for Specmatic

Create `contracts/sentinel-watch.yaml` with `openapi: 3.0.3`, `info`, `servers`, `/alerts/{heroId}`, response schemas, and named examples:

```yaml
openapi: 3.0.3
info:
  title: Sentinel Watch
  version: "1.0.0"
servers:
  - url: http://localhost:8090
paths:
  /alerts/{heroId}:
    get:
      operationId: getHeroAlert
      parameters:
        - name: heroId
          in: path
          required: true
          schema: { type: integer, format: int64, minimum: 1 }
          examples:
            IRON_MAN_ALERT_200_OK: { value: 1 }
      responses:
        "200":
          description: Current alert
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/Alert"
              examples:
                IRON_MAN_ALERT_200_OK:
                  value:
                    heroId: 1
                    severity: LOW
                    active: false
        "404":
          description: No alert subject exists
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Error" }
components:
  schemas:
    Alert:
      type: object
      additionalProperties: false
      required: [heroId, severity, active]
      properties:
        heroId: { type: integer, format: int64 }
        severity: { type: string, enum: [LOW, HIGH, CRITICAL] }
        active: { type: boolean }
    Error:
      type: object
      required: [status, message]
      properties:
        status: { type: integer }
        message: { type: string }
```

Specmatic-specific requirements:

- Put the file in the central `contracts/` directory so the root CI sees it.
- Keep request examples and response examples using exactly the same scenario name and status suffix.
- Make required fields and enum values explicit; Specmatic generates requests and validates responses from these constraints.
- Add the service to a Specmatic configuration or invoke the contract file directly. For this repository’s Docker-based checks, direct invocation is simplest: `specmatic test contracts/sentinel-watch.yaml --testBaseURL=http://host.docker.internal:8090` and `specmatic mock contracts/sentinel-watch.yaml --port 9090`.
- Add a compatibility check for the new file. It should be additive on its first PR; later removal/renaming of required fields is breaking.

## 3. Implement and test the Node service

In `src/server.ts`, register `GET /alerts/:heroId`, return the exact `Alert` JSON shape, and listen on `process.env.PORT ?? 8090`. Keep business logic in `src/alerts.ts`. Add unit tests for severity calculation and an HTTP test for 200/404 behavior.

For consumer-style tests of another service, run a Specmatic mock from this contract and point the Node client at it with an environment variable such as `SENTINEL_WATCH_URL=http://localhost:9090`. Do not replace the mock with a hand-written fake: the purpose is to test the client against the executable contract.

## 4. Wire CI and local commands

Extend `.github/workflows/validate.yml` with a Node job:

```yaml
- uses: actions/setup-node@v4
  with:
    node-version-file: sentinel-watch/.nvmrc
    cache: npm
    cache-dependency-path: sentinel-watch/package-lock.json
- run: npm ci
  working-directory: sentinel-watch
- run: npm test
  working-directory: sentinel-watch
- run: npm run build
  working-directory: sentinel-watch
```

Add a service-contract step that starts the built server, waits for `http://localhost:8090/health`, then runs the Specmatic image against `contracts/sentinel-watch.yaml`. Add Docker cleanup with `if: always()`.

Keep the existing Kotlin and Rust jobs unchanged; they prove their own service boundaries. Add the new contract to the compatibility job’s target path (the root `contracts/` directory already includes it). Update the root README and Makefile with `sentinel-test` and `sentinel-contract-test` commands.

The final pull request should contain the service code, its contract, named examples, tests, CI wiring, and documentation together. Specmatic validates the HTTP agreement; TypeScript tests validate implementation behavior; neither replaces the other.
