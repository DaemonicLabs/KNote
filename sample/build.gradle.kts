import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib", Kotlin.version))
//    api(files(shadowJar.archiveFile))
//    implementation(project("host"))

    // TODO: add to plugin
    implementation(group = "daemoniclabs", name = "knote", version = "1.0-SNAPSHOT")
}

//val shadowJar = tasks.getByPath("host:shadowJar") as ShadowJar

val hostRoot = rootDir.absoluteFile.parentFile

val buildHost = task<GradleBuild>("buildHost") {
    tasks = listOf("publishToMavenLocal", "shadowJar")
    dir = hostRoot
    buildFile = hostRoot.resolve("build.gradle.kts")
}

val jarFile = hostRoot
    .resolve("build").resolve("libs")
    .resolve("KNote.jar")

val pagesDir = rootDir.resolve("pages").apply { mkdirs() }
val notebookDir = rootDir.resolve("notebooks").apply { mkdirs() }

val runDir = rootDir.resolve("run").apply { mkdirs() }

notebookDir
    .listFiles { _, name -> name.endsWith(".notebook.kts") }
    .forEach { scriptFile ->
        val id = scriptFile.name.substringBeforeLast(".knote.kts")
        task<JavaExec>("run_$id") {
            dependsOn(buildHost)
            group = "application"
            args = listOf(scriptFile.name)
            workingDir = runDir
            main = "knote.MainKt"
            classpath(jarFile)
            doFirst {
                logger.lifecycle("executing")
                logger.lifecycle("java -jar ${jarFile.path} ${(args as List<String>).joinToString(" ")}")
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