package knote.util.Dsl

@DslMarker
annotation class ScriptDiagnosticDsl

fun infoDiagnostic(message: String, block: T<out InfoDiagnostic>.() -> Unit): T {
    return T(message).apply(block)
}

fun errorDiagnostic(message: String, block: T<out ErrorDiagnostic>.() -> Unit): T {
    return T(message).apply(block)
}

fun debugDiagnostic(message: String, block: T<out ErrorDiagnostic>.() -> Unit): T {
    return T(message).apply(block)
}