plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.spring") version "2.2.10"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.avengers"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.9")
    implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.30")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.register<JavaExec>("generateOpenApi") {
    group = "documentation"
    description = "Generate the service-owned OpenAPI contract"
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("io.avengers.hq.GenerateOpenApiKt")
    args(providers.gradleProperty("openApiOutput").orElse("../build/generated-contracts/heroes.yaml").get())
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
