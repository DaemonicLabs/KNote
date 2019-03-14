package knote.util.Dsl

@DslMarker
annotation class ScriptDiagnosticDsl

fun compileTimeStampDiagnostic(message: String, block: CompileTimeStampDiagnostic.() -> Unit): CompileTimeStampDiagnostic {
    return CompileTimeStampDiagnostic(message).apply(block)
}

fun notebookIdDiagnostic(message: String, block: NotebookIdDiagnostic.() -> Unit): NotebookIdDiagnostic {
    return NotebookIdDiagnostic(message).apply(block)
}

fun pageIdDiagnostic(message: String, block: PageIdDiagnostic.() -> Unit): PageIdDiagnostic {
    return PageIdDiagnostic(message).apply(block)
}

fun errorDiagnostic(message: String, block: ErrorDiagnostic.() -> Unit): ErrorDiagnostic {
    return ErrorDiagnostic(message).apply(block)
}

fun debugDiagnostic(message: String, block: DebugDiagnostic.() -> Unit): DebugDiagnostic {
    return DebugDiagnostic(message).apply(block)
}