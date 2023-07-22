import shell.CommandBuilder
import shell.CommandResult
import shell.ShellCommand

object ShellProcess {

    suspend fun runSign(
        zipalignPath: String,
        apksignerPath: String,
        originFilepath: String,
        tempFilepath: String,
        jksPath: String,
        alias: String,
        outputFile: String,
        keystorePwd: String,
        keyPwd: String,
    ) {
        val zipalignResult = runZipalign(zipalignPath, originFilepath, tempFilepath)
        if (zipalignResult.code == 0) {
            println(">>>> zipalign success")
            val signResult = runSign(apksignerPath, jksPath, alias, outputFile, tempFilepath, keystorePwd, keyPwd)
            if (signResult.code == 0) {
                println(">>>> sign success")
            }
        }
    }

    private suspend fun runZipalign(
        zipalignPath: String,
        originFilepath: String,
        tempFilepath: String,
    ): CommandResult {
        val zipalignCommand = CommandBuilder()
            .append(zipalignPath)
            .append("-p")
            .append("-f")
            .append("-v")
            .append("4")
            .append(originFilepath)
            .append(tempFilepath)
            .build()
        return ShellCommand.runCommand(zipalignCommand)
    }

    private suspend fun runSign(
        apksignerPath: String,
        jksPath: String,
        alias: String,
        outputFile: String,
        tempFilepath: String,
        keystorePwd: String,
        keyPwd: String,
    ): CommandResult {
        val signCommand = CommandBuilder()
            .append("java")
            .append("-jar")
            .append(apksignerPath)
            .append("sign")
            .append("--verbose")
            .append("--ks")
            .append(jksPath)
            .append("--ks-pass")
            .append("pass:$keystorePwd")
            .append("--ks-key-alias")
            .append(alias)
            .append("--key-pass")
            .append("pass:$keyPwd")
            .append("--out")
            .append(outputFile)
            .append(tempFilepath)
            .build()
        return ShellCommand.runCommand(signCommand)
    }
}