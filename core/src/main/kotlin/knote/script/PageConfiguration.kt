package knote.script

import knote.KNote
import knote.annotations.FromPage
import knote.api.PageManager
import knote.core.CoreConstants
import knote.poet.PageDependency
import mu.KLogging
import org.jetbrains.kotlin.script.InvalidScriptResolverAnnotation
import java.time.Instant
import java.util.Date
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCollectedData
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.foundAnnotations
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.importScripts
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

class PageConfiguration : ScriptCompilationConfiguration({
    defaultImports(
        FromPage::class
    )
    jvm {
        // ensures that all dependencies are available to the script
        dependenciesFromCurrentContext(wholeClasspath = false)
    }

    // TODO: generate `Page.id`

    refineConfiguration {
        beforeParsing { context ->
            val reports = mutableListOf<ScriptDiagnostic>()
            reports += ScriptDiagnostic(
                "beforeParsing time: ${System.currentTimeMillis()}",
                ScriptDiagnostic.Severity.DEBUG
            )

            context.compilationConfiguration.asSuccess(reports)
        }

        beforeCompiling { context ->
            val reports = mutableListOf<ScriptDiagnostic>()
            val scriptFile = (context.script as FileScriptSource).file
            val notebookDir = scriptFile.parentFile.parentFile
            val notebookId = notebookDir.name
            val pageId = scriptFile.name.substringBeforeLast(".page.kts")

            reports += ScriptDiagnostic(
                "beforeCompiling time: ${System.currentTimeMillis()}",
                ScriptDiagnostic.Severity.DEBUG
            )
            reports += ScriptDiagnostic(
                "knote version: ${CoreConstants.FULL_VERSION}",
                ScriptDiagnostic.Severity.INFO
            )
            if (CoreConstants.BUILD_NUMBER < 0) {
                val compileTime = Date.from(Instant.ofEpochSecond(CoreConstants.COMPILE_TIMESTAMP))
                reports += ScriptDiagnostic(
                    "COMPILE_TIMESTAMP: $compileTime",
                    ScriptDiagnostic.Severity.INFO
                )
            }
            reports += ScriptDiagnostic(
                "notebook id: $notebookId",
                ScriptDiagnostic.Severity.INFO
            )
            reports += ScriptDiagnostic(
                "page id: $pageId",
                ScriptDiagnostic.Severity.INFO
            )

            ScriptCompilationConfiguration(context.compilationConfiguration) {
                ide.acceptedLocations.append(ScriptAcceptedLocation.Project)
            }.asSuccess(reports)
        }

        onAnnotations(FromPage::class) { context ->
            logger.debug("on annotations")
            val scriptFile = (context.script as FileScriptSource).file
            val notebookDir = scriptFile.parentFile.parentFile
            System.setProperty("knote.notebookDir", notebookDir.path)
            val notebookId = notebookDir.name
            System.setProperty("knote.id", notebookId)

            val pageId = scriptFile.name.substringBeforeLast(".page.kts")

            val reports = mutableListOf<ScriptDiagnostic>()
            reports += ScriptDiagnostic(
                "notebookDir: $notebookDir",
                ScriptDiagnostic.Severity.INFO
            )

//            System.setProperty("user.dir", rootDir.absolutePath)

            val annotations = context.collectedData?.get(ScriptCollectedData.foundAnnotations)?.also { annotations ->
                if (annotations.isNotEmpty()) {
                    reports += ScriptDiagnostic("file_annotations: $annotations", ScriptDiagnostic.Severity.DEBUG)
                }
                if (annotations.any { it is InvalidScriptResolverAnnotation }) {
                    reports += ScriptDiagnostic(
                        "InvalidScriptResolverAnnotation found",
                        ScriptDiagnostic.Severity.ERROR
                    )
                    return@onAnnotations ResultWithDiagnostics.Failure(reports)
                }
            }

            val fromPageAnnotations = annotations?.filterIsInstance(FromPage::class.java)
                ?.takeIf { it.isNotEmpty() }
            val dependencyScripts: List<SourceCode> = if (fromPageAnnotations != null) {
                reports += ScriptDiagnostic(
                    "fromPage: $fromPageAnnotations",
                    ScriptDiagnostic.Severity.INFO
                )
                // TODO: DETECT CIRCULAR DEPENDENCIES
//                val startedPages = loopDetector.getOrPut(notebookId) { mutableListOf() }
//                if(pageId in startedPages) {
//                    reports += ScriptDiagnostic(
//                        "page $pageId depends on itself",
//                        ScriptDiagnostic.Severity.ERROR
//                    )
//                    return@onAnnotations ResultWithDiagnostics.Failure(reports)
//                }
//                startedPages += pageId

//                val notebook = KNote.NOTEBOOK_MANAGER.compileNotebookCached(notebookId)
//                if(notebook != null) {
//                    logger.error("notebook $notebookId could not be loaded")
//                    reports += ScriptDiagnostic(
//                        "notebook $notebookId could not be loaded",
//                        ScriptDiagnostic.Severity.ERROR
//                    )
//                    return@onAnnotations ResultWithDiagnostics.Failure(reports)
//                }
                val pageManager: PageManager = KNote.NOTEBOOK_MANAGER.pageManager
                val generatedSrc = KNote.rootDir.resolve("build").resolve(".knote").resolve(notebookId).absoluteFile
                generatedSrc.mkdirs()
                val pageDependencies = fromPageAnnotations
                    .map { it.source }
                    .distinct()
                    .associate { depId ->
                        // TODO: DETECT CIRCULAR DEPENDENCIES
                        if (depId == pageId) {
                            reports += ScriptDiagnostic(
                                "page $pageId depends on itself",
                                ScriptDiagnostic.Severity.FATAL
                            )
                            return@onAnnotations ResultWithDiagnostics.Failure(reports)
                        }
                        val resultType = pageManager.resultType(depId)
                        if (resultType == null) {
                            reports += ScriptDiagnostic(
                                "resultType of page $depId is unknown",
                                ScriptDiagnostic.Severity.ERROR
                            )
                            return@onAnnotations ResultWithDiagnostics.Failure(reports)
                        }
                        depId to resultType
                    }

                logger.debug("generating dependencies for $notebookId:$pageId")
                val file = PageDependency.generate(
                    output = generatedSrc,
                    notebookId = notebookId,
                    pageId = pageId,
                    pageDependencies = pageDependencies
                )
                logger.debug("generated $file, ${file.exists()}")
                listOf(file.toScriptSource())
            } else listOf()

            val compilationConfiguration = ScriptCompilationConfiguration(context.compilationConfiguration) {
                if (dependencyScripts.isNotEmpty()) {
                    reports += ScriptDiagnostic(
                        "importScripts += ${dependencyScripts.map { it.locationId }}",
                        ScriptDiagnostic.Severity.INFO
                    )
                    importScripts.append(dependencyScripts)
//                    ide.dependenciesSources.append(dependencyScripts)
                }
            }
//            loopDetector.remove(pageId)
            compilationConfiguration.asSuccess(reports)
        }
    }
}) {
    companion object : KLogging() {
//        val loopDetector: MutableMap<String, MutableList<String>> = mutableMapOf()
    }
}


