.PHONY: provider-test consumer-test consumer-contract-test verify

provider-test:
	./avengers-hq/scripts/test-contract.sh

consumer-test:
	cargo test --manifest-path thanos-gauntlet/Cargo.toml

consumer-contract-test:
	./thanos-gauntlet/scripts/test-against-mock.sh

verify: provider-test consumer-test consumer-contract-test
