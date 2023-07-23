package views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.awt.Color
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import javax.swing.JPanel
import kotlin.math.roundToInt

@Composable
fun DragContent(
    modifier: Modifier = Modifier,
    window: ComposeWindow,
    content: @Composable BoxScope.() -> Unit,
    onDragFilepath: (List<TextFieldValue>) -> Unit
) {
    val component = remember {
        val c = ComposePanel()
        c.background = Color.BLUE
        val target = object : DropTarget() {
            override fun drop(evt: DropTargetDropEvent) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_REFERENCE)
                    val droppedFiles = evt
                        .transferable.getTransferData(
                            DataFlavor.javaFileListFlavor
                        ) as List<*>
                    val pathList = mutableListOf<TextFieldValue>()
                    droppedFiles.forEach {
                        val path = TextFieldValue((it as File).absolutePath)
                        pathList.add(path)
                    }
                    onDragFilepath.invoke(pathList)
                    evt.dropComplete(true)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    evt.dropComplete(false)
                }
            }
        }
        c.dropTarget = target
        c.isOpaque = false
        c
    }
    val pane = remember {
        window.rootPane
    }
    Box(
        modifier = modifier.onPlaced {
            val x = it.positionInWindow().x.roundToInt()
            val y = it.positionInWindow().y.roundToInt()
            val width = it.size.width
            val height = it.size.height
            component.setBounds(x, y, width, height)
        }
    ) {
        DisposableEffect(true) {
            pane.add(component)
            onDispose {
                runCatching {
                    pane.remove(component)
                }
            }
        }
        content.invoke(this)
    }
}