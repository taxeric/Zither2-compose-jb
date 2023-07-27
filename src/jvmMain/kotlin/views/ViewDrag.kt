package views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TitleWithDragView(
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