# Specmatic MCU POC

This is one repository with one executable OpenAPI contract and two applications:

| Application | Role | What CI proves |
| --- | --- | --- |
| `avengers-hq/` (Kotlin + Spring Boot) | **Provider** of `GET /heroes/{id}` | The real HTTP response conforms to the OpenAPI contract. |
| `thanos-gauntlet/` (Rust + Axum) | **Consumer** of that endpoint | The HTTP client works against a Specmatic mock generated from the contract. |
| `contracts/heroes.yaml` | Shared contract | Changes are backward-compatible with the base branch. |

## How Specmatic works here

OpenAPI describes the agreement: URL, path parameter, status codes, JSON fields, types, and allowed values. Specmatic turns that document into executable checks in two directions.

1. **Provider contract testing.** Compose builds and health-checks the Spring Boot application, then the Specmatic container sends requests to `http://avengers-hq:8080` and validates every response. If Kotlin changes `powerLevel` to `strength` without a contract change, this job fails because the required JSON property is missing.
2. **Consumer service virtualization.** Specmatic runs an HTTP mock from the *same* generated OpenAPI artifact. The containerized Rust contract tests call it at `http://specmatic-mock:9000`. Named OpenAPI examples connect the parameter example (`IRON_MAN_200_OK`, id `1`) with the response example, so the mock returns the expected Iron Man response.
3. **Compatibility governance.** On every pull request, Specmatic compares `contracts/` with the PR base branch. Removing or renaming a required response field is a breaking change and fails the compatibility job before either application needs to be deployed.

The source contract is deliberately stored only at `contracts/heroes.yaml`. `generate-contracts` materializes it once in `build/generated-contracts`, and the provider test and mock mount that same directory read-only. The generator remains replaceable through the stable `scripts/generate-contracts.sh <output-directory>` interface.

## Prerequisites

- Docker Engine with Docker Compose v2
- Git and Make

No host JDK, Gradle, Rust, Cargo, Node, Curl, or Specmatic installation is needed for validation.

## Run locally

From the repository root:

```bash
make provider-test          # Spring Boot + Specmatic provider contract test
make consumer-test          # fast Rust unit tests; no server needed
make consumer-contract-test # Specmatic mock + Rust integration tests
make verify                 # all three checks
```

The shell wrappers call the same root `compose.yaml` services used by CI:

```bash
./avengers-hq/scripts/test-contract.sh
./scripts/compose-run.sh thanos-unit-test
./thanos-gauntlet/scripts/test-against-mock.sh
```

`docker compose` uses internal service DNS rather than host networking. The validation services therefore do not publish ports and do not depend on `host.docker.internal`. To inspect a failure, run `docker compose logs avengers-hq specmatic-mock`; `make clean` removes containers, volumes, and orphans. CI assigns a unique Compose project name to each check.

For native development only, you may run `cargo` or `./avengers-hq/gradlew` directly if those toolchains are installed. Those commands are conveniences, not the supported validation path.

## Images and build cache

All base and tool image versions are explicit in `.env` and passed to the multi-stage Dockerfiles. The provider uses a Gradle/JDK 21 builder and slim JRE runtime. The consumer Dockerfile exposes separate unit-test, contract-test, runtime, and build stages. Gradle and Cargo dependency layers are cached before application sources are copied. Update pinned versions in `.env`; do not replace them with `latest`, `stable`, or an unqualified Specmatic tag.

## The contract and its examples

`GET /heroes/{id}` returns a `Hero` with required fields `id`, `name`, `alias`, `powerLevel`, and `infinityStoneStatus`. `location` is optional. The only allowed stone statuses are `SECURED`, `MISSING`, and `COMPROMISED`. Unknown heroes return a 404 body with `status` and `message`.

The examples are executable fixtures, not prose. `IRON_MAN_200_OK` links request id `1` to the matching 200 response; `UNKNOWN_HERO_404` links id `666` to the 404 response. Add or change examples when you need deterministic mock behaviour for a consumer scenario.

## CI and pull requests

`.github/workflows/validate.yml` runs three independent required checks:

1. **Provider honours OpenAPI** builds Spring Boot and runs Specmatic’s CLI contract test entirely in Compose.
2. **Consumer works against Specmatic mock** runs containerized Rust unit tests, then health-gates the containerized contract tests on the mock.
3. **Contract remains backward compatible** compares the PR contract with its base branch using the pinned Specmatic Compose service.

After the repository is on GitHub, mark these three checks as required in the `main` branch protection rule. The workflow runs on every pull request and every push to `main`.

## Suggested demonstration pull requests

- `example/provider-add-hulk`: provider-only change that adds Hero id 5 while leaving the contract unchanged. This demonstrates that additive implementation work remains safe until a contract example is intentionally added.
- `example/consumer-snap-report`: consumer-only change that adds a report field derived from existing contract data. This demonstrates an independent consumer change while all contract checks stay green.
- `example/breaking-power-level-rename`: intentionally rename `powerLevel` in the contract to show the compatibility gate failing. Do not merge it; it is a teaching PR.

## Breaking-change drill

Run `./avengers-hq/scripts/demo-breaking-change.sh`. It temporarily renames `powerLevel` in the central contract and invokes Specmatic’s backward-compatibility check. The script restores the file automatically. For an implementation-only break, rename `powerLevel` in `Hero.kt` but leave the contract unchanged, then run `make provider-test`; the provider contract test must fail.
