package knote.plugin

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import knote.gradle.plugin.GradlePluginConstants
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.task
import org.gradle.plugins.ide.idea.model.IdeaModel
import java.io.File

open class KNotePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.logger.lifecycle("KNote version ${GradlePluginConstants.FULL_VERSION}")
        val knoteExtension = project.run {
            pluginManager.apply("org.gradle.idea")
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("org.jetbrains.kotlin.plugin.scripting")

            extensions.create<KNoteExtension>("knote", project)
        }

        val implementation = project.configurations.getByName("implementation")

        val knoteConfiguration = project.configurations.create("knote")
        val knoteFXConfiguration = project.configurations.create("knoteFx") {
            //            extendsFrom(knoteConfiguration)
        }
//        implementation.extendsFrom(knoteConfiguration)

        project.repositories {
            maven(url = "http://maven.modmuss50.me") {
                name = "modmuss50"
            }
        }
        project.dependencies {
            add(
                configurationName = knoteConfiguration.name,
                dependencyNotation = create(
                    group = "daemoniclabs.knote",
                    name = "core",
                    version = GradlePluginConstants.FULL_VERSION
                )
            )
            add(
                configurationName = knoteFXConfiguration.name,
                dependencyNotation = create(
                    group = "daemoniclabs.knote",
                    name = "tornadofx-viewer",
                    version = GradlePluginConstants.FULL_VERSION
                )
            )
            if (knote.util.Platform.isWindows) {
                // Windows required jansi for logging
                add(
                    configurationName = knoteConfiguration.name,
                    dependencyNotation = create(
                        group = "org.fusesource.jansi",
                        name = "jansi",
                        version = "1.9"
                    )
                )
            }
        }

        val notebookDir = project.rootDir.resolve("notebooks").apply { mkdirs() }

        val sourceSets = project.extensions.getByName<SourceSetContainer>("sourceSets")
        val main = sourceSets.getByName("main")

        val shadowCore = project.tasks.create<ShadowJar>("shadowCore") {
            group = "shadow"
            from(main.output)
            archiveBaseName.set("core")
            configurations = listOf(knoteConfiguration)
            manifest {
                attributes(
                    mapOf(
                        "Main-Class" to "knote.Main",
                        "version" to project.version
                    )
                )
            }
        }
        val shadowViewer = project.tasks.create<ShadowJar>("shadowViewer") {
            group = "shadow"
            from(main.output)
            archiveBaseName.set("tornadofx-viewer")
            configurations = listOf(knoteFXConfiguration)
            manifest {
                attributes(
                    mapOf(
                        "Main-Class" to "knote.tornadofx.ViewerApp",
                        "version" to project.version
                    )
                )
            }
        }


        project.afterEvaluate {
            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }


            dependencies {
                knoteConfiguration.resolvedConfiguration.firstLevelModuleDependencies.forEach {
                    logger.lifecycle("adding to implementation: $it")
                    add(
                        implementation.name, create(
                            it.moduleGroup,
                            it.moduleName,
                            it.moduleVersion
                        )
                    )
                }
                knoteFXConfiguration.resolvedConfiguration.firstLevelModuleDependencies.forEach {
                    logger.lifecycle("adding to implementation: $it")
                    add(
                        implementation.name, create(
                            it.moduleGroup,
                            it.moduleName,
                            it.moduleVersion
                        )
                    )
                }
            }

            // TODO: loop through registered notebooks (in extension ?)

            notebookDir
                .listFiles { _, name -> name.endsWith(".notebook.kts") }
                .forEach { scriptFile ->
                    val id = scriptFile.name.substringBeforeLast(".notebook.kts")

                    val generatedSrc = project.rootDir.resolve("build").resolve(".knote").resolve(id).apply { mkdirs() }
                    val pagesSrc = rootDir.resolve("${id}_pages").apply { mkdirs() }
                    val pages = pagesSrc.listFiles { file -> file.isFile && file.name.endsWith(".page.kts") } ?: run {
                        logger.error("no files found in $pagesSrc")
                        arrayOf<File>()
                    }
//                    PageMarker.generate(generatedSrc, pages, fileName = id.capitalize())

//                    extensions.configure<IdeaModel> {
//                        module {
//                            sourceDirs.add(pagesSrc)
//                            sourceDirs.add(generatedSrc)
//                            generatedSourceDirs.add(generatedSrc)
//                        }
//                    }
//                    extensions.configure<KotlinJvmProjectExtension> {
//                        this.sourceSets.maybeCreate("main_script").apply {
//                            kotlin.srcDir(pagesSrc)
//                            kotlin.srcDir(generatedSrc)
//                             dependsOn(sourceSets.getByName("main"))
//                        }
//                    }

                    task<JavaExec>("run_$id") {
                        dependsOn(shadowCore)
//                        dependsOn(copyLibs)
                        val jarFile = shadowCore.archiveFile.get()
                        group = "application"
                        args = listOf(id)
                        workingDir = rootDir
                        this.main = "knote.Main"
                        classpath(shadowCore.archiveFile)
                        doFirst {
                            logger.lifecycle("executing")
                            logger.lifecycle("java -jar ${jarFile} ${(args as List<String>).joinToString(" ")}")
                            logger.lifecycle("\n")
                        }
                    }
                    task<JavaExec>("runViewer_$id") {
                        dependsOn(shadowViewer)
//                        dependsOn(copyLibs)
                        val jarFile = shadowCore.archiveFile.get()
                        group = "application"
                        args = listOf(id)
                        this.main = "knote.tornadofx.ViewerApp"
                        workingDir = rootDir
                        classpath(shadowViewer.archiveFile)
                        systemProperty("notebook", id)
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