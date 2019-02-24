import java.io.File
import java.util.concurrent.Executors

object HostUtil {
    fun publishToMavenLocal(hostRoot: File) {
        val gradleWrapper = when {
            Platform.isWindows -> "gradlew.bat"
            Platform.isLinux -> "./gradlew"
            Platform.isMac -> "./gradlew"
            else -> throw IllegalStateException("unsupported OS: ${Platform.osType}")
        }
        val cmd = arrayOf(gradleWrapper, "publishToMavenLocal")
        println("executing ${cmd.joinToString(" ", "[", "]")} in ${hostRoot}")
        System.err.println("executing ${cmd.joinToString(" ", "[", "]")} in ${hostRoot}")
        val command = ProcessBuilder(*cmd)
        val process = command
            .directory(hostRoot.parentFile)
            .start()
        val outStreamGobbler = Runnable {
            process.inputStream.bufferedReader().use {
                it.forEachLine { line -> println("% $line") }
            }
        }
        val errStreamGobbler = Runnable {
            process.errorStream.bufferedReader().use {
                it.forEachLine { line -> System.err.println("% $line") }
            }
        }
        Executors.newSingleThreadExecutor().submit(outStreamGobbler)
        Executors.newSingleThreadExecutor().submit(errStreamGobbler)
        val result = process.waitFor()
        println("command finished with code: $result")

    }

}