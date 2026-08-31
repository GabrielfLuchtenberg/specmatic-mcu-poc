# Adding an endpoint to the current architecture

This guide describes a contract-first change to the existing `avengers-hq` provider and `thanos-gauntlet` consumer. The shared contract is [../contracts/heroes.yaml](../contracts/heroes.yaml); it is the only API contract file in this repository.

The example endpoint below is `GET /heroes/{id}/power-report`. It returns a report derived from an existing hero. The same sequence applies to any new endpoint.

## 1. Change the OpenAPI contract first

Add the path, operation id, parameters, response status codes, schema, and named examples to `contracts/heroes.yaml`:

```yaml
/heroes/{id}/power-report:
  get:
    operationId: getHeroPowerReport
    parameters:
      - name: id
        in: path
        required: true
        schema:
          type: integer
          format: int64
          minimum: 1
        examples:
          IRON_MAN_POWER_REPORT_200_OK:
            value: 1
          UNKNOWN_HERO_POWER_REPORT_404:
            value: 666
    responses:
      "200":
        description: Power report generated
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/PowerReport"
            examples:
              IRON_MAN_POWER_REPORT_200_OK:
                value:
                  heroId: 1
                  alias: Iron Man
                  powerLevel: 95
                  snapViable: true
      "404":
        description: Hero does not exist
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/Error"
            examples:
              UNKNOWN_HERO_POWER_REPORT_404:
                value:
                  status: 404
                  message: Hero not found
                  path: /heroes/666/power-report
```

Define `PowerReport` under `components.schemas`. Keep `additionalProperties: false` when the response is deliberately closed. Give every generated scenario a unique example name ending in the status code; Specmatic uses matching request/response example names to create deterministic scenarios. Do not edit a second copy under either application—there must remain one source of truth.

Specmatic-specific requirements:

- Include every supported response status in the contract, including errors.
- Mark fields that consumers must receive in `required`.
- Use the correct OpenAPI type and format; a Kotlin `Long`, Rust `u64`, and TypeScript `number` still need an OpenAPI integer definition.
- Add examples for the happy path and important errors when deterministic mock data is useful.
- Treat renaming/removing a required response field as a breaking change. The pull-request compatibility job will compare this file with the PR base.

## 2. Implement the provider in Kotlin/Spring Boot

In `avengers-hq`:

1. Add a response data class, for example `PowerReport`, in `hero/` with Kotlin property names matching the JSON contract. Jackson’s Kotlin module serializes these names directly; use `@JsonProperty` only when the wire name intentionally differs.
2. Add a registry/service method such as `powerReport(id: Long): PowerReport?`. Keep domain calculations in the service/registry, not in the controller.
3. Add `@GetMapping("/heroes/{id}/power-report")` to `HeroController`. Use `@PathVariable id: Long`; return `ResponseEntity.ok(report)` for 200 and the existing `ApiError` shape with `ResponseEntity.status(HttpStatus.NOT_FOUND)` for 404.
4. Keep the application on port 8080. `avengers-hq/scripts/test-contract.sh` starts the app and runs the official `specmatic/specmatic` image against `contracts/heroes.yaml`.

Kotlin-specific checks:

- `Long` maps to OpenAPI `type: integer, format: int64`; do not silently change it to `String`.
- Nullable Kotlin properties serialize as optional fields only when the contract does not list them as required.
- Enum values must exactly match the OpenAPI enum, including case.
- Test a normal response and the 404 path with a Spring test if the calculation has meaningful branching; Specmatic validates the HTTP shape.

Run:

```bash
./avengers-hq/scripts/test-contract.sh
```

## 3. Update the Rust consumer

In `thanos-gauntlet/src/lib.rs`:

1. Add a `PowerReport` struct with `#[derive(Deserialize)]`.
2. Use `#[serde(rename_all = "camelCase")]` if Rust fields are snake_case (`hero_id` → `heroId`).
3. Add an `AvengersClient::get_power_report(id: u64)` method using the same URL path and status mapping as `get_hero`.
4. Return `GauntletError::NotFound` for 404 and preserve a distinct error for unexpected statuses.

Add a mock-backed scenario to `thanos-gauntlet/tests/contract_client.rs` with `#[ignore]`, following the existing tests. The ignore is intentional: plain `cargo test` has no server; CI runs `./scripts/test-against-mock.sh`, which starts Specmatic and executes ignored tests with `--ignored`.

Rust-specific checks:

- `u64` is appropriate for the positive path id, but the OpenAPI contract remains the authority for range validation.
- `i32`/`u32` choices for values such as power level must accommodate the contract’s minimum and maximum.
- Serde rejects missing required fields; do not add `#[serde(default)]` to hide a contract mismatch.

## 4. Validate the complete change

From the repository root:

```bash
make provider-test
make consumer-test
make consumer-contract-test
```

The GitHub workflow runs the same provider and consumer checks and a Specmatic backward-compatibility check. A new endpoint is additive and should pass compatibility; changing an existing required field should fail it. Review the generated Specmatic coverage output rather than relying only on application unit tests.
