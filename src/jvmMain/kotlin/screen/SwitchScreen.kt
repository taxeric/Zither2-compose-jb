package screen

import views.CustomTab
import views.SimpleRadioGroup
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SwitchScreen(
    modifier: Modifier = Modifier,
    onSelected: (Int) -> Unit
) {
    var selectedIndex by remember {
        mutableStateOf(0)
    }
    val tabs = mutableListOf<CustomTab>().apply {
        add(CustomTab("签名"))
        add(CustomTab("常用"))
    }
    SimpleRadioGroup(
        tabs = tabs,
        orientation = Orientation.Vertical,
        onSelected = { index, _ ->
            selectedIndex = index
            onSelected.invoke(selectedIndex)
        },
        defaultSelected = selectedIndex,
        modifier = modifier
            .fillMaxWidth(),
        contentModifier = Modifier
            .padding(24.dp, 0.dp)
    ) { tab, selected, childModifier ->
        Text(
            text = tab.text,
            textAlign = TextAlign.Center,
            color = Color.DarkGray,
            fontSize = 12.sp,
            modifier = childModifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (selected == tab.tag) Color.DarkGray else Color.Transparent,
                    RoundedCornerShape(4.dp)
                )
                .padding(0.dp, 12.dp)
        )
    }
}