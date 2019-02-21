import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Kotlin.version
    kotlin("plugin.scripting") version Kotlin.version
    id("com.github.johnrengelman.shadow") version "4.0.0"
    application
    `maven-publish`
}


repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib", Kotlin.version))

    // script definition
    implementation(kotlin("scripting-jvm", Kotlin.version))

    // host
    implementation(kotlin("script-util", Kotlin.version))
    implementation(kotlin("scripting-jvm-host-embeddable", Kotlin.version))


    implementation(kotlin("compiler-embeddable", Kotlin.version))

    // not strictly necessary
    implementation(kotlin("reflect", Kotlin.version))

    implementation("com.squareup:kotlinpoet:1.0.1")
}

application {
    mainClassName = "knote.MainKt"
}

val shadowJar by tasks.getting(ShadowJar::class) {
    archiveClassifier.set("")
}

task<DefaultTask>("depsize") {
    group = "help"
    description = "prints dependency sizes"
    doLast {
        val formatStr = "%,10.2f"
        val size = configurations.default.get().resolve()
            .map { it.length() / (1024.0 * 1024.0) }.sum()

        val out = buildString {
            append("Total dependencies size:".padEnd(45))
            append("${String.format(formatStr, size)} Mb\n\n")
            configurations
                .default
                .get()
                .resolve()
                .sortedWith(compareBy { -it.length() })
                .forEach {
                    append(it.name.padEnd(45))
                    append("${String.format(formatStr, (it.length() / 1024.0))} kb\n")
                }
        }
        println(out)
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val javadoc by tasks.getting(Javadoc::class) {}
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(javadoc)
}

publishing {
    publications {

        create("default", MavenPublication::class.java) {
            groupId = "daemoniclabs"
            artifactId = "knote"
            version = "1.0-SNAPSHOT"

            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
        }
    }
}