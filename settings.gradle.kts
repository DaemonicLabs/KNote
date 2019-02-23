pluginManagement {
    repositories {
        maven(url = "https://kotlin.bintray.com/kotlinx/") {
            name = "kotlinx"
        }
        maven(url = "http://maven.modmuss50.me") {
            name = "modmuss50"
        }
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}
rootProject.name = "KNote"

include("core")
include("gradle-plugin")
include("tornadofx-viewer")