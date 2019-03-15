package knote.host

import knote.api.Page
import knote.script.KTScriptCallback.ktScriptCallbackHandler
import knote.util.MutableKObservableObject
import mu.KLogging
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtScript
import java.io.File
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.api.resultOrNull
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptEvaluator
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.impl.withDefaults

object EvalScript : KLogging() {
    fun createJvmScriptingHost(
        cacheDir: File
    ): BasicJvmScriptingHost {
        val cache = FileBasedScriptCache(cacheDir)
        val knJvmCompiler =  KNJvmCompilerImpl(defaultJvmScriptingHostConfiguration.withDefaults())
        val compiler = JvmScriptCompiler(defaultJvmScriptingHostConfiguration, compilerProxy = knJvmCompiler, cache = cache)
        val evaluator = BasicJvmScriptEvaluator()
        val host = BasicJvmScriptingHost(compiler = compiler, evaluator = evaluator)
        return host
    }

    inline fun <reified T : Any> evalScript(
        host: BasicJvmScriptingHost,
        scriptFile: File,
        vararg args: Any?,
//        noinline ktScriptCallback: ((KtScript?) -> Unit)? = null,
        noinline ktScriptCallback: ((KtFile?) -> Unit)? = null,
        compilationConfig: ScriptCompilationConfiguration = createJvmCompilationConfigurationFromTemplate<T> {
            jvm {
                // when you can run your script-host from a fat jar, you can set this to
                // `wholeClasspath = false` to reduce dependencies and speed up script compilation
                dependenciesFromCurrentContext(wholeClasspath = false)

//            val JDK_HOME = System.getProperty("jdkHome") ?: System.getenv("JAVA_HOME")
//                ?: throw IllegalStateException("please pass -DjdkHome=path/to/jdk or please set JAVA_HOME to the installed jdk")
//            jdkHome(File(JDK_HOME))
            }
            if(ktScriptCallback != null) {
                ktScriptCallbackHandler(ktScriptCallback)
            }
        }
    ): Pair<T?, List<ScriptDiagnostic>> = evalScriptNoInline<T>(
        host = host,
        scriptFile = scriptFile,
        args = *args,
        compilationConfig = compilationConfig
    )

    // this should fix linenumbers from the logger
    fun <T : Any> evalScriptNoInline(
        host: BasicJvmScriptingHost,
        scriptFile: File,
        vararg args: Any?,
        compilationConfig: ScriptCompilationConfiguration
    ): Pair<T?, List<ScriptDiagnostic>> {
        logger.debug("compilationConfig entries")
        compilationConfig.entries().forEach {
            logger.debug("    $it")
        }

        val evaluationConfig = ScriptEvaluationConfiguration {
            args.forEach { logger.debug("constructorArg: $it  ${it!!::class}") }
            constructorArgs.append(*args)
        }

        logger.debug("evaluationConfig entries")
        evaluationConfig.entries().forEach {
            logger.debug("    $it")
        }

        val scriptSource = scriptFile.toScriptSource()

        logger.debug("compiling script, please be patient")
        val result = host.eval(scriptSource, compilationConfig, evaluationConfig)

        return EvalScript.get<T?>(result, scriptFile)
    }

    fun SourceCode.Location.posToString() = "(${start.line}, ${start.col})"

    fun <T> get(
        resultWithDiagnostics: ResultWithDiagnostics<EvaluationResult>,
        scriptFile: File
    ): Pair<T?, List<ScriptDiagnostic>> {
//        for (report in resultWithDiagnostics.reports) {
//            val path = report.sourcePath?.let { "$it: " } ?: ""
//            val location = report.location?.posToString()?.let { "$it: " } ?: ""
//            val messageString = "$path$location${report.message}"
//            when (report.severity) {
//                ScriptDiagnostic.Severity.FATAL -> logger.error { "FATAL: $messageString" }
//                ScriptDiagnostic.Severity.ERROR -> logger.error { messageString }
//                ScriptDiagnostic.Severity.WARNING -> logger.warn { messageString }
//                ScriptDiagnostic.Severity.INFO -> logger.info { messageString }
//                ScriptDiagnostic.Severity.DEBUG -> logger.debug { messageString }
//            }
//            report.exception?.apply {
//                logger.error(message, this)
////            printStackTrace()
//                this.cause?.apply {
//                    logger.error(message, this)
////                printStackTrace()
//                }
//                this.suppressed.forEach {
//                    logger.error("suppressed exception: ${it.message}", this)
////                it.printStackTrace()
//                }
//            }
//        }

        val evalResult = resultWithDiagnostics.resultOrNull() ?: resultWithDiagnostics.run {
            logger.error("evaluation result failed for file $scriptFile")
            return null to this.reports
        }

        val resultValue = evalResult.returnValue
        logger.trace("resultValue = '$resultValue'")
        logger.trace("resultValue::class = '${resultValue::class}'")

        return when (resultValue) {
            is ResultValue.Value -> {
                logger.trace("resultValue.name = '${resultValue.name}'")
                logger.trace("resultValue.value = '${resultValue.value}'")
                logger.trace("resultValue.type = '${resultValue.type}'")

                logger.trace("resultValue.value::class = '${resultValue.value!!::class}'")
                logger.trace("resultValue.value::class.supertypes = '${resultValue.value!!::class.supertypes}'")

                val env = resultValue.scriptInstance as T
                logger.debug { env }
                env to resultWithDiagnostics.reports
            }
            is ResultValue.Unit -> {
                System.err.println("evaluation failed")
                logger.error("evaluation failed")
                null to resultWithDiagnostics.reports
            }
        }
    }
}





