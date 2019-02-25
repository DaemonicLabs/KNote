package knote.host

import mu.KLogger
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

fun createJvmScriptingHost(cacheDir: File): BasicJvmScriptingHost {
    val cache = FileBasedScriptCache(cacheDir)
    val compiler = JvmScriptCompiler(defaultJvmScriptingHostConfiguration, cache = cache)
    val evaluator = BasicJvmScriptEvaluator()
    val host = BasicJvmScriptingHost(compiler = compiler, evaluator = evaluator)
    return host
}

inline fun <reified T: Any> BasicJvmScriptingHost.evalScript(
    scriptFile: File,
    vararg args: Any?,
    logger: KLogger,
    libs: File? = null,
    importScripts: List<SourceCode> = listOf(),
    compilationConfig: ScriptCompilationConfiguration = createJvmCompilationConfigurationFromTemplate<T> {
        jvm {
            // when yyou can run your script-host from a fat jar, you can set this to
            // `wholeClasspath = false` to reduce dependencies and speed up script compilation
            dependenciesFromCurrentContext(wholeClasspath = true)

            importScripts(importScripts)

            libs?.absoluteFile?.takeIf { it.exists() }?.apply {
                listFiles { file -> file.name.endsWith(".jar")}
                    .forEach {
                        logger.debug("adding dependency: $it")
                        dependencies.append(JvmDependency(it))
                    }
            }
//            val JDK_HOME = System.getProperty("jdkHome") ?: System.getenv("JAVA_HOME")
//                ?: throw IllegalStateException("please pass -DjdkHome=path/to/jdk or please set JAVA_HOME to the installed jdk")
//            jdkHome(File(JDK_HOME))
        }
    }
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
    val result = eval(scriptSource, compilationConfig, evaluationConfig)

    return result.get<T?>(scriptFile, logger)
}

fun SourceCode.Location.posToString() = "(${start.line}, ${start.col})"

inline fun <reified T> ResultWithDiagnostics<EvaluationResult>.get(scriptFile: File, logger: KLogger): Pair<T?, List<ScriptDiagnostic>> {

//    for (report in reports) {
//        val severityIndicator = when (report.severity) {
//            ScriptDiagnostic.Severity.FATAL -> "fatal"
//            ScriptDiagnostic.Severity.ERROR -> "e"
//            ScriptDiagnostic.Severity.WARNING -> "w"
//            ScriptDiagnostic.Severity.INFO -> "i"
//            ScriptDiagnostic.Severity.DEBUG -> "d"
//        }
//        println("$severityIndicator: ${report.sourcePath}: ${report.location?.posToString()}: ${report.message}")
//        report.exception?.apply {
//            println("exception: $message")
//            printStackTrace()
//            this.cause?.apply {
//                println("cause: $message")
//                printStackTrace()
//            }
//            this.suppressed.forEach {
//                println("suppressed exception: ${it.message}")
//                it.printStackTrace()
//            }
//        }
//    }
//    println(this)
    val evalResult = resultOrNull() ?: run {
        logger.error("evaluation result failed for notebook $scriptFile")
        return null to reports
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

            val env = resultValue.value as T
            logger.debug { env }
            env to reports
        }
        is ResultValue.Unit -> {
            System.err.println("evaluation failed")
            logger.error("evaluation failed")
            null to reports
        }
    }
}