import moe.nikky.counter.CounterExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import plugin.ConstantsExtension
import plugin.GenerateConstantsTask

plugins {
    kotlin("jvm") version Constants.Kotlin.version
    kotlin("plugin.scripting") version Constants.Kotlin.version
    id("moe.nikky.persistentCounter") version "0.0.7-SNAPSHOT"
    constantsGenerator apply false
//    id("com.github.johnrengelman.shadow") version Constants.ShadowJar.version apply false
    `maven-publish`
    idea
    wrapper
}

val wrapper = tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = Constants.Gradle.version
    distributionType = Constants.Gradle.distributionType
}

val runnableProjects = mapOf(
    project("core") to "knote.Main",
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
            "KNOTE_VERSION" to Constants.KNote.version,
            "GRADLE_VERSION" to Constants.Gradle.version
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
                useVersion(Constants.Kotlin.version)
                because("pin to kotlin version ${Constants.Kotlin.version}")
            }
        }
    }

    counter {
        variable(id = "buildnumber", key = "${Constants.KNote.version}${Env.branch}")
    }
    val counter: CounterExtension = extensions.getByType()
    val buildnumber by counter.map

    val versionSuffix = if (Env.isCI) "-$buildnumber" else "-dev"

    val fullVersion = "${Constants.KNote.version}$versionSuffix"

    version = fullVersion


    apply {
        plugin("constantsGenerator")
    }
    val folder = listOf("knote") + project.name.split('-')

    configure<ConstantsExtension> {
        constantsObject(
            pkg = folder.joinToString("."),
            className = project.name
                .split("-")
                .joinToString("") {
                    it.capitalize()
                } + "Constants"
        ) {
            //            field("JENKINS_URL") value Jenkins.url
//            field("JENKINS_JOB") value Jenkins.job
//            field("JENKINS_BUILD_NUMBER") value (System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: -1)
            field("GRADLE_VERSION") value Constants.Gradle.version
            field("KOTLIN_VERSION") value Constants.Kotlin.version
//            field("BUILD") value versionSuffix
            field("VERSION") value Constants.KNote.version
            field("FULL_VERSION") value fullVersion
            field("BUILD_NUMBER") value buildnumber
        }
    }

    val generateConstants by tasks.getting(GenerateConstantsTask::class) {
        kotlin.sourceSets["main"].kotlin.srcDir(outputFolder)
    }

    tasks.withType<KotlinCompile> {
        dependsOn(generateConstants)
    }

    runnableProjects[project]?.let { mainClass ->
        //        apply<ApplicationPlugin>()
        apply {
            plugin("application")
//            plugin("com.github.johnrengelman.shadow")
        }

        configure<JavaApplication> {
            mainClassName = mainClass
        }

//        val runDir = rootProject.file("run")
//
//        val run by tasks.getting(JavaExec::class) {
//            workingDir = runDir
//        }
//
//        val runShadow by tasks.getting(JavaExec::class) {
//            workingDir = runDir
//        }

//        val shadowJar by tasks.getting(ShadowJar::class) {
//            archiveClassifier.set("")
//        }

//        val build by tasks.getting(Task::class) {
//            dependsOn(shadowJar)
//        }
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
