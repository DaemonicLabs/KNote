package knote.util.Dsl

@DslMarker
annotation class ScriptDiagnosticDsl

fun infoDiagnostic(message: String, block: INFODIAGNOSTIC.() -> Unit): INFODIAGNOSTIC {
    return INFODIAGNOSTIC(message).apply(block)
}