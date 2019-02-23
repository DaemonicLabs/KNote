import org.gradle.api.tasks.wrapper.Wrapper

object Gradle {
    const val version = "5.2.1"
    val distributionType = Wrapper.DistributionType.BIN
}

object Kotlin {
    const val version = "1.3.21"
}

object KotlinPoet {
    const val version = "1.0.1"
    const val dependency = "com.squareup:kotlinpoet:$version"
}

object TornadoFX {
    const val version = "1.7.18"
    const val dependency = "no.tornado:tornadofx:$version"
}

object KNote {
    const val major = 1
    const val minor = 0
    const val patch = 0
    const val version = "$major.$minor.$patch"
}
