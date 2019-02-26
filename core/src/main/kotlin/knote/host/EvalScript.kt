package knote.host

import mu.KLogging
import java.io.File
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.api.dependencies
import kotlin.script.experimental.api.importScripts
import kotlin.script.experimental.api.resultOrNull
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptEvaluator
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

object EvalScript : KLogging() {
    fun createJvmScriptingHost(cacheDir: File): BasicJvmScriptingHost {
        val cache = FileBasedScriptCache(cacheDir)
        val compiler = JvmScriptCompiler(defaultJvmScriptingHostConfiguration, cache = cache)
        val evaluator = BasicJvmScriptEvaluator()
        val host = BasicJvmScriptingHost(compiler = compiler, evaluator = evaluator)
        return host
    }

    inline fun <reified T : Any> evalScript(
        host: BasicJvmScriptingHost,
        scriptFile: File,
        vararg args: Any?,
        libs: File? = null,
        importScripts: List<SourceCode> = listOf(),
        compilationConfig: ScriptCompilationConfiguration = createJvmCompilationConfigurationFromTemplate<T> {
            jvm {
                // when you can run your script-host from a fat jar, you can set this to
                // `wholeClasspath = false` to reduce dependencies and speed up script compilation
                dependenciesFromCurrentContext(wholeClasspath = false)

                importScripts(importScripts)

                libs?.absoluteFile?.takeIf { it.exists() }?.apply {
                    listFiles { file -> file.name.endsWith(".jar") }
                        .forEach {
                            EvalScript.logger.debug("adding dependency: $it")
                            dependencies.append(JvmDependency(it))
                        }
                }
//            val JDK_HOME = System.getProperty("jdkHome") ?: System.getenv("JAVA_HOME")
//                ?: throw IllegalStateException("please pass -DjdkHome=path/to/jdk or please set JAVA_HOME to the installed jdk")
//            jdkHome(File(JDK_HOME))
            }
        }
    ): Pair<T?, List<ScriptDiagnostic>> {
        EvalScript.logger.debug("compilationConfig entries")
        compilationConfig.entries().forEach {
            EvalScript.logger.debug("    $it")
        }

        val evaluationConfig = ScriptEvaluationConfiguration {
            args.forEach { EvalScript.logger.debug("constructorArg: $it  ${it!!::class}") }
            constructorArgs.append(*args)
        }

        EvalScript.logger.debug("evaluationConfig entries")
        evaluationConfig.entries().forEach {
            EvalScript.logger.debug("    $it")
        }

        val scriptSource = scriptFile.toScriptSource()

        EvalScript.logger.debug("compiling script, please be patient")
        val result = host.eval(scriptSource, compilationConfig, evaluationConfig)

        return EvalScript.get<T?>(result, scriptFile)
    }

    fun SourceCode.Location.posToString() = "(${start.line}, ${start.col})"

    fun <T> get(
        resultWithDiagnostics: ResultWithDiagnostics<EvaluationResult>,
        scriptFile: File
    ): Pair<T?, List<ScriptDiagnostic>> {
        val evalResult = resultWithDiagnostics.resultOrNull() ?: resultWithDiagnostics.run {
            EvalScript.logger.error("evaluation result failed for notebook $scriptFile")
            return null to this.reports
        }

        for (report in resultWithDiagnostics.reports) {
            val path = report.sourcePath?.let {"$it: "} ?: ""
            val location = report.location?.posToString()?.let { "$it: "} ?: ""
            val messageString = "$path$location${report.message}"
            when (report.severity) {
                ScriptDiagnostic.Severity.FATAL -> EvalScript.logger.error { "FATAL: $messageString" }
                ScriptDiagnostic.Severity.ERROR -> EvalScript.logger.error { messageString }
                ScriptDiagnostic.Severity.WARNING -> EvalScript.logger.warn { messageString }
                ScriptDiagnostic.Severity.INFO -> EvalScript.logger.info { messageString }
                ScriptDiagnostic.Severity.DEBUG -> EvalScript.logger.debug { messageString }
            }
//            EvalScript.logger.trace("$severityIndicator: ${report.sourcePath}: ${report.location?.posToString()}: ${report.message}")
            report.exception?.apply {
                EvalScript.logger.error("exception: $message", this)
//            printStackTrace()
                this.cause?.apply {
                    EvalScript.logger.error("cause: $message", this)
//                printStackTrace()
                }
                this.suppressed.forEach {
                    EvalScript.logger.error("suppressed exception: ${it.message}", this)
//                it.printStackTrace()
                }
            }
        }

        val resultValue = evalResult.returnValue
        EvalScript.logger.trace("resultValue = '$resultValue'")
        EvalScript.logger.trace("resultValue::class = '${resultValue::class}'")

        return when (resultValue) {
            is ResultValue.Value -> {
                EvalScript.logger.trace("resultValue.name = '${resultValue.name}'")
                EvalScript.logger.trace("resultValue.value = '${resultValue.value}'")
                EvalScript.logger.trace("resultValue.type = '${resultValue.type}'")

                EvalScript.logger.trace("resultValue.value::class = '${resultValue.value!!::class}'")
                EvalScript.logger.trace("resultValue.value::class.supertypes = '${resultValue.value!!::class.supertypes}'")

                val env = resultValue.value as T
                EvalScript.logger.debug { env }
                env to resultWithDiagnostics.reports
            }
            is ResultValue.Unit -> {
                System.err.println("evaluation failed")
                EvalScript.logger.error("evaluation failed")
                null to resultWithDiagnostics.reports
            }
        }
    }
}





