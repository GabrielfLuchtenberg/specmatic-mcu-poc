# Thanos Gauntlet — consumer

Rust/Axum consumer of Avengers HQ’s central contract at `../contracts/heroes.yaml`.

```bash
cargo test                         # fast tests, no server required
./scripts/test-against-mock.sh      # requires Docker; uses the Specmatic mock
```

To run the API against the mock manually, start `./scripts/start-mock.sh` and run:

```bash
AVENGERS_HQ_URL=http://localhost:9000 cargo run
curl http://localhost:3000/targets/1
```

The integration suite is intentionally ignored during plain `cargo test`; the script and GitHub Actions run it against a mock generated from the same OpenAPI file. See the [repository README](../README.md) for details.
