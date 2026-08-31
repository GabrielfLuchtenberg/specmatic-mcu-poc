use std::{env, fs, path::PathBuf};
use thanos_gauntlet::provider_openapi::ProviderApi;
use utoipa::OpenApi;

fn main() {
    let output = env::args().nth(1).expect("usage: generate-openapi <output-file>");
    let output = PathBuf::from(output);
    if let Some(parent) = output.parent() {
        fs::create_dir_all(parent).expect("create output directory");
    }
    fs::write(output, ProviderApi::openapi().to_yaml().expect("serialize OpenAPI"))
        .expect("write OpenAPI contract");
}
