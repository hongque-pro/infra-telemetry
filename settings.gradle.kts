rootProject.name = "infra-telemetry"

include("core-starter")
include("dummy-server")



pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
