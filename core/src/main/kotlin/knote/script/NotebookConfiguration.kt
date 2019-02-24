package knote.script

import knote.annotations.Import
import knote.poet.NotePage
import org.jetbrains.kotlin.script.InvalidScriptResolverAnnotation
import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCollectedData
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.dependencies
import kotlin.script.experimental.api.foundAnnotations
import kotlin.script.experimental.api.importScripts
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

class NotebookConfiguration : ScriptCompilationConfiguration({
    defaultImports(
        Import::class,
        NotePage::class
    )
//    defaultImports.append(
//        "knote.Constants"
//    )
    jvm {
        // ensures that all dependencies are available to the script
        dependenciesFromCurrentContext(wholeClasspath = false)
    }

    refineConfiguration {
        beforeParsing { context ->
            val reports = mutableListOf<ScriptDiagnostic>()
            reports += ScriptDiagnostic("beforeParsing time: ${System.currentTimeMillis()}", ScriptDiagnostic.Severity.DEBUG)

            context.compilationConfiguration.asSuccess(reports)
        }

        beforeCompiling { context ->
            val reports = mutableListOf<ScriptDiagnostic>()
            reports += ScriptDiagnostic("beforeCompiling time: ${System.currentTimeMillis()}", ScriptDiagnostic.Severity.DEBUG)

            context.compilationConfiguration.asSuccess(reports)
        }

        onAnnotations(Import::class) { context ->
            println("on annotations")
            val scriptFile = (context.script as FileScriptSource).file
            val rootDir = scriptFile.parentFile.parentFile

            val reports = mutableListOf<ScriptDiagnostic>()
            val annotations = context.collectedData?.get(ScriptCollectedData.foundAnnotations)?.also { annotations ->
                reports += ScriptDiagnostic("file_annotations: $annotations", ScriptDiagnostic.Severity.INFO)

                if (annotations.any { it is InvalidScriptResolverAnnotation }) {
                    reports += ScriptDiagnostic(
                        "InvalidScriptResolverAnnotation found",
                        ScriptDiagnostic.Severity.ERROR
                    )
                    return@onAnnotations ResultWithDiagnostics.Failure(reports)
                }
            } ?: return@onAnnotations context.compilationConfiguration.asSuccess(reports)

            // TODO: list pages, generate constants and include
            val pagesDir = rootDir.resolve("pages")
            val pages = pagesDir.listFiles { file -> file.isFile && file.name.endsWith(".page.kts") } ?: run {
                reports += ScriptDiagnostic("no files found in $pagesDir", ScriptDiagnostic.Severity.ERROR)
                arrayOf<File>()
//                return@onAnnotations ResultWithDiagnostics.Failure(reports)
            }
            val generatedSrc = rootDir.resolve("build").resolve(".knote")
            val generatedFiles = NotePage.generate(generatedSrc, pages)
            reports += ScriptDiagnostic("generated: ${generatedFiles.map { it.relativeTo(rootDir) }}", ScriptDiagnostic.Severity.INFO)

            val compilationBuilder = ScriptCompilationConfiguration(context.compilationConfiguration) {
                if (generatedFiles.isNotEmpty()) {
                    importScripts.append(generatedFiles.map { it.toScriptSource() })
                }
            }

            val importAnnotations = annotations.filterIsInstance(Import::class.java)
            reports += ScriptDiagnostic("importAnnotations: $importAnnotations", ScriptDiagnostic.Severity.DEBUG)

            val sources = importAnnotations.map {
                rootDir.resolve("include").resolve(it.source)
            }.distinct()

            return@onAnnotations compilationBuilder.apply {
                if (sources.isNotEmpty()) {
                    importScripts.append(sources.map { it.toScriptSource() })
                    reports += ScriptDiagnostic(
                        "importScripts += ${sources.map { it.relativeTo(rootDir) }}",
                        ScriptDiagnostic.Severity.INFO
                    )
                }
            }.asSuccess(reports)
        }
    }
})

