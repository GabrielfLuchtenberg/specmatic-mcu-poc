# syntax=docker/dockerfile:1.7
ARG GRADLE_IMAGE=gradle:8.14.3-jdk21-jammy
ARG JRE_IMAGE=eclipse-temurin:21.0.8_9-jre-jammy

FROM ${GRADLE_IMAGE} AS builder
WORKDIR /workspace
COPY --chown=gradle:gradle gradlew gradle.properties settings.gradle.kts build.gradle.kts ./
COPY --chown=gradle:gradle gradle ./gradle
RUN --mount=type=cache,target=/home/gradle/.gradle,uid=1000,gid=1000 \
    ./gradlew dependencies --no-daemon
COPY --chown=gradle:gradle src ./src
RUN --mount=type=cache,target=/home/gradle/.gradle,uid=1000,gid=1000 \
    ./gradlew bootJar --no-daemon

FROM ${JRE_IMAGE} AS runtime
RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
RUN useradd --system --uid 10001 --create-home app
WORKDIR /app
COPY --from=builder /workspace/build/libs/avengers-hq-1.0.0.jar /app/app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
