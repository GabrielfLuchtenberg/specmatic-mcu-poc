# syntax=docker/dockerfile:1.7
ARG RUST_IMAGE=rust:1.88.0-bookworm
ARG DEBIAN_IMAGE=debian:bookworm-20250811-slim

FROM ${RUST_IMAGE} AS builder
WORKDIR /workspace
COPY Cargo.toml Cargo.lock ./
COPY src ./src
RUN cargo fetch --locked
COPY tests ./tests

FROM builder AS unit-test
CMD ["cargo", "test", "--locked"]

FROM builder AS contract-test
ENV AVENGERS_HQ_URL=http://specmatic-mock:9000
CMD ["cargo", "test", "--locked", "--test", "contract_client", "--", "--ignored"]

FROM builder AS release
RUN --mount=type=cache,target=/usr/local/cargo/registry \
    cargo build --locked --release

FROM ${DEBIAN_IMAGE} AS runtime
RUN useradd --system --uid 10001 --create-home app
COPY --from=release /workspace/target/release/thanos-gauntlet /usr/local/bin/thanos-gauntlet
USER app
EXPOSE 3000
ENTRYPOINT ["thanos-gauntlet"]
