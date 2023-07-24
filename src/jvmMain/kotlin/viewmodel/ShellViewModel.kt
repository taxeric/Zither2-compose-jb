package viewmodel

import shell.RunCommandState
import di.ShellProcess
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import viewmodel.base.ScopeViewModel
import viewmodel.base.ScopeViewModelImpl

@OptIn(DelicateCoroutinesApi::class)
class ShellViewModel(
    private val delegate: ScopeViewModelImpl = ScopeViewModelImpl()
): ScopeViewModel by delegate {

    init {
        provideScope(GlobalScope)
    }

    private val _signState = MutableStateFlow<RunCommandState>(RunCommandState.Idle)
    val signState: StateFlow<RunCommandState> = _signState.asStateFlow()

    fun runSign(
        originFilepath: String,
        jksPath: String,
        alias: String,
        outputFile: String,
        keystorePwd: String,
        keyPwd: String,
    ) {
        delegate.scope.launch {
            _signState.tryEmit(RunCommandState.Running)
            ShellProcess.runSign(
                zipalignPath = "",
                apksignerPath = "",
                originFilepath = originFilepath,
                tempFilepath = "",
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