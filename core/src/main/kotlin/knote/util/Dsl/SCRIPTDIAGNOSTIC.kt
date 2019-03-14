package knote.util.Dsl

@ScriptDiagnosticDsl
class INFODIAGNOSTIC(val message: String) {

}

class InfoDiagnostic(val message: String)
class CompileTimeStampDiagnostic(
        val message: String = "COMPILETIMESTAMP")