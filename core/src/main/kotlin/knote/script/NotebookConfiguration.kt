package knote.script

import knote.annotations.FromPage
import knote.core.CoreConstants
import knote.poet.PageMarker
import mu.KLogging
import java.time.Instant
import java.util.Date
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

class NotebookConfiguration : ScriptCompilationConfiguration({
    defaultImports(
        PageMarker::class
    )
    jvm {
        // ensures that all dependencies are available to the script
        dependenciesFromCurrentContext(wholeClasspath = false)
    }

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

            val compileTime = Date.from(Instant.ofEpochSecond( CoreConstants.COMPILE_TIMESTAMP ))
            reports += ScriptDiagnostic(
                "COMPILE_TIMESTAMP: ${compileTime}",
                ScriptDiagnostic.Severity.INFO
            )

            ScriptCompilationConfiguration(context.compilationConfiguration) {
                ide.acceptedLocations.append(ScriptAcceptedLocation.Project)
            }.asSuccess(reports)
        }

        this.onAnnotations(FromPage::class) { context ->
            val reports = mutableListOf<ScriptDiagnostic>()
            val scriptFile = (context.script as FileScriptSource).file
            val rootDir = scriptFile.parentFile

            ScriptCompilationConfiguration(context.compilationConfiguration) {
                    ide.acceptedLocations.append(ScriptAcceptedLocation.Project)
            }.asSuccess(reports)
        }
//        onAnnotations(Import::class) { context ->
//            logger.debug("on annotations")
//            val scriptFile = (context.script as FileScriptSource).file
//            val rootDir = scriptFile.parentFile.parentFile
//            val id = scriptFile.name.substringBeforeLast(".notebook.kts")
//
//            val reports = mutableListOf<ScriptDiagnostic>()
//            val annotations = context.collectedData?.get(ScriptCollectedData.foundAnnotations)?.also { annotations ->
//                reports += ScriptDiagnostic("file_annotations: $annotations", ScriptDiagnostic.Severity.INFO)
//
//                if (annotations.any { it is InvalidScriptResolverAnnotation }) {
//                    reports += ScriptDiagnostic(
//                        "InvalidScriptResolverAnnotation found",
//                        ScriptDiagnostic.Severity.ERROR
//                    )
//                    return@onAnnotations ResultWithDiagnostics.Failure(reports)
//                }
//            }
//
//            // TODO: list pages, generate constants and include
//            val pagesDir = rootDir.resolve("${id}_pages")
//            val pages = pagesDir.listFiles { file -> file.isFile && file.name.endsWith(".page.kts") } ?: run {
//                reports += ScriptDiagnostic("no files found in $pagesDir", ScriptDiagnostic.Severity.ERROR)
//                arrayOf<File>()
////                return@onAnnotations ResultWithDiagnostics.Failure(reportsObject)
//            }
//
//            val generatedSrc = rootDir.resolve("build").resolve(".knote").resolve(id)
//            val generatedFiles = PageMarker.generate(generatedSrc, pages, fileName = id.capitalize())
//
//            reports += ScriptDiagnostic(
//                "generated: ${generatedFiles.map { it.relativeTo(rootDir) }}",
//                ScriptDiagnostic.Severity.INFO
//            )
//
//            return@onAnnotations ScriptCompilationConfiguration(context.compilationConfiguration) {
//                ide.acceptedLocations.append(ScriptAcceptedLocation.Project)
//                if (generatedFiles.isNotEmpty()) {
//                    importScripts.append(generatedFiles.map { it.toScriptSource() })
//                }
//
//                if(annotations != null) {
//                    val importAnnotations = annotations.filterIsInstance(Import::class.java)
//                    reports += ScriptDiagnostic("importAnnotations: $importAnnotations", ScriptDiagnostic.Severity.DEBUG)
//
//                    val sources = importAnnotations.map {
//                        rootDir.resolve("include").resolve(it.source)
//                    }.distinct()
//
//                    if (sources.isNotEmpty()) {
//                        importScripts.append(sources.map { it.toScriptSource() })
//                        reports += ScriptDiagnostic(
//                            "importScripts += ${sources.map { it.relativeTo(rootDir) }}",
//                            ScriptDiagnostic.Severity.INFO
//                        )
//                    }
//                }
//
//            }.asSuccess(reports)
//        }
    }
}) {
    companion object : KLogging()
}


