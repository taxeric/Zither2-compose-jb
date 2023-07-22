package shell

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext

object ShellCommand {

    suspend fun runCommand(
        args: List<String>,
    ): CommandResult {
        println(">>>> run command: $args")
        return withContext(Dispatchers.IO) {
            var cmd: Process? = null
            try {
                cmd = Runtime.getRuntime().exec(args.toTypedArray())
                val exitCode = async {
                    runInterruptible { cmd.waitFor() }
                }
                val stdout = async {
                    runInterruptible {
                        String(cmd.inputStream.readAllBytes(), charset("GBK"))
                    }
                }
                val stderr = async {
                    runInterruptible {
                        String(cmd.errorStream.readAllBytes(), charset("GBK"))
                    }
                }
                CommandResult(
                    code = exitCode.await(),
                    stdout = stdout.await(),
                    stderr = stderr.await(),
                )
            } catch (e: Throwable) {
                CommandResult(
                    code = cmd?.exitValue() ?: -1,
                    stdout = "failed",
                    stderr = e.message ?: "unknown"
                )
            } finally {
                cmd?.destroy()
            }
        }
    }
}