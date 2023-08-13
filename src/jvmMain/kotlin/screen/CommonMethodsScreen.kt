package screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import views.TitleWithTextField

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
            1 -> CreateJKSView(composeWindow, shellViewModel)
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
            add("新建签名")
            add("杂项")
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
                    shellViewModel.analyseSignApkNative(apkPath)
                }
            ) {
                Text("获取支持架构")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    shellViewModel.analysePermissionsFromApk(apkPath)
                }
            ) {
                Text("获取apk权限")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
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

        SelectionContainer(
            modifier = Modifier
                .width(480.dp)
                .height(300.dp)
                .border(1.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                .background(Color.Transparent)
                .padding(8.dp)
        ) {
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CreateJKSView(
    composeWindow: ComposeWindow,
    shellViewModel: ShellViewModel,
) {
    var alias by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var ksPwd by remember { mutableStateOf("") }
    var saveLocation by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("unknown") }
    var organizationalUnit by remember { mutableStateOf("unknown") }
    var organizational by remember { mutableStateOf("unknown") }
    var location by remember { mutableStateOf("Hangzhou") }
    var province by remember { mutableStateOf("ZheJiang") }
    var country by remember { mutableStateOf("CN") }
    var filename by remember { mutableStateOf("new") }

    val runningState = shellViewModel.createSignState.collectAsState().value

    val idleState = fun () {
        shellViewModel.idleState()
    }

    if (runningState is RunCommandState.Success<*>) {
        AlertDialog(
            onDismissRequest = idleState,
            text = {
                Text("创建完成")
            },
            confirmButton = {
                Button(
                    onClick = idleState
                ) {
                    Text("Sure")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TitleWithTextField(
            title = "alias",
            enabled = { true },
            value = { alias },
            onValueChange = {
                alias = it
            }
        )

        TitleWithTextField(
            title = "pwd",
            enabled = { true },
            value = { pwd },
            onValueChange = {
                pwd = it
            }
        )

        TitleWithTextField(
            title = "ksPwd",
            enabled = { true },
            value = { ksPwd },
            onValueChange = {
                ksPwd = it
            }
        )

        Row {
            TitleWithTextField(
                title = "姓氏",
                textFieldWidth = 120.dp,
                textFieldSingleLine = true,
                enabled = { true },
                value = { username },
                onValueChange = {
                    username = it
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            TitleWithTextField(
                title = "组织单位",
                textFieldWidth = 120.dp,
                textFieldSingleLine = true,
                enabled = { true },
                value = { organizationalUnit },
                onValueChange = {
                    organizationalUnit = it
                }
            )
        }

        Row {
            TitleWithTextField(
                title = "组织",
                textFieldWidth = 120.dp,
                textFieldSingleLine = true,
                enabled = { true },
                value = { organizational },
                onValueChange = {
                    organizational = it
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            TitleWithTextField(
                title = "城市",
                textFieldWidth = 120.dp,
                textFieldSingleLine = true,
                enabled = { true },
                value = { location },
                onValueChange = {
                    location = it
                }
            )
        }

        Row {
            TitleWithTextField(
                title = "省",
                textFieldWidth = 120.dp,
                textFieldSingleLine = true,
                enabled = { true },
                value = { province },
                onValueChange = {
                    province = it
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            TitleWithTextField(
                title = "国家",
                textFieldWidth = 120.dp,
                textFieldSingleLine = true,
                enabled = { true },
                value = { country },
                onValueChange = {
                    country = it
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .width(500.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TitleWithTextField(
                title = "路径",
                enabled = { true },
                withBottomSpace = false,
                textFieldWidth = 250.dp,
                textFieldSingleLine = true,
                value = { saveLocation },
                onValueChange = {
                    saveLocation = it
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = filename,
                onValueChange = {
                    filename = it
                },
                singleLine = true,
                modifier = Modifier
                    .width(80.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(".jks")
        }

        Button(
            onClick = {
                shellViewModel.createJKS(
                    alias = alias,
                    pwd = pwd,
                    ksPwd = ksPwd,
                    validity = 10,
                    username = username,
                    organizationalUnit = organizationalUnit,
                    organizational = organizational,
                    location = location,
                    province = province,
                    country = country,
                    saveLocation = saveLocation,
                    filename = filename,
                )
            }
        ) {
            Text("create")
        }
    }
}