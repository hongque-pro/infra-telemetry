plugins {
    id("com.labijie.infra") version(Versions.infraPlugin)
    id("com.gorylenko.gradle-git-properties") version(Versions.gitProperties) apply false
}

allprojects {
    group = "com.labijie.infra"
    version = "1.2.1"

    infra {
        useDefault {
            includeSource = true
            infraBomVersion = Versions.infraBom
            kotlinVersion = Versions.kotlin
            useMavenProxy = true
            useMavenProxy = false
        }

        useNexusPublish()
    }
}

subprojects {
    if (!this.name.startsWith("dummy")) {
        infra {
            usePublish {
                description = "telemetry library base on OpenTelemetry"
                githubUrl("hongque-pro", "infra-telemetry")
                artifactId {
                    "telemetry-${it.name}"
                }
            }
        }
    }
}

