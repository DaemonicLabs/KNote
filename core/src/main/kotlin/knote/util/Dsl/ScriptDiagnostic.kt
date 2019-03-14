package knote.util.Dsl

import kotlin.script.experimental.api.ScriptDiagnostic

@ScriptDiagnosticDsl
sealed class InfoDiagnostic(val message: String) {
    operator fun invoke() {
        ScriptDiagnostic(message, ScriptDiagnostic.Severity.INFO)
    }
}

@ScriptDiagnosticDsl
class CompileTimeStampDiagnostic(message: String = "COMPILE TIMESTAMP"): InfoDiagnostic(message)

@ScriptDiagnosticDsl
class NotebookIdDiagnostic(notebookId: String): InfoDiagnostic(notebookId)

@ScriptDiagnosticDsl
class PageIdDiagnostic(pageId: String): InfoDiagnostic(pageId)


@ScriptDiagnosticDsl
class ErrorDiagnostic(val message: String) {
    operator fun invoke() {
        ScriptDiagnostic(message, ScriptDiagnostic.Severity.ERROR)
    }
}

@ScriptDiagnosticDsl
class DebugDiagnostic(val message: String) {
    operator fun invoke() {
        ScriptDiagnostic(message, ScriptDiagnostic.Severity.DEBUG)
    }
}
