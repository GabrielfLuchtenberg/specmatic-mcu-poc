# Avengers HQ — provider

Kotlin/Spring Boot provider for the central contract at `../contracts/heroes.yaml`.

```bash
./gradlew bootRun
curl http://localhost:8080/heroes/1
```

Run the provider contract test from this directory (requires Docker):

```bash
./scripts/test-contract.sh
```

The script starts the application, then runs Specmatic’s official CLI image against it. A response that omits, renames, or changes the type of a contract field fails the check. See the [repository README](../README.md) for the full workflow.
