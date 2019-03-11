package knote.script

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
//    defaultImports(
//        PageMarker::class
//    )
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

            val scriptFile = (context.script as FileScriptSource).file
            val id = scriptFile.name.substringBeforeLast(".notebook.kts")

            reports += ScriptDiagnostic(
                "beforeCompiling time: ${System.currentTimeMillis()}",
                ScriptDiagnostic.Severity.DEBUG
            )

            reports += ScriptDiagnostic(
                "knote version: ${CoreConstants.FULL_VERSION}",
                ScriptDiagnostic.Severity.INFO
            )
            if(CoreConstants.BUILD_NUMBER < 0) {
                val compileTime = Date.from(Instant.ofEpochSecond( CoreConstants.COMPILE_TIMESTAMP ))
                reports += ScriptDiagnostic(
                    "COMPILE_TIMESTAMP: $compileTime",
                    ScriptDiagnostic.Severity.INFO
                )
            }
            reports += ScriptDiagnostic(
                "notebook id: $id",
                ScriptDiagnostic.Severity.INFO
            )

            ScriptCompilationConfiguration(context.compilationConfiguration) {
                ide.acceptedLocations.append(ScriptAcceptedLocation.Project)
            }.asSuccess(reports)
        }
    }
}) {
    companion object : KLogging()
}


