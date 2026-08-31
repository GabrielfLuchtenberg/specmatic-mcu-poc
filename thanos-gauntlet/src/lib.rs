use reqwest::StatusCode;
use serde::Deserialize;
use thiserror::Error;

#[derive(Debug, Clone, Deserialize, PartialEq, Eq)]
#[serde(rename_all = "camelCase")]
pub struct Hero {
    pub id: u64,
    pub name: String,
    pub alias: String,
    pub power_level: i32,
    pub infinity_stone_status: String,
    pub location: Option<String>,
}

#[derive(Debug, Clone, Deserialize, PartialEq, Eq)]
#[serde(rename_all = "camelCase")]
pub struct PowerReport {
    pub hero_id: u64,
    pub alias: String,
    pub power_level: i32,
    pub snap_viable: bool,
}

#[derive(Debug, Error)]
pub enum GauntletError {
    #[error("hero not found")]
    NotFound,
    #[error("http error: {0}")]
    Http(#[from] reqwest::Error),
    #[error("unexpected status {0}")]
    UnexpectedStatus(StatusCode),
}

#[derive(Clone)]
pub struct AvengersClient {
    http: reqwest::Client,
    base_url: String,
}

impl AvengersClient {
    pub fn new(base_url: impl Into<String>) -> Self {
        Self {
            http: reqwest::Client::new(),
            base_url: base_url.into().trim_end_matches('/').to_string(),
        }
    }

    pub async fn get_hero(&self, id: u64) -> Result<Hero, GauntletError> {
        let response = self
            .http
            .get(format!("{}/heroes/{id}", self.base_url))
            .send()
            .await?;

        match response.status() {
            StatusCode::OK => Ok(response.json::<Hero>().await?),
            StatusCode::NOT_FOUND => Err(GauntletError::NotFound),
            other => Err(GauntletError::UnexpectedStatus(other)),
        }
    }

    pub async fn get_power_report(&self, id: u64) -> Result<PowerReport, GauntletError> {
        let response = self.http.get(format!("{}/heroes/{id}/power-report", self.base_url)).send().await?;
        match response.status() {
            StatusCode::OK => Ok(response.json::<PowerReport>().await?),
            StatusCode::NOT_FOUND => Err(GauntletError::NotFound),
            other => Err(GauntletError::UnexpectedStatus(other)),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::Hero;

    #[test]
    fn deserializes_the_contract_field_names() {
        let hero: Hero = serde_json::from_str(
            r#"{
                "id": 1,
                "name": "Tony Stark",
                "alias": "Iron Man",
                "powerLevel": 95,
                "infinityStoneStatus": "SECURED",
                "location": "Avengers Tower"
            }"#,
        )
        .expect("the documented Hero example must deserialize");

        assert_eq!(hero.power_level, 95);
        assert_eq!(hero.infinity_stone_status, "SECURED");
    }

    #[test]
    fn rejects_a_renamed_required_contract_field() {
        let result = serde_json::from_str::<Hero>(
            r#"{
                "id": 1,
                "name": "Tony Stark",
                "alias": "Iron Man",
                "strength": 95,
                "infinityStoneStatus": "SECURED"
            }"#,
        );

        assert!(result.is_err(), "powerLevel is required by the contract");
    }
}
