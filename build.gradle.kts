import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import moe.nikky.counter.CounterExtension

plugins {
    kotlin("jvm") version Kotlin.version
    kotlin("plugin.scripting") version Kotlin.version
    id("moe.nikky.persistentCounter") version "0.0.7-SNAPSHOT"
    id("com.github.johnrengelman.shadow") version "4.0.0" apply false
    application
    `maven-publish`
    idea
    wrapper
}

val wrapper = tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = Gradle.version
    distributionType = Gradle.distributionType
}

val runnableProjects = mapOf(
    project("core") to "knote.MainKt",
    project("tornadofx-viewer") to "knote.tornadofx.ViewApp"
)
allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }

    group = "daemoniclabs.knote${Env.branch}"

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
}

tasks.create<Copy>("processMDTemplates") {
    group = "documentation"
    from(rootDir)
    include("**/*.template_md")
    filesMatching("**/*.template_md") {
        name = this.sourceName.substringBeforeLast(".template_md") + ".md"
        expand(
            "KNOTE_VERSION" to KNote.version,
            "GRADLE_VERSION" to Gradle.version
        )
    }
    destinationDir = rootDir

    idea {
        module {
            excludeDirs.add(file("run"))
        }
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("moe.nikky.persistentCounter")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            apiVersion = "1.3"
            languageVersion = "1.3"
            jvmTarget = "1.8"
        }
    }

    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(Kotlin.version)
                because("pin to kotlin version ${Kotlin.version}")
            }
        }
    }

    counter {
        variable(id = "buildnumber", key = "${KNote.version}${Env.branch}")
    }
    val counter: CounterExtension = extensions.getByType()
    val buildnumber by counter.map

    val versionSuffix = if (Env.isCI) "-$buildnumber" else "-dev"

    val fullVersion = "${KNote.version}$versionSuffix"

    version = fullVersion

    runnableProjects[project]?.let { mainClass ->
        apply<ApplicationPlugin>()

        configure<JavaApplication> {
            mainClassName = mainClass
        }

        apply(plugin = "com.github.johnrengelman.shadow")

//        val runDir = rootProject.file("run")
//
//        val run by tasks.getting(JavaExec::class) {
//            workingDir = runDir
//        }
//
//        val runShadow by tasks.getting(JavaExec::class) {
//            workingDir = runDir
//        }

        val shadowJar by tasks.getting(ShadowJar::class) {
            archiveClassifier.set("")
        }

        val build by tasks.getting(Task::class) {
            dependsOn(shadowJar)
        }
    }

    apply(plugin = "maven-publish")

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
//                groupId = project.group
//                artifactId = project.name
//                version = "1.0-SNAPSHOT"

                from(components["java"])
                artifact(sourcesJar.get())
                artifact(javadocJar.get())
            }
        }
        repositories {
            maven(url = "http://mavenupload.modmuss50.me/") {
                val mavenPass: String? = project.properties["mavenPass"] as String?
                mavenPass?.let {
                    credentials {
                        username = "buildslave"
                        password = mavenPass
                    }
                }
            }
        }
    }
}
