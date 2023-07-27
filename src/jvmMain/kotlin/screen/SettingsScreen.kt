package screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import di.CommonSetting
import entity.SettingEntity
import views.TitleWithDragView

@Composable
fun SettingsScreen(
    composeWindow: ComposeWindow
) {
    val settings = CommonSetting.commonSettingFlow.collectAsState().value

    var commonOutputPath by remember { mutableStateOf(CommonSetting.outputPath) }
    var localZipalignPath by remember { mutableStateOf(settings.zipalignPath) }
    var localApksignerPath by remember { mutableStateOf(settings.apksignerPath) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TitleWithDragView(
            "本地zipalign路径",
            composeWindow = composeWindow,
            value = localZipalignPath,
            withBottomSpace = true,
            enabled = { true }
        ) {
            localZipalignPath = it
        }

        TitleWithDragView(
            "本地apksigner路径",
            composeWindow = composeWindow,
            value = localApksignerPath,
            withBottomSpace = true,
            enabled = { true }
        ) {
            localApksignerPath = it
        }

        TitleWithDragView(
            "通用输出路径",
            composeWindow = composeWindow,
            value = commonOutputPath,
            withBottomSpace = true,
            enabled = { false }
        ) {}

        Button(
            onClick = {
                CommonSetting.writeSetting(
                    SettingEntity(
                        outputPath = commonOutputPath,
                        zipalignPath = localZipalignPath,
                        apksignerPath = localApksignerPath
                    )
                )
            }
        ) {
            Text("Save")
        }
    }
}