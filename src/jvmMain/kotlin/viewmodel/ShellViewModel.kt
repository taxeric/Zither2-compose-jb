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

    /**
     * 重签名状态
     */
    private val _signState = MutableStateFlow<RunCommandState>(RunCommandState.Idle)
    val signState: StateFlow<RunCommandState> = _signState.asStateFlow()

    /**
     * 创建签名状态
     */
    private val _createSignState = MutableStateFlow<RunCommandState>(RunCommandState.Idle)
    val createSignState: StateFlow<RunCommandState> = _createSignState.asStateFlow()

    /**
     * APK相关状态
     */
    private val _singedApkInfoState = MutableStateFlow<RunCommandState>(RunCommandState.Idle)
    val signedApkInfoState: StateFlow<RunCommandState> = _singedApkInfoState.asStateFlow()

    private fun buildOutputApkPath(): String = CommonSetting.outputPath + outputFilename

    fun idleState() {
        _signState.tryEmit(RunCommandState.Idle)
        _singedApkInfoState.tryEmit(RunCommandState.Idle)
        _createSignState.tryEmit(RunCommandState.Idle)
    }

    fun outputFile() = File(CommonSetting.outputPath)

    fun analyseSignVersion(
        apkPath: String
    ) {
        delegateRunWithState(_singedApkInfoState) {
            ShellProcess.signVersionFromApk(
                apksignerPath = CommonSetting.apksignerPath,
                apkPath = apkPath,
                onSuccess = {
                    _singedApkInfoState.tryEmit(RunCommandState.Success(it))
                },
                onFailed = {
                    _singedApkInfoState.tryEmit(RunCommandState.Failed(it))
                }
            )
        }
    }

    fun analyseSignInfoFromApk(
        apkPath: String
    ) {
        delegateRunWithState(_singedApkInfoState) {
            ShellProcess.signInfoFromApk(
                keytoolPath = CommonSetting.keytoolPath,
                apkPath = apkPath,
                onSuccess = {
                    _singedApkInfoState.tryEmit(RunCommandState.Success(it))
                },
                onFailed = {
                    _singedApkInfoState.tryEmit(RunCommandState.Failed(it))
                }
            )
        }
    }

    fun analyseBaseInfoFromApk(
        apkPath: String
    ) {
        delegateRunWithState(_singedApkInfoState) {
            ShellProcess.baseInfoFromApk(
                aapt2Path = CommonSetting.aapt2Path,
                apkPath = apkPath,
                onSuccess = {
                    val result = it.replace(" ", "\n")
                    _singedApkInfoState.tryEmit(RunCommandState.Success(result))
                },
                onFailed = {
                    _singedApkInfoState.tryEmit(RunCommandState.Failed(it))
                }
            )
        }
    }

    fun analyseSignApkNative(
        apkPath: String
    ) {
        delegateRunWithState(_singedApkInfoState) {
            ShellProcess.signApkNative(
                aapt2Path = CommonSetting.aapt2Path,
                apkPath = apkPath,
                onSuccess = {
                    _singedApkInfoState.tryEmit(RunCommandState.Success(it))
                },
                onFailed = {
                    _singedApkInfoState.tryEmit(RunCommandState.Failed(it))
                }
            )
        }
    }

    fun analysePermissionsFromApk(
        apkPath: String
    ) {
        delegateRunWithState(_singedApkInfoState) {
            ShellProcess.permissionsFromApk(
                aapt2Path = CommonSetting.aapt2Path,
                apkPath = apkPath,
                onSuccess = {
                    _singedApkInfoState.tryEmit(RunCommandState.Success(it))
                },
                onFailed = {
                    _singedApkInfoState.tryEmit(RunCommandState.Failed(it))
                }
            )
        }
    }

    fun createJKS(
        alias: String,
        pwd: String,
        ksPwd: String,
        validity: Int,
        username: String,
        organizationalUnit: String,
        organizational: String,
        location: String,
        province: String,
        country: String,
        saveLocation: String,
        filename: String,
    ) {
        delegateRunWithState(_createSignState) {
            ShellProcess.createJKS(
                keytoolPath = CommonSetting.keytoolPath,
                alias = alias,
                pwd = pwd,
                ksPwd = ksPwd,
                validity = validity,
                username = username,
                organizationalUnit = organizationalUnit,
                organizational = organizational,
                location = location,
                province = province,
                country = country,
                saveLocation = saveLocation,
                filename = "$filename.jks",
                onSuccess = {
                    _createSignState.tryEmit(RunCommandState.Success(null))
                },
                onFailed = {
                    _createSignState.tryEmit(RunCommandState.Failed(it))
                }
            )
        }
    }

    fun runSign(
        originFilepath: String,
        jksPath: String,
        alias: String,
        outputFile: String = buildOutputApkPath(),
        keystorePwd: String,
        keyPwd: String,
    ) {
        delegateRunWithState(_signState) {
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
                    _signState.tryEmit(RunCommandState.Success(null))
                },
                onFailed = {
                    _signState.tryEmit(RunCommandState.Failed(it))
                },
            )
        }
    }

    private fun delegateRunWithState(
        state: MutableStateFlow<RunCommandState>,
        block: suspend () -> Unit
    ) {
        delegate.scope.launch {
            state.tryEmit(RunCommandState.Running)
            block.invoke()
        }
    }
}