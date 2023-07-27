package screen

import viewmodel.ShellViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import views.SwitchWithTitle
import views.TitleWithDragView

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SignScreen(
    vm: ShellViewModel,
    composeWindow: ComposeWindow
) {
    val signState = vm.signState.collectAsState().value
    var unsignedApkPath by remember {
        mutableStateOf("选择文件, 支持拖拽")
    }
    var jksPath by remember {
        mutableStateOf("选择签名文件, 支持拖拽")
    }
    var alias by remember {
        mutableStateOf("")
    }
    var keyPwd by remember {
        mutableStateOf("")
    }
    var ksPwd by remember {
        mutableStateOf("")
    }
    var useLocalConfigJks by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {

        TitleWithDragView(
            "选择未签名APK",
            composeWindow = composeWindow,
            value = unsignedApkPath,
            withBottomSpace = true,
            enabled = { true }
        ) {
            unsignedApkPath = it
        }

        SwitchWithTitle(
            "使用本地配置",
            checked = useLocalConfigJks,
            onCheckedChange = {
                useLocalConfigJks = it
            }
        )

        TitleWithDragView(
            "选择签名",
            composeWindow = composeWindow,
            value = jksPath,
            enabled = {
                !useLocalConfigJks
            },
            withBottomSpace = true,
        ) {
            jksPath = it
        }

        TitleWithTextField(
            title = "keystore pwd",
            enabled = {
                !useLocalConfigJks
            },
            onValueChange = {
                ksPwd = it
            }
        )

        TitleWithTextField(
            title = "alias",
            enabled = {
                !useLocalConfigJks
            },
            onValueChange = {
                alias = it
            }
        )

        TitleWithTextField(
            title = "key pwd",
            enabled = {
                !useLocalConfigJks
            },
            onValueChange = {
                keyPwd = it
            }
        )

        Button(
            onClick = {
                vm.runSign(
                    originFilepath = unsignedApkPath,
                    jksPath = jksPath,
                    alias = alias,
                    keyPwd = keyPwd,
                    keystorePwd = ksPwd,
                )
            }
        ) {
            Text("Run")
        }
    }
}

@Composable
private fun TitleWithTextField(
    title: String,
    enabled: () -> Boolean,
    withBottomSpace: Boolean = true,
    onValueChange: (String) -> Unit
) {
    var text by remember {
        mutableStateOf("")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            modifier = Modifier
                .width(80.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = text,
            onValueChange = {
                text = it
                onValueChange.invoke(it)
            },
            enabled = enabled(),
            modifier = Modifier
                .width(400.dp)
        )
    }
    if (withBottomSpace) {
        Spacer(modifier = Modifier.height(16.dp))
    }
}