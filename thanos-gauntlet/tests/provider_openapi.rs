use thanos_gauntlet::provider_openapi::{ProviderApi, TargetReport};
use utoipa::OpenApi;

#[test]
fn provider_contract_contains_only_rust_owned_operations() {
    let document = ProviderApi::openapi().to_json().expect("serialize OpenAPI");
    assert!(document.contains("/targets/{id}"));
    assert!(!document.contains("/heroes/{id}"));
    assert!(document.contains("heroId"));
    assert!(document.contains("powerLevel"));
    assert!(document.contains("infinityStoneStatus"));
    assert!(document.contains("\"200\""));
    assert!(document.contains("\"404\""));
    assert!(document.contains("\"502\""));
}

#[test]
fn target_report_serialization_is_camel_case_and_complete() {
    let value = serde_json::to_value(TargetReport {
        hero_id: 1,
        alias: "Iron Man".into(),
        power_level: 95,
        infinity_stone_status: "SECURED".into(),
        snap_viable: true,
    }).unwrap();
    assert_eq!(value["heroId"], 1);
    assert_eq!(value["powerLevel"], 95);
    assert_eq!(value["infinityStoneStatus"], "SECURED");
    assert!(value.get("hero_id").is_none());
}
