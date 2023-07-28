package viewmodel

import di.CommonSetting
import shell.RunCommandState
import di.ShellProcess
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import viewmodel.base.ScopeViewModel
import viewmodel.base.ScopeViewModelImpl
import java.io.File

class ShellViewModel(
    scope: CoroutineScope,
    private val delegate: ScopeViewModelImpl = ScopeViewModelImpl()
): ScopeViewModel by delegate {

    companion object {

        private const val cacheTempPathname = "cacheTempPath"
        private const val cacheTempFilename = "cacheTempApk.apk"
        private const val outputFilename = "signed.apk"
    }

    init {
        provideScope(scope)
    }

    private val tempPath = CommonSetting.basePath + File.separator + cacheTempPathname + File.separator
    private val tempApk = tempPath + cacheTempFilename

    private val _signState = MutableStateFlow<RunCommandState>(RunCommandState.Idle)
    val signState: StateFlow<RunCommandState> = _signState.asStateFlow()

    private fun buildOutputApkPath(): String = CommonSetting.outputPath + outputFilename

    fun runSign(
        originFilepath: String,
        jksPath: String,
        alias: String,
        outputFile: String = buildOutputApkPath(),
        keystorePwd: String,
        keyPwd: String,
    ) {
        delegate.scope.launch {
            withContext(Dispatchers.IO) {
                val cacheTempPath = File(tempPath)
                if (!cacheTempPath.exists()) {
                    cacheTempPath.mkdir()
                }
                val outputPath = File(CommonSetting.outputPath)
                if (!outputPath.exists()) {
                    outputPath.mkdir()
                }
            }
            _signState.tryEmit(RunCommandState.Running)
            ShellProcess.runSign(
                zipalignPath = CommonSetting.zipalignPath,
                apksignerPath = CommonSetting.apksignerPath,
                originFilepath = originFilepath,
                tempFilepath = tempApk,
                jksPath = jksPath,
                alias = alias,
                outputFile = outputFile,
                keystorePwd = keystorePwd,
                keyPwd = keyPwd,
                onSuccess = {
                    _signState.tryEmit(RunCommandState.Success)
                },
                onFailed = {
                    _signState.tryEmit(RunCommandState.Failed(it))
                },
            )
        }
    }
}