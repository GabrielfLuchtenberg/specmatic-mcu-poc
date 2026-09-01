.PHONY: provider-test consumer-test consumer-contract-test verify clean

provider-test:
	./scripts/compose-run.sh specmatic-provider-test

consumer-test:
	./scripts/compose-run.sh thanos-unit-test

consumer-contract-test:
	./scripts/compose-run.sh thanos-contract-test

verify: provider-test consumer-test consumer-contract-test

clean:
	docker compose down --volumes --remove-orphans
