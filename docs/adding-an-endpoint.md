# Adding an endpoint

Provider code and its OpenAPI metadata are the source of truth. Generated contracts remain checked in under `contracts/` so consumers, Specmatic, reviewers, and compatibility checks have a stable artifact and Git baseline. Never edit generated YAML as the implementation of a change.

## Kotlin provider (`avengers-hq`)

1. Add or change the Spring mapping and Kotlin DTO.
2. Describe the operation with `@Operation`, `@ApiResponses`, `@Parameter`, and `@Schema`. Preserve required versus nullable fields, integer formats and bounds, enum values, descriptions, and closed-object behavior.
3. Add complex named request/response examples in `OpenApiConfiguration`. Matching names such as `IRON_MAN_200_OK` let Specmatic pair deterministic scenarios; include important error responses too.
4. Add a focused generation assertion when the endpoint introduces semantics that must not disappear silently.

## Rust provider boundary (`thanos-gauntlet`)

Only Rust-owned HTTP operations such as `/targets/{id}` belong in `contracts/thanos-gauntlet.yaml`. Add `utoipa` path metadata and `ToSchema` derives beside the provider response types. The `AvengersClient` and its `Hero` model consume `heroes.yaml`; they must not generate or redefine the Kotlin provider contract.

Keep Serde wire names, optionality, and numeric widths aligned with the generated schema and cover them with serialization/schema tests.

## Generate and validate

From the repository root:

```bash
./scripts/generate-contracts.sh contracts
git diff -- contracts
./scripts/check-contract-drift.sh
```

Generation stages every provider document in a validated temporary directory before publishing it. The drift check generates again, compares byte-for-byte with `contracts/`, and prints the regeneration command when code and checked-in artifacts differ.

Review the generated diff before running provider, consumer, and Specmatic compatibility checks. Adding an optional property or a new endpoint is normally additive. Renaming or removing a required field is breaking even after regeneration, and the backward-compatibility check should reject it.

TypeScript is not currently a service in this repository. Add its framework-appropriate, pinned generator and a dedicated service-owned contract only when that provider lands.
