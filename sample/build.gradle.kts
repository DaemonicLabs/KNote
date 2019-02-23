import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import knote.util.Platform
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.concurrent.Executors

plugins {
    kotlin("jvm") version Kotlin.version
    kotlin("plugin.scripting") version Kotlin.version
    id("com.github.johnrengelman.shadow") version "4.0.0"
    idea
//    application
}

val wrapper = tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = Gradle.version
    distributionType = Gradle.distributionType
}

allprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            apiVersion = "1.3"
            languageVersion = "1.3"
            jvmTarget = "1.8"
        }
    }

}

repositories {
    mavenLocal()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx-html/") {
        name = "Kotlinx"
    }
    mavenCentral()
    jcenter()
}

val knoteConfiguration = project.configurations.create("knote")
knoteConfiguration.extendsFrom(configurations.implementation.get())

val shadowCoreConfiguration = configurations.create("shadow-core")
shadowCoreConfiguration.extendsFrom(configurations.implementation.get())

val shadowViewerConfiguration = configurations.create("shadow-viewer")
shadowViewerConfiguration.extendsFrom(shadowCoreConfiguration)

dependencies {
    fun add(
        configuration: Configuration,
        group: String,
        name: String, version: String
    ) = add(
        configurationName = configuration.name,
        dependencyNotation = "$group:$name:$version"
    )


    implementation(kotlin("stdlib", Kotlin.version))
//    add("knote", kotlin("stdlib", Kotlin.version))

    // TODO: add to plugin
    add(
        configuration = shadowCoreConfiguration,
        group = "daemoniclabs.knote",
        name = "core",
        version = "1.0.0-dev"
    )
    add(
        configuration = shadowViewerConfiguration,
        group = "daemoniclabs.knote",
        name = "tornadofx-viewer",
        version = "1.0.0-dev"
    )

//    implementation(group = "org.jetbrains.kotlinx", name ="kotlinx-html-jvm", version = "0.6.12")
    add(
        configuration = knoteConfiguration,
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-html-jvm",
        version = "0.6.12"
    )
}

val libs = rootDir.resolve("libs")

afterEvaluate {
    libs.deleteRecursively()
    libs.mkdirs()
    val resolvedFiles = knoteConfiguration.resolve()
    logger.lifecycle("resolved files: $resolvedFiles")
    for (file in resolvedFiles) {
        file.copyTo(libs.resolve(file.name), overwrite = true)
    }
}

val ideaActive = System.getProperty("idea.active") == "true"

if (ideaActive) {
    val gradleWrapper = when {
        Platform.isWindows -> "gradlew"
        Platform.isLinux -> "./gradlew"
        Platform.isMac -> "./gradlew"
        else -> throw IllegalStateException("unsupported OS: ${Platform.osType}")
    }
    val cmd = arrayOf(gradleWrapper, "publishToMavenLocal")
    logger.lifecycle("executing ${cmd.joinToString(" ", "[", "]")}")
    val command = ProcessBuilder(*cmd)
    val process = command
        .directory(hostRoot)
        .start()
    val outStreamGobbler = Runnable {
        process.inputStream.bufferedReader().use {
            it.forEachLine { line -> logger.lifecycle("% $line") }
        }
    }
    val errStreamGobbler = Runnable {
        process.errorStream.bufferedReader().use {
            it.forEachLine { line -> logger.error("% $line") }
        }
    }
    Executors.newSingleThreadExecutor().submit(outStreamGobbler)
    Executors.newSingleThreadExecutor().submit(errStreamGobbler)
    val result = process.waitFor()
    logger.lifecycle("command finished with code: $result")
}

val hostRoot = rootDir.absoluteFile.parentFile

val publishHost = task<GradleBuild>("publishHost") {
    tasks = listOf("publishToMavenLocal")
    dir = hostRoot
    buildFile = hostRoot.resolve("build.gradle.kts")
}

val shadowCore = tasks.create<ShadowJar>("shadowCore") {
    dependsOn += publishHost
    group = "shadow"
    archiveBaseName.set("core")
    configurations = listOf(shadowCoreConfiguration)
}
val shadowViewer = tasks.create<ShadowJar>("shadowViewer") {
    dependsOn += publishHost
    group = "shadow"
    archiveBaseName.set("tornadofx-viewer")
    configurations = listOf(shadowViewerConfiguration)
}

val pagesDir = rootDir.resolve("pages").apply { mkdirs() }
val notebookDir = rootDir.resolve("notebooks").apply { mkdirs() }

//val runDir = rootDir.resolve("run").apply { mkdirs() }

notebookDir
    .listFiles { _, name -> name.endsWith(".notebook.kts") }
    .forEach { scriptFile ->
        val id = scriptFile.name.substringBeforeLast(".notebook.kts")
        task<JavaExec>("run_$id") {
            dependsOn(shadowCore)
            val jarFile = shadowCore.archiveFile.get()
//            dependsOn(copyLibs)
            group = "application"
            args = listOf(id)
            workingDir = rootDir
            main = "knote.MainKt"
            classpath(shadowCore.archiveFile)
            doFirst {
                logger.lifecycle("executing")
                logger.lifecycle("java -jar ${jarFile} ${(args as List<String>).joinToString(" ")}")
                logger.lifecycle("\n")
            }
        }
        task<JavaExec>("runViewer_$id") {
            dependsOn(shadowViewer)
            val jarFile = shadowCore.archiveFile.get()
//            dependsOn(copyLibs)
            group = "application"
            args = listOf(id)
            main = "knote.tornadofx.ViewerApp"
            workingDir = rootDir
            classpath(shadowViewer.archiveFile)
            doFirst {
                logger.lifecycle("executing")
                logger.lifecycle("java -jar ${jarFile} ${(args as List<String>).joinToString(" ")}")
                logger.lifecycle("\n")
            }
        }
    }

//TODO: move to gradle plugin
val generatedSrc = rootDir.resolve("build").resolve(".knote")

kotlin {
    sourceSets.maybeCreate("main").kotlin.apply {
        srcDir(pagesDir)
        srcDir(notebookDir)
        srcDir(generatedSrc)
    }
}

//idea {
//    module {
//        generatedSourceDirs.add(generatedSrc)
//    }
//}

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
