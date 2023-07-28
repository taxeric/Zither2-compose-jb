package views

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun SimpleRadioGroup(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    defaultSelected: Int = 0,
    tabs: List<BaseRadioTab>,
    onSelected: (Int, BaseRadioTab) -> Unit,
    content: @Composable (
        tab: BaseRadioTab,
        selected: String,
        childModifier: Modifier
    ) -> Unit = { _,_,_ -> },
) {
    if (tabs.isEmpty()) {
        return
    }
    var selectedTab by remember {
        mutableStateOf(tabs[defaultSelected].tag)
    }
    if (orientation == Orientation.Horizontal) {
        Row(
            modifier = modifier
                .selectableGroup()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemView(
                modifier = contentModifier,
                selectedTab = selectedTab,
                tabs = tabs,
                orientation = orientation,
                onSelected = { index, tab ->
                    selectedTab = tabs[index].tag
                    onSelected(index, tab)
                },
                content = content
            )
        }
    } else {
        Column(
            modifier = modifier
                .selectableGroup()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ItemView(
                modifier = contentModifier,
                selectedTab = selectedTab,
                tabs = tabs,
                orientation = orientation,
                onSelected = { index, tab ->
                    selectedTab = tabs[index].tag
                    onSelected(index, tab)
                },
                content = content
            )
        }
    }
}

@Composable
private fun ItemView(
    modifier: Modifier,
    selectedTab: String,
    tabs: List<BaseRadioTab>,
    orientation: Orientation = Orientation.Horizontal,
    onSelected: (Int, BaseRadioTab) -> Unit,
    content: @Composable (
        tab: BaseRadioTab,
        selected: String,
        childModifier: Modifier
    ) -> Unit = { _,_,_ -> },
) {
    tabs.forEachIndexed { index, tab ->
        Column {
            Box(
                modifier = modifier
                    .selectable(
                        selected = selectedTab == tab.tag,
                        onClick = {
                            onSelected(index, tabs[index])
                        },
                        role = Role.RadioButton
                    )
            ) {
                content(tab, selectedTab, Modifier.align(Alignment.Center))
            }
            if (index != tabs.size - 1) {
                if (orientation == Orientation.Horizontal) {
                    Spacer(Modifier.width(8.dp))
                } else if (orientation == Orientation.Vertical) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

open class BaseRadioTab(
    var text: String,
    var tag: String = text
)