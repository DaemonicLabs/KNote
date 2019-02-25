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

object EvalScript : KLogging()

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
    val result = eval(scriptSource, compilationConfig, evaluationConfig)

    return result.get<T?>(scriptFile)
}

inline fun <reified T> ResultWithDiagnostics<EvaluationResult>.get(scriptFile: File): Pair<T?, List<ScriptDiagnostic>> {
    val evalResult = resultOrNull() ?: run {
        EvalScript.logger.error("evaluation result failed for notebook $scriptFile")
        return null to reports
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
            env to reports
        }
        is ResultValue.Unit -> {
            System.err.println("evaluation failed")
            EvalScript.logger.error("evaluation failed")
            null to reports
        }
    }
}