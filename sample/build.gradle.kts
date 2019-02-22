import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import knote.util.Platform
import org.gradle.api.internal.AbstractTask
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors

plugins {
    kotlin("jvm") version Kotlin.version
    kotlin("plugin.scripting") version Kotlin.version
    idea
//    application
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

dependencies {
    implementation(kotlin("stdlib", Kotlin.version))
//    add("knote", kotlin("stdlib", Kotlin.version))

    // TODO: add to plugin
    implementation(group = "daemoniclabs", name = "knote", version = "1.0-SNAPSHOT")
    implementation(group = "org.jetbrains.kotlinx", name ="kotlinx-html-jvm", version = "0.6.12")
    add(knoteConfiguration.name, "org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.12")
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


//val copyLibs = task<AbstractTask>("copyVoodooLibs") {
//    group = "build"
//    doFirst {
//        libs.deleteRecursively()
//        libs.mkdirs()
//        for (file in knoteConfiguration.resolve()) {
//            file.copyTo(libs.resolve(file.name))
//        }
//    }
//}

val hostRoot = rootDir.absoluteFile.parentFile

val ideaActive = System.getProperty("idea.active") == "true"

if(ideaActive) {
    class StreamGobbler(private val inputStream: InputStream, private val consumer: (String) -> Unit) :
        Runnable {

        override fun run() {
            BufferedReader(InputStreamReader(inputStream)).forEachLine(consumer)
        }
    }
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
    val outStreamGobbler = StreamGobbler(process.inputStream) { line -> logger.lifecycle("% $line") }
    val errStreamGobbler = StreamGobbler(process.errorStream) { line -> logger.error("% $line") }
    val f1 = Executors.newSingleThreadExecutor().submit(outStreamGobbler)
    val f2 = Executors.newSingleThreadExecutor().submit(errStreamGobbler)
    val result = process.waitFor()
    logger.lifecycle("command finished with code: $result")
}

val jarFile = rootDir
    .resolve("build").resolve(".knote-lib")
    .resolve("KNote.jar")
val buildHost = task<GradleBuild>("buildHost") {
    tasks = listOf("shadowJar")
    dir = hostRoot
    buildFile = hostRoot.resolve("build.gradle.kts")
    doLast {
        hostRoot
            .resolve("build").resolve("libs")
            .resolve("KNote.jar")
            .copyTo(jarFile, overwrite = true)
    }
}


val pagesDir = rootDir.resolve("pages").apply { mkdirs() }
val notebookDir = rootDir.resolve("notebooks").apply { mkdirs() }

//val runDir = rootDir.resolve("run").apply { mkdirs() }

notebookDir
    .listFiles { _, name -> name.endsWith(".notebook.kts") }
    .forEach { scriptFile ->
        val id = scriptFile.name.substringBeforeLast(".notebook.kts")
        task<JavaExec>("run_$id") {
            dependsOn(buildHost)
//            dependsOn(copyLibs)
            group = "application"
            args = listOf(id)
            workingDir = rootDir
            main = "knote.MainKt"
            classpath(jarFile)
            doFirst {
                logger.lifecycle("executing")
                logger.lifecycle("java -jar ${jarFile.path} ${(args as List<String>).joinToString(" ")}")
                logger.lifecycle("\n")
            }
        }
        task<JavaExec>("runViewer_$id") {
            dependsOn(buildHost)
//    dependsOn(copyLibs)
            group = "application"
            args = listOf(id)
            main = "knote.tornadofx.ViewerApp"
            workingDir = rootDir
            classpath(jarFile)
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
