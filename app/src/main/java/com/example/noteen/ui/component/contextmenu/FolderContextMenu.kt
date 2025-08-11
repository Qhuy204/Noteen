package com.example.noteen.ui.component.contextmenu

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@Composable
fun CustomDropdownMenu(
    isVisible: Boolean,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    if (isVisible) {
        val density = LocalDensity.current
        val offsetPx = with(density) {
            IntOffset(
                x = offset.x.toPx().roundToInt() - 180,
                y = offset.y.toPx().roundToInt() - 100
            )
        }

        Popup(
            offset = offsetPx,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp,
                color = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.wrapContentSize()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    MenuItem(text = "Edit", onClick = onEditClick)
                    MenuItem(text = "Delete", onClick = onDeleteClick)
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

data class MenuState(
    val isVisible: Boolean = false,
    val offset: DpOffset = DpOffset(0.dp, 0.dp),
    val itemId: Int? = null
)