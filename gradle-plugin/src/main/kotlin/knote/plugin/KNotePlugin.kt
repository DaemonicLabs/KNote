package knote.plugin

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import knote.gradle.plugin.GradlePluginConstants
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.AbstractTask
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.task
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

open class KNotePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val knoteExtension = project.run {
            pluginManager.apply("org.gradle.idea")
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("org.jetbrains.kotlin.plugin.scripting")

            extensions.create<KNoteExtension>("knote", project)
        }

        val implementation = project.configurations.getByName("implementation")
        val knoteConfiguration = project.configurations.create("knote")
        knoteConfiguration.extendsFrom(implementation)

        val shadowCoreConfiguration = project.configurations.create("knote-shadow-core"){
            extendsFrom(implementation)
            isVisible = false
        }

        val shadowViewerConfiguration = project.configurations.create("knote-shadow-viewer") {
            extendsFrom(shadowCoreConfiguration)
            isVisible = false
        }


        project.dependencies {
            add(
                configurationName = shadowCoreConfiguration.name,
                dependencyNotation = create(
                    group = "daemoniclabs.knote",
                    name = "core",
                    version = GradlePluginConstants.FULL_VERSION
                )
            )
            add(
                configurationName = shadowViewerConfiguration.name,
                dependencyNotation = create(
                    group = "daemoniclabs.knote",
                    name = "tornadofx-viewer",
                    version = GradlePluginConstants.FULL_VERSION
                )
            )
        }

        val shadowCore = project.tasks.create<ShadowJar>("shadowCore") {
            group = "shadow"
            archiveBaseName.set("core")
            configurations = listOf(shadowCoreConfiguration)
        }
        val shadowViewer =  project.tasks.create<ShadowJar>("shadowViewer") {
            group = "shadow"
            archiveBaseName.set("tornadofx-viewer")
            configurations = listOf(shadowViewerConfiguration)
        }

        val libs = project.rootDir.resolve("libs")
        val generatedSrc = project.rootDir.resolve("build").resolve(".knote")

        val copyLibs = project.task<AbstractTask>("copyLibs") {
            group = "build"
            doFirst {
                libs.deleteRecursively()
                libs.mkdirs()
                for (file in knoteConfiguration.resolve()) {
                    file.copyTo(libs.resolve(file.name))
                }
            }
        }

        val notebookDir = project.rootDir.resolve("notebooks").apply { mkdirs() }

        project.afterEvaluate {
            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }

            extensions.configure<KotlinJvmProjectExtension> {
                sourceSets.maybeCreate("main").kotlin.apply {
                    srcDir(notebookDir)
                    srcDir(generatedSrc)
                }
            }

            extensions.configure<IdeaModel> {
                module {
                    generatedSourceDirs.add(generatedSrc)
                }
            }

            // TODO: loop through registered notebooks (in extension)



            notebookDir
                .listFiles { _, name -> name.endsWith(".notebook.kts") }
                .forEach { scriptFile ->
                    val id = scriptFile.name.substringBeforeLast(".notebook.kts")
                    task<JavaExec>("run_$id") {
                        dependsOn(shadowCore)
                        dependsOn(copyLibs)
                        val jarFile = shadowCore.archiveFile.get()
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
                        dependsOn(copyLibs)
                        val jarFile = shadowCore.archiveFile.get()
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
        }
    }

}