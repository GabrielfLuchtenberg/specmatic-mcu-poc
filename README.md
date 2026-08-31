# Specmatic MCU POC

This is one repository with one executable OpenAPI contract and two applications:

| Application | Role | What CI proves |
| --- | --- | --- |
| `avengers-hq/` (Kotlin + Spring Boot) | **Provider** of `GET /heroes/{id}` | The real HTTP response conforms to the OpenAPI contract. |
| `thanos-gauntlet/` (Rust + Axum) | **Consumer** of that endpoint | The HTTP client works against a Specmatic mock generated from the contract. |
| `contracts/heroes.yaml` | Shared contract | Changes are backward-compatible with the base branch. |

## How Specmatic works here

OpenAPI describes the agreement: URL, path parameter, status codes, JSON fields, types, and allowed values. Specmatic turns that document into executable checks in two directions.

1. **Provider contract testing.** `avengers-hq/scripts/test-contract.sh` starts the Spring Boot application on port 8080. The official Specmatic CLI image generates requests from `contracts/heroes.yaml`, sends them to the running application, and validates every response against the specified status code and schema. If Kotlin changes `powerLevel` to `strength` without a contract change, this job fails because the required JSON property is missing.
2. **Consumer service virtualization.** Specmatic runs an HTTP mock from the *same* OpenAPI file on port 9000. The Rust client calls that mock in `tests/contract_client.rs`. Named OpenAPI examples connect the parameter example (`IRON_MAN_200_OK`, id `1`) with the response example, so the mock returns the expected Iron Man response. If Rust deserializes `strength` instead of `powerLevel`, the consumer test fails.
3. **Compatibility governance.** On every pull request, Specmatic compares `contracts/` with the PR base branch. Removing or renaming a required response field is a breaking change and fails the compatibility job before either application needs to be deployed.

The contract is deliberately stored only at `contracts/heroes.yaml`. Neither application owns a copied version, so a pull request cannot accidentally update a provider spec while leaving the consumer’s mock stale.

## Prerequisites

- JDK 21
- Rust stable
- Docker (required for the consumer’s Specmatic mock and compatibility job)

## Run locally

From the repository root:

```bash
make provider-test          # Spring Boot + Specmatic provider contract test
make consumer-test          # fast Rust unit tests; no server needed
make consumer-contract-test # Specmatic mock + Rust integration tests
make verify                 # all three checks
```

Equivalent commands are available without `make`:

```bash
./avengers-hq/scripts/test-contract.sh
cargo test --manifest-path thanos-gauntlet/Cargo.toml
./thanos-gauntlet/scripts/test-against-mock.sh
```

To explore manually, start the mock with `./thanos-gauntlet/scripts/start-mock.sh`, then run the consumer with `AVENGERS_HQ_URL=http://localhost:9000 cargo run --manifest-path thanos-gauntlet/Cargo.toml`. It exposes `GET http://localhost:3000/targets/1`. To call the real provider instead, run `./avengers-hq/gradlew -p avengers-hq bootRun` and point the consumer at `http://localhost:8080`.

## The contract and its examples

`GET /heroes/{id}` returns a `Hero` with required fields `id`, `name`, `alias`, `powerLevel`, and `infinityStoneStatus`. `location` is optional. The only allowed stone statuses are `SECURED`, `MISSING`, and `COMPROMISED`. Unknown heroes return a 404 body with `status` and `message`.

The examples are executable fixtures, not prose. `IRON_MAN_200_OK` links request id `1` to the matching 200 response; `UNKNOWN_HERO_404` links id `666` to the 404 response. Add or change examples when you need deterministic mock behaviour for a consumer scenario.

## CI and pull requests

`.github/workflows/validate.yml` runs three independent required checks:

1. **Provider honours OpenAPI** starts Spring Boot and runs Specmatic’s CLI contract test.
2. **Consumer works against Specmatic mock** runs Rust unit tests, then boots the mock and runs the ignored integration tests explicitly.
3. **Contract remains backward compatible** compares the PR contract with its base branch.

After the repository is on GitHub, mark these three checks as required in the `main` branch protection rule. The workflow runs on every pull request and every push to `main`.

## Suggested demonstration pull requests

- `example/provider-add-hulk`: provider-only change that adds Hero id 5 while leaving the contract unchanged. This demonstrates that additive implementation work remains safe until a contract example is intentionally added.
- `example/consumer-snap-report`: consumer-only change that adds a report field derived from existing contract data. This demonstrates an independent consumer change while all contract checks stay green.
- `example/breaking-power-level-rename`: intentionally rename `powerLevel` in the contract to show the compatibility gate failing. Do not merge it; it is a teaching PR.

## Breaking-change drill

Run `./avengers-hq/scripts/demo-breaking-change.sh`. It temporarily renames `powerLevel` in the central contract and invokes Specmatic’s backward-compatibility check. The script restores the file automatically. For an implementation-only break, rename `powerLevel` in `Hero.kt` but leave the contract unchanged, then run `make provider-test`; the provider contract test must fail.
