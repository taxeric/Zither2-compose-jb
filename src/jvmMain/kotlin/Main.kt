import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import di.CommonSetting
import screen.SettingsScreen
import screen.SignScreen
import screen.SwitchScreen
import viewmodel.ShellViewModel

@Composable
@Preview
fun App(
    composeWindow: ComposeWindow
) {
    val vm = remember { ShellViewModel() }

    var selectedIndex by remember {
        mutableStateOf(0)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(vertical = 8.dp)
    ) {
        SwitchScreen(
            modifier = Modifier
                .weight(2f)
        ) {
            selectedIndex = it
        }
        Box(
            modifier = Modifier
                .weight(4f)
        ) {
            when (selectedIndex) {
                0 -> SignScreen(vm, composeWindow)
                1 -> SettingsScreen(composeWindow)
            }
        }
    }
}

fun main() = application {
    CommonSetting.readSetting()
    Window(onCloseRequest = ::exitApplication) {
        App(this.window)
    }
}
