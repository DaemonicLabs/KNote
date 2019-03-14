import org.gradle.api.tasks.wrapper.Wrapper

object Constants {
    object Gradle {
        const val version = "5.2.1"
        val distributionType = Wrapper.DistributionType.ALL
    }

    object Kotlin {
        const val version = "1.3.21"
    }

    object Coroutines {
        const val version = "1.1.1"
        const val dependency = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    }

    object Html {
        const val version = "0.6.12"
        const val dependency = "org.jetbrains.kotlinx:kotlinx-html-jvm:$version"
    }

    object KotlinLogging {
        const val version = "1.6.24"
        const val dependency = "io.github.microutils:kotlin-logging:$version"
    }

    object LogbackClassic {
        const val version = "1.3.0-alpha4"
        const val dependency = "ch.qos.logback:logback-classic:$version"
    }

    object KotlinPoet {
        const val version = "1.1.0"
        const val dependency = "com.squareup:kotlinpoet:$version"
    }

    object TornadoFX {
        const val version = "1.7.18"
        const val dependency = "no.tornado:tornadofx:$version"
    }

    object ShadowJar {
        const val version = "4.0.4"
        const val dependency = "com.github.jengelman.gradle.plugins:shadow:$version"
    }

    object RichTextFX {
        const val version = "0.9.3"
        const val dependency = "org.fxmisc.richtext:richtextfx:$version"
    }

    object KastreeCommon {
        const val version = "0.4.0"
        const val dependency = "com.github.cretz.kastree:kastree-ast-common:$version"
    }

    object KastreeJvm {
        const val version = "0.4.0"
        const val dependency = "com.github.cretz.kastree:kastree-ast-jvm:$version"
    }

    object KastreePsi {
        const val version = "0.4.0"
        const val dependency = "com.github.cretz.kastree:kastree-ast-psi:$version"
    }


    object KNote {
        const val major = 1
        const val minor = 0
        const val patch = 0
        const val version = "$major.$minor.$patch"
    }
}

