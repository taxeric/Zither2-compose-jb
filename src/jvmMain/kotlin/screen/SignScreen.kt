package screen

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import viewmodel.ShellViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import entity.JksEntity
import viewmodel.ConfigViewModel
import views.SimpleRadioGroup
import views.SwitchWithTitle
import views.TitleWithDragView

@Composable
fun SignScreen(
    shellVM: ShellViewModel,
    configVM: ConfigViewModel,
    composeWindow: ComposeWindow
) {
    val signState = shellVM.signState.collectAsState().value
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
    var currentChooseJks by remember {
        mutableStateOf(JksEntity.default)
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

        if (useLocalConfigJks) {
            ScrollableLocalSignView(
                modifier = Modifier
                    .width(400.dp),
                vm = configVM
            ) { jks, index ->
                currentChooseJks = jks
            }
        }

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
            title = "ks pwd",
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

        Row {

            Button(
                enabled = !useLocalConfigJks,
                onClick = {
                    val jksEntity = JksEntity.buildEntityByPath(
                        path = jksPath,
                        pwd = keyPwd,
                        alias = alias,
                        ksPwd = ksPwd
                    )
                    configVM.writeJksConfig(jksEntity)
                }
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    shellVM.runSign(
                        originFilepath = unsignedApkPath,
                        jksPath = if (useLocalConfigJks) currentChooseJks.path else jksPath,
                        alias = if (useLocalConfigJks) currentChooseJks.alias else alias,
                        keyPwd = if (useLocalConfigJks) currentChooseJks.pwd else keyPwd,
                        keystorePwd = if (useLocalConfigJks) currentChooseJks.ksPwd else ksPwd,
                    )
                }
            ) {
                Text("Run")
            }
        }
    }
}

@Composable
private fun ScrollableLocalSignView(
    modifier: Modifier = Modifier,
    vm: ConfigViewModel,
    onSelected: (JksEntity, Int) -> Unit
) {
    val jksList = vm.jksConfigFlow.collectAsState().value
    var selectedIndex by remember { mutableStateOf(0) }
    SimpleRadioGroup(
        tabs = jksList,
        orientation = Orientation.Horizontal,
        onSelected = { index, _ ->
            selectedIndex = index
            onSelected.invoke(jksList[selectedIndex], selectedIndex)
        },
        defaultSelected = selectedIndex,
        modifier = modifier,
        contentModifier = Modifier
            .padding(8.dp, 0.dp)
    ) { tab, selected, childModifier ->
        Text(
            text = tab.text,
            textAlign = TextAlign.Center,
            color = Color.DarkGray,
            fontSize = 12.sp,
            modifier = childModifier
                .border(
                    1.dp,
                    if (selected == tab.tag) Color.DarkGray else Color.Transparent,
                    RoundedCornerShape(4.dp)
                )
                .padding(12.dp, 8.dp)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
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