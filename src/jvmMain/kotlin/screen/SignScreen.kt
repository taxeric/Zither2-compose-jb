package screen

import viewmodel.ShellViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import views.DragContent
import views.SwitchWithTitle

@Composable
fun SignScreen(
    vm: ShellViewModel,
    composeWindow: ComposeWindow
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {

        var unsignedApkPath by remember {
            mutableStateOf("选择文件, 支持拖拽")
        }
        var jksPath by remember {
            mutableStateOf("选择签名文件, 支持拖拽")
        }
        var useLocalConfigJks by remember {
            mutableStateOf(false)
        }

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
            onValueChange = {}
        )

        TitleWithTextField(
            title = "alias",
            enabled = {
                !useLocalConfigJks
            },
            onValueChange = {}
        )

        TitleWithTextField(
            title = "key pwd",
            enabled = {
                !useLocalConfigJks
            },
            onValueChange = {}
        )

        Button(
            onClick = {}
        ) {
            Text("Run")
        }
    }
}

@Composable
private fun TitleWithDragView(
    title: String,
    composeWindow: ComposeWindow,
    withBottomSpace: Boolean = false,
    value: String,
    enabled: () -> Boolean,
    onValueChange: (String) -> Unit
) {
    Text(
        title,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ){
        DragContent(
            modifier = Modifier
                .width(400.dp)
                .fillMaxHeight()
                .background(Color.White, RoundedCornerShape(20))
                .border(1.dp, Color.LightGray, RoundedCornerShape(20)),
            window = composeWindow,
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text(
                        value,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        ) {
            if (enabled()) {
                it.forEach { file ->
                    println(">>>> ${file.text}")
                }
                onValueChange.invoke(it.first().text)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            enabled = enabled(),
            modifier = Modifier,
            onClick = {}
        ) {
            Text("选择文件")
        }
    }
    if (withBottomSpace) {
        Spacer(modifier = Modifier.height(16.dp))
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