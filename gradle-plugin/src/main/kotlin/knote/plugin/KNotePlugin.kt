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

        project.repositories {
            maven(url = "http://maven.modmuss50.me") {
                name = "modmuss50"
            }
        }

        project.afterEvaluate {
            knoteExtension.notebooks.forEach { notebookModel ->
                val id = notebookModel.id
                val knoteDepScope = notebookModel.dependencies
                dependencies {
                    add(
                        configurationName = knoteDepScope.knoteConfiguration.name,
                        dependencyNotation = create(
                            group = "daemoniclabs.knote",
                            name = "core",
                            version = GradlePluginConstants.FULL_VERSION
                        )
                    )
                    add(
                        configurationName = knoteDepScope.knoteFXConfiguration.name,
                        dependencyNotation = create(
                            group = "daemoniclabs.knote",
                            name = "tornadofx-viewer",
                            version = GradlePluginConstants.FULL_VERSION
                        )
                    )
                    if (knote.util.Platform.isWindows) {
                        // Windows required jansi for logging
                        add(
                            configurationName = knoteDepScope.knoteConfiguration.name,
                            dependencyNotation = create(
                                group = "org.fusesource.jansi",
                                name = "jansi",
                                version = "1.9"
                            )
                        )
                    }
                    knoteDepScope.dependencies.forEach { (configuration, dependencies) ->
                        dependencies.forEach { dependencyNotation ->
                            add(
                                configurationName = configuration.name,
                                dependencyNotation = dependencyNotation
                            )
                        }
                    }

                    // adding dependencies to implementation ?

                    knoteDepScope.knoteConfiguration.resolvedConfiguration.firstLevelModuleDependencies.forEach {
                        logger.lifecycle("[$id] adding to implementation knote: $it")
                        add(
                            implementation.name, create(
                                it.moduleGroup,
                                it.moduleName,
                                it.moduleVersion
                            )
                        )
                    }
                    knoteDepScope.knoteFXConfiguration.resolvedConfiguration.firstLevelModuleDependencies.forEach {
                        logger.lifecycle("[$id] adding to implementation knoteFX: $it")
                        add(
                            implementation.name, create(
                                it.moduleGroup,
                                it.moduleName,
                                it.moduleVersion
                            )
                        )
                    }
                }

                val sourceSets = project.extensions.getByName<SourceSetContainer>("sourceSets")
                val main = sourceSets.getByName("main")

                val shadowCore = project.tasks.create<ShadowJar>("${id}_shadowCore") {
                    group = "shadow"
                    from(main.output)
                    archiveBaseName.set("${id}_core")
                    configurations = listOf(knoteDepScope.knoteConfiguration)
                    manifest {
                        attributes(
                            mapOf(
                                "Main-Class" to "knote.Main",
                                "version" to project.version
                            )
                        )
                    }
                }
                val shadowViewer = project.tasks.create<ShadowJar>("${id}_shadowViewer") {
                    group = "shadow"
                    from(main.output)
                    archiveBaseName.set("${id}_tornadofx-viewer")
                    configurations = listOf(knoteDepScope.knoteConfiguration, knoteDepScope.knoteFXConfiguration)
                    manifest {
                        attributes(
                            mapOf(
                                "Main-Class" to "knote.tornadofx.ViewerApp",
                                "version" to project.version
                            )
                        )
                    }
                }

                task<JavaExec>("${id}_run") {
                    dependsOn(shadowCore)
                    val jarFile = shadowCore.archiveFile.get()
                    group = "application"
//                    args = listOf(id)
                    systemProperty("knote.notebookDir", notebookModel.notebookDir.path)
                    systemProperty("knote.id", id)
                    workingDir = rootDir
                    this.main = "knote.Main"
                    classpath(shadowCore.archiveFile)
                    doFirst {
                        logger.lifecycle("executing")
                        logger.lifecycle("java -jar ${jarFile} ${(args as List<String>).joinToString(" ")}")
                        logger.lifecycle("\n")
                    }
                }
                task<JavaExec>("${id}_runViewer") {
                    dependsOn(shadowViewer)
                    val jarFile = shadowViewer.archiveFile.get()
                    group = "application"
//                    args = listOf(id)
                    systemProperty("knote.notebookDir", notebookModel.notebookDir.path)
                    systemProperty("knote.id", id)
                    workingDir = rootDir
                    this.main = "knote.tornadofx.ViewerApp"
                    classpath(shadowViewer.archiveFile)
                    systemProperty("notebook", id)
                    doFirst {
                        logger.lifecycle("executing")
                        logger.lifecycle("java -jar ${jarFile} ${(args as List<String>).joinToString(" ")}")
                        logger.lifecycle("\n")
                    }
                }
            }
            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }

            // TODO: loop through registered notebooks (in extension ?)

            knoteExtension.notebooks.forEach { notebookModel ->
                val scriptFile = notebookModel.notebookDir.resolve(notebookModel.mainFile)
                val id = notebookModel.id

                val generatedSrc = project.rootDir.resolve("build").resolve(".knote").resolve(id)
                    .apply { mkdirs() }
                val pagesSrc = notebookModel.notebookDir.resolve("pages").apply { mkdirs() }
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
            }
        }
    }
}