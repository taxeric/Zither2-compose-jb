package screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import shell.RunCommandState
import viewmodel.ShellViewModel
import views.TitleWithDragView

@Composable
fun CommonMethodsScreen(
    shellViewModel: ShellViewModel,
    composeWindow: ComposeWindow
) {
    var selectedIndex by remember {
        mutableStateOf(0)
    }
    Column {
        SwitchMethodsTab { index ->
            selectedIndex = index
        }
        when (selectedIndex) {
            0 -> ApkMethodsView(composeWindow, shellViewModel)
        }
    }
}

@Composable
private fun SwitchMethodsTab(
    onSelectedIndex: (Int) -> Unit
) {
    val tabs = remember {
        mutableStateListOf<String>().apply {
            add("APK相关")
            add("其他")
        }
    }
    var selectedIndex by remember {
        mutableStateOf(0)
    }
    TabRow(
        selectedTabIndex = selectedIndex,
        backgroundColor = Color.Transparent,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = Color.Red
            )
        }
    ) {
        tabs.forEachIndexed { i, str ->
            Tab(
                text = {
                    Text(str)
                },
                selected = selectedIndex == i,
                onClick = {
                    selectedIndex = i
                    onSelectedIndex.invoke(i)
                }
            )
        }
    }
}

@Composable
private fun ApkMethodsView(
    composeWindow: ComposeWindow,
    shellViewModel: ShellViewModel
) {
    val parseValue = shellViewModel.signedApkInfoState.collectAsState().value

    var apkPath by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    if (parseValue is RunCommandState.Success<*>) {
        error = false
        info = (parseValue.data as? String)?: "Unknown Data"
    }
    if (parseValue is RunCommandState.Failed) {
        error = true
        info = parseValue.msg
    }
    if (parseValue is RunCommandState.Idle) {
        error = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        TitleWithDragView(
            "APK文件",
            composeWindow = composeWindow,
            value = apkPath,
            enabled = { true },
            withBottomSpace = true,
        ) {
            apkPath = it
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Button(
                onClick = {
                    shellViewModel.analyseBaseInfoFromApk(apkPath)
                }
            ) {
                Text("获取APK信息")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    shellViewModel.analysePermissionsFromApk(apkPath)
                }
            ) {
                Text("获取apk权限")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    shellViewModel.analyseSignInfoFromApk(apkPath)
                }
            ) {
                Text("获取签名信息")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    shellViewModel.analyseSignVersion(apkPath)
                }
            ) {
                Text("获取签名版本")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                shellViewModel.idleState()
                info = ""
            }
        ) {
            Text("Reset")
        }

        Spacer(modifier = Modifier.height(16.dp))

        SelectionContainer {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = if (error) Color.Red else Color.Black)) {
                        append(info)
                    }
                },
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}