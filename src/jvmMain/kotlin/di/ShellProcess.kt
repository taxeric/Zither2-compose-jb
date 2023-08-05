package di

import shell.CommandBuilder
import shell.CommandResult
import shell.ShellCommand

object ShellProcess {

    /**
     * 签名版本
     */
    suspend fun signVersionFromApk(
        apksignerPath: String,
        apkPath: String,
        onSuccess: (msg: String) -> Unit,
        onFailed: (msg: String) -> Unit
    ) {
        val command = CommandBuilder()
            .append("java")
            .append("-jar")
            .append(apksignerPath)
            .append("verify")
            .append("-v")
            .append(apkPath)
            .build()
        commonPrint(command, onSuccess, onFailed)
    }

    /**
     * 签名信息
     */
    suspend fun signInfoFromApk(
        keytoolPath: String,
        apkPath: String,
        onSuccess: (msg: String) -> Unit,
        onFailed: (msg: String) -> Unit
    ) {
        val command = CommandBuilder()
            .append(keytoolPath)
            .append("-printcert")
            .append("-jarfile")
            .append(apkPath)
            .build()
        commonPrint(command, onSuccess, onFailed)
    }

    /**
     * 基本信息
     */
    suspend fun baseInfoFromApk(
        aapt2Path: String,
        apkPath: String,
        onSuccess: (msg: String) -> Unit,
        onFailed: (msg: String) -> Unit
    ) {
        val command = CommandBuilder()
            .append(aapt2Path)
            .append("dump")
            .append("badging")
            .append(apkPath)
            .append("|")
            .append("findstr")
            .append("package")
            .build()
        commonPrint(command, onSuccess, onFailed)
    }

    /**
     * 权限
     */
    suspend fun permissionsFromApk(
        aapt2Path: String,
        apkPath: String,
        onSuccess: (msg: String) -> Unit,
        onFailed: (msg: String) -> Unit
    ) {
        val command = CommandBuilder()
            .append(aapt2Path)
            .append("dump")
            .append("badging")
            .append(apkPath)
            .append("|")
            .append("findstr")
            .append("permission")
            .build()
        commonPrint(command, onSuccess, onFailed)
    }

    private suspend fun commonPrint(
        command: List<String>,
        onSuccess: (msg: String) -> Unit,
        onFailed: (msg: String) -> Unit
    ) {
        val result = ShellCommand.runCommand(command)
        if (result.code == 0) {
            onSuccess.invoke(result.stdout)
        } else {
            onFailed.invoke("获取失败: ${result.stderr}")
        }
    }

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
        onSuccess: () -> Unit,
        onFailed: (msg: String) -> Unit,
    ) {
        val zipalignResult = runZipalign(zipalignPath, originFilepath, tempFilepath)
        if (zipalignResult.code == 0) {
            val signResult = runSign(apksignerPath, jksPath, alias, outputFile, tempFilepath, keystorePwd, keyPwd)
            if (signResult.code == 0) {
                onSuccess.invoke()
            } else {
                onFailed("签名失败: ${signResult.stderr}")
            }
        } else {
            onFailed("对齐失败: ${zipalignResult.stderr}")
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