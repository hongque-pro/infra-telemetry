

plugins {
    id("com.gorylenko.gradle-git-properties")
}

dependencies {
    api("com.labijie.infra:commons-core-starter:${Versions.infraCommons}")

    api("io.opentelemetry:opentelemetry-sdk:${Versions.openTelemetry}")
    api("io.opentelemetry:opentelemetry-api:${Versions.openTelemetry}")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp-common:${Versions.openTelemetry}")
    implementation("org.slf4j:jul-to-slf4j")

    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation ("org.springframework.boot:spring-boot-starter-actuator")

    compileOnly("org.apache.kafka:kafka-clients")
    compileOnly("org.springframework.security:spring-security-web")
    compileOnly("org.springframework.security:spring-security-config")
}


gitProperties {
    // Customize file name (could be a file name or a relative file path below gitPropertiesResourceDir dir)
    gitPropertiesName = "telemetry-git.properties"
}