package screen

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.ui.window.Dialog
import entity.JksEntity
import shell.RunCommandState
import viewmodel.ConfigViewModel
import viewmodel.ShellViewModel
import views.SimpleRadioGroup
import views.SwitchWithTitle
import views.TitleWithDragView
import views.TitleWithTextField
import java.awt.Desktop

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SignScreen(
    shellVM: ShellViewModel,
    configVM: ConfigViewModel,
    composeWindow: ComposeWindow
) {
    val signState = shellVM.signState.collectAsState().value
    if (signState is RunCommandState.Success<*>) {
        AlertDialog(
            onDismissRequest = { shellVM.idleState() },
            text = {
                Text("签名完成")
            },
            confirmButton = {
                Button(
                    onClick = {
                        shellVM.idleState()
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        Desktop.getDesktop().open(shellVM.outputFile())
                        shellVM.idleState()
                    }
                ) {
                    Text("打开文件夹")
                }
            }
        )
    }
    var unsignedApkPath by remember { mutableStateOf("选择文件, 支持拖拽") }
    var jksPath by remember { mutableStateOf("选择签名文件, 支持拖拽") }
    var alias by remember { mutableStateOf("") }
    var keyPwd by remember { mutableStateOf("") }
    var ksPwd by remember { mutableStateOf("") }
    var currentChooseJks by remember { mutableStateOf(JksEntity.default) }
    var currentChooseJksIndex by remember { mutableStateOf(-1) }
    var useLocalConfigJks by remember { mutableStateOf(false) }
    var showEditJksDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {

        TitleWithDragView(
            "未签名APK",
            composeWindow = composeWindow,
            value = unsignedApkPath,
            withBottomSpace = true,
            enabled = { true }
        ) {
            unsignedApkPath = it
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            SwitchWithTitle(
                "选择已保存的配置",
                checked = useLocalConfigJks,
                onCheckedChange = {
                    useLocalConfigJks = it
                }
            )

            if (useLocalConfigJks) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "使用保存配置时点击Save后将覆盖配置",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }

        if (useLocalConfigJks) {
            ScrollableLocalSignView(
                modifier = Modifier
                    .width(400.dp),
                vm = configVM,
                onSelected = { jks, index ->
                    currentChooseJks = jks
                    currentChooseJksIndex = index
                    jksPath = jks.path
                    alias = jks.alias
                    ksPwd = jks.ksPwd
                    keyPwd = jks.pwd
                },
                onEdit = { index, jks ->
                    configVM.emitNeedEditJks(jks)
                    showEditJksDialog = true
                },
                onDelete = { index, jks ->
                    if (index == currentChooseJksIndex) {
                        jksPath = ""
                        alias = ""
                        ksPwd = ""
                        keyPwd = ""
                    }
                    configVM.deleteJksConfig(jks)
                }
            )
        }

        TitleWithDragView(
            "签名文件",
            composeWindow = composeWindow,
            value = jksPath,
            enabled = { true },
            withBottomSpace = true,
        ) {
            jksPath = it
        }

        TitleWithTextField(
            title = "ks pwd",
            enabled = { true },
            value = { ksPwd },
            onValueChange = {
                ksPwd = it
            }
        )

        TitleWithTextField(
            title = "alias",
            enabled = { true },
            value = { alias },
            onValueChange = {
                alias = it
            }
        )

        TitleWithTextField(
            title = "key pwd",
            enabled = { true },
            value = { keyPwd },
            onValueChange = {
                keyPwd = it
            }
        )

        Row {

            Button(
                enabled = true,
                onClick = {
                    var isNewJks = true
                    val jksEntity = if (useLocalConfigJks) {
                        if (currentChooseJks.fileUid.isEmpty() || currentChooseJks.default) {
                            JksEntity.buildEntityByPath(
                                path = jksPath,
                                pwd = keyPwd,
                                alias = alias,
                                ksPwd = ksPwd
                            )
                        } else {
                            isNewJks = false
                            currentChooseJks.copy(
                                path = jksPath,
                                pwd = keyPwd,
                                alias = alias,
                                ksPwd = ksPwd
                            )
                        }
                    } else {
                        JksEntity.buildEntityByPath(
                            path = jksPath,
                            pwd = keyPwd,
                            alias = alias,
                            ksPwd = ksPwd
                        )
                    }
                    configVM.writeJksConfig(jksEntity, isNewJks) {
                        configVM.readJksConfig()
                    }
                }
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    shellVM.runSign(
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

    if (showEditJksDialog) {
        JksEditView(
            configVM,
            composeWindow
        ) {
            showEditJksDialog = false
        }
    }
}

@Composable
private fun JksEditView(
    vm: ConfigViewModel,
    composeWindow: ComposeWindow,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onCloseRequest = onDismissRequest,
        title = "签名编辑",
        resizable = true,
    ) {
        val needEditJks = vm.needEditJksEntity.collectAsState().value
        var newJksPath by remember { mutableStateOf(needEditJks.path) }
        var newAlias by remember { mutableStateOf(needEditJks.alias) }
        var newKeyPwd by remember { mutableStateOf(needEditJks.pwd) }
        var newKsPwd by remember { mutableStateOf(needEditJks.ksPwd) }
        var newFilename by remember { mutableStateOf(needEditJks.name) }
        Column(
            modifier = Modifier
        ) {
            TitleWithDragView(
                "选择签名",
                composeWindow = composeWindow,
                value = newJksPath,
                enabled = { true },
                withBottomSpace = true,
            ) {
                newJksPath = it
            }

            TitleWithTextField(
                title = "jks name",
                enabled = { true },
                value = { newFilename },
                onValueChange = {
                    newFilename = it
                }
            )

            TitleWithTextField(
                title = "ks pwd",
                enabled = { true },
                value = { newKsPwd },
                onValueChange = {
                    newKsPwd = it
                }
            )

            TitleWithTextField(
                title = "alias",
                enabled = { true },
                value = { newAlias },
                onValueChange = {
                    newAlias = it
                }
            )

            TitleWithTextField(
                title = "key pwd",
                enabled = { true },
                value = { newKeyPwd },
                onValueChange = {
                    newKeyPwd = it
                }
            )

            Row {
                Button(
                    onClick = {
                        vm.saveEditJksConfig(
                            needEditJks.copy(
                                name = newFilename,
                                path = newJksPath,
                                alias = newAlias,
                                pwd = newKeyPwd,
                                ksPwd = newKsPwd
                            )
                        )
                        onDismissRequest.invoke()
                    }
                ) {
                    Text("确定")
                }
                Button(
                    onClick = {
                        onDismissRequest.invoke()
                    }
                ) {
                    Text("取消")
                }
            }
        }
    }
}

@Composable
private fun ScrollableLocalSignView(
    modifier: Modifier = Modifier,
    vm: ConfigViewModel,
    onSelected: (JksEntity, Int) -> Unit,
    onEdit: (Int, JksEntity) -> Unit = {_,_ ->},
    onDelete: (Int, JksEntity) -> Unit = {_,_ ->},
) {
    val jksList = vm.jksConfigFlow.collectAsState().value
    var selectedIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        if (jksList.isNotEmpty()) {
            onSelected.invoke(jksList[selectedIndex], selectedIndex)
        }
    }
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
    ) { index, tab, selected, childModifier ->
        ContextMenuArea(
            items = {
                listOf(
                    ContextMenuItem("编辑") {
                        onEdit.invoke(index, jksList[index])
                    },
                    ContextMenuItem("删除") {
                        onDelete.invoke(index, jksList[index])
                    },
                )
            }
        ) {
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
    }
    Spacer(modifier = Modifier.height(16.dp))
}
