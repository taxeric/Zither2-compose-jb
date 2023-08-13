package views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TitleWithTextField(
    title: String,
    enabled: () -> Boolean,
    withBottomSpace: Boolean = true,
    textFieldWidth: Dp = 400.dp,
    textFieldSingleLine: Boolean = false,
    value: () -> String,
    onValueChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (title.isNotEmpty()) {
            Text(
                title,
                modifier = Modifier
                    .width(80.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        TextField(
            value = value(),
            onValueChange = {
                onValueChange.invoke(it)
            },
            singleLine = textFieldSingleLine,
            enabled = enabled(),
            modifier = Modifier
                .width(textFieldWidth)
        )
    }
    if (withBottomSpace) {
        Spacer(modifier = Modifier.height(16.dp))
    }
}