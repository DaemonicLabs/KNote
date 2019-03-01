package knote.script

import knote.KNote
import knote.annotations.FromPage
import knote.api.PageManager
import knote.poet.PageDependency
import mu.KLogging
import org.jetbrains.kotlin.script.InvalidScriptResolverAnnotation
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
            reports += ScriptDiagnostic(
                "beforeCompiling time: ${System.currentTimeMillis()}",
                ScriptDiagnostic.Severity.DEBUG
            )

            ScriptCompilationConfiguration(context.compilationConfiguration) {
                ide.acceptedLocations.append(ScriptAcceptedLocation.Project)
            }.asSuccess(reports)
        }

        onAnnotations(FromPage::class) { context ->
            logger.debug("on annotations")
            val scriptFile = (context.script as FileScriptSource).file
            val rootDir = scriptFile.parentFile.parentFile

            val notebookId = scriptFile.parentFile.name.substringBeforeLast("_pages")
            val pageId = scriptFile.name.substringBeforeLast(".page.kts")

            val reports = mutableListOf<ScriptDiagnostic>()
            reports += ScriptDiagnostic(
                "rootDir: $rootDir",
                ScriptDiagnostic.Severity.INFO
            )

            val annotations = context.collectedData?.get(ScriptCollectedData.foundAnnotations)?.also { annotations ->
                reports += ScriptDiagnostic("file_annotations: $annotations", ScriptDiagnostic.Severity.DEBUG)

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
                    "fromPageAnnotations: $fromPageAnnotations",
                    ScriptDiagnostic.Severity.DEBUG
                )
                val startedPages = loopDetector.getOrPut(notebookId) { mutableListOf() }
                if(pageId in startedPages) {
                    reports += ScriptDiagnostic(
                        "page $pageId depends on itself",
                        ScriptDiagnostic.Severity.ERROR
                    )
                    return@onAnnotations ResultWithDiagnostics.Failure(reports)
                }
                startedPages += pageId
                val pageManager: PageManager = KNote.NOTEBOOK_MANAGER.getPageManager(notebookId)
//                val page = pageManager.findPage(pageId)!! as PageImpl
                val generatedSrc = rootDir.resolve("build").resolve(".dependencies").resolve(notebookId).absoluteFile
                generatedSrc.deleteRecursively()
                generatedSrc.mkdirs()
                val pageDependencies = fromPageAnnotations
                    .map { it.source }
                    .distinct()
                    .associate { depId ->
                        if(depId == pageId) {
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

                val file = PageDependency.generate(
                    output = generatedSrc,
                    notebookId = notebookId,
                    pageId = pageId,
                    pageDependencies = pageDependencies
                )
                listOf(file.toScriptSource())
            } else listOf()

            val compilationConfiguration = ScriptCompilationConfiguration(context.compilationConfiguration) {
                ide.acceptedLocations.append(ScriptAcceptedLocation.Project)

                if (dependencyScripts.isNotEmpty()) {
                    defaultImports(
                        FromPage::class
                    )
                    reports += ScriptDiagnostic(
                        "importScripts += ${dependencyScripts.map { it.locationId }}",
                        ScriptDiagnostic.Severity.INFO
                    )
                    importScripts.append(dependencyScripts)
                }
            }
            loopDetector.remove(pageId)
            compilationConfiguration.asSuccess(reports)
        }
    }
}) {
    companion object : KLogging() {
        val loopDetector: MutableMap<String, MutableList<String>> = mutableMapOf()
    }
}


