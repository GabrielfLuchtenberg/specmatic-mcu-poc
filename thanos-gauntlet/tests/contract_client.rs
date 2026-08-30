//! Consumer contract tests: the HTTP client is exercised against a Specmatic
//! mock generated from the same OpenAPI spec the Kotlin provider honours.
//!
//! Start the mock first (see scripts/start-mock.sh or docker compose), then:
//!   AVENGERS_HQ_URL=http://localhost:9000 cargo test

use pretty_assertions::assert_eq;
use thanos_gauntlet::{AvengersClient, GauntletError};

fn mock_url() -> String {
    std::env::var("AVENGERS_HQ_URL").unwrap_or_else(|_| "http://localhost:9000".into())
}

#[tokio::test]
#[ignore = "requires the Specmatic mock; run scripts/test-against-mock.sh"]
async fn iron_man_matches_named_example() {
    let client = AvengersClient::new(mock_url());
    let hero = client
        .get_hero(1)
        .await
        .expect("Specmatic mock should return IRON_MAN_200_OK for id=1");

    assert_eq!(hero.id, 1);
    assert_eq!(hero.name, "Tony Stark");
    assert_eq!(hero.alias, "Iron Man");
    assert_eq!(hero.power_level, 95);
    assert_eq!(hero.infinity_stone_status, "SECURED");
    assert_eq!(hero.location.as_deref(), Some("Avengers Tower"));
}

#[tokio::test]
#[ignore = "requires the Specmatic mock; run scripts/test-against-mock.sh"]
async fn thor_matches_named_example() {
    let client = AvengersClient::new(mock_url());
    let hero = client.get_hero(2).await.expect("THOR_200_OK");
    assert_eq!(hero.alias, "God of Thunder");
    assert_eq!(hero.power_level, 99);
}

#[tokio::test]
#[ignore = "requires the Specmatic mock; run scripts/test-against-mock.sh"]
async fn unknown_hero_is_404() {
    let client = AvengersClient::new(mock_url());
    let err = client.get_hero(666).await.expect_err("UNKNOWN_HERO_404");
    assert!(matches!(err, GauntletError::NotFound));
}

#[tokio::test]
#[ignore = "requires the Specmatic mock; run scripts/test-against-mock.sh"]
async fn client_deserializes_required_contract_fields() {
    let client = AvengersClient::new(mock_url());
    let hero = client.get_hero(1).await.expect("hero");
    assert!(hero.power_level >= 0 && hero.power_level <= 100);
    assert!(!hero.infinity_stone_status.is_empty());
}
