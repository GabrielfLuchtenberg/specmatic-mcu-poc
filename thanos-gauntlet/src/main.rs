use axum::{
    extract::{Path, State},
    http::StatusCode,
    response::IntoResponse,
    routing::get,
    Json, Router,
};
use serde::Serialize;
use std::net::SocketAddr;
use thanos_gauntlet::{AvengersClient, GauntletError};
use tower_http::trace::TraceLayer;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

#[derive(Clone)]
struct AppState {
    client: AvengersClient,
}

#[derive(Serialize)]
struct TargetReport {
    hero_id: u64,
    alias: String,
    power_level: i32,
    infinity_stone_status: String,
    stone_secured: bool,
    snap_viable: bool,
}

#[tokio::main]
async fn main() {
    tracing_subscriber::registry()
        .with(tracing_subscriber::EnvFilter::try_from_default_env().unwrap_or_else(|_| "info".into()))
        .with(tracing_subscriber::fmt::layer())
        .init();

    let base_url = std::env::var("AVENGERS_HQ_URL")
        .unwrap_or_else(|_| "http://localhost:8080".into());
    let state = AppState {
        client: AvengersClient::new(base_url),
    };

    let app = Router::new()
        .route("/targets/{id}", get(assess_target))
        .layer(TraceLayer::new_for_http())
        .with_state(state);

    let addr = SocketAddr::from(([0, 0, 0, 0], 3000));
    tracing::info!("Thanos Gauntlet listening on {addr}");
    let listener = tokio::net::TcpListener::bind(addr).await.expect("bind");
    axum::serve(listener, app).await.expect("serve");
}

async fn assess_target(
    State(state): State<AppState>,
    Path(id): Path<u64>,
) -> impl IntoResponse {
    match state.client.get_hero(id).await {
        Ok(hero) => {
            let stone_secured = hero.infinity_stone_status == "SECURED";
            let report = TargetReport {
                snap_viable: hero.power_level < 100 && hero.infinity_stone_status != "COMPROMISED",
                hero_id: hero.id,
                alias: hero.alias,
                power_level: hero.power_level,
                infinity_stone_status: hero.infinity_stone_status,
                stone_secured,
            };
            (StatusCode::OK, Json(report)).into_response()
        }
        Err(GauntletError::NotFound) => StatusCode::NOT_FOUND.into_response(),
        Err(err) => (
            StatusCode::BAD_GATEWAY,
            format!("Avengers HQ unreachable or contract mismatch: {err}"),
        )
            .into_response(),
    }
}
