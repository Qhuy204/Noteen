package com.example.noteen.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.noteen.R

data class TransformState(val scale: Float, val offsetX: Float, val offsetY: Float) {
    fun updateTransform(
        centroid: Offset,
        zoom: Float,
        pan: Offset,
        canvasWidthPx: Float,
        canvasHeightPx: Float,
        minScale: Float,
        maxScale: Float,
        zoomEnabled: Boolean = true
    ): TransformState {
        val newScale = if (zoomEnabled) {
            val zoomSpeedLimit = 0.6f
            val limitedZoom = (zoom - 1f) * zoomSpeedLimit + 1f
            (scale * limitedZoom).coerceIn(minScale, maxScale)
        } else {
            scale
        }

        val scaleFactor = newScale / scale

        val canvasCenterX = canvasWidthPx / 2
        val canvasCenterY = canvasHeightPx / 2

        val newOffsetX =
            offsetX + (centroid.x - canvasCenterX - offsetX) * (1 - scaleFactor)
        val newOffsetY =
            offsetY + (centroid.y - canvasCenterY - offsetY) * (1 - scaleFactor)

        val scaledWidth = canvasWidthPx * newScale
        val scaledHeight = canvasHeightPx * newScale

        val maxOffsetX = ((scaledWidth - canvasWidthPx) / 2).coerceAtLeast(0f)
        val maxOffsetY = ((scaledHeight - canvasHeightPx) / 2).coerceAtLeast(0f)

        return TransformState(
            scale = newScale,
            offsetX = (newOffsetX + pan.x * newScale).coerceIn(-maxOffsetX, maxOffsetX),
            offsetY = (newOffsetY + pan.y * newScale).coerceIn(-maxOffsetY, maxOffsetY)
        )
    }

    fun updateDrag(
        dragAmount: Offset,
        canvasWidthPx: Float,
        canvasHeightPx: Float
    ): TransformState {
        val scaledWidth = canvasWidthPx * scale
        val scaledHeight = canvasHeightPx * scale

        val maxOffsetX = ((scaledWidth - canvasWidthPx) / 2).coerceAtLeast(0f)
        val maxOffsetY = ((scaledHeight - canvasHeightPx) / 2).coerceAtLeast(0f)

        val newOffsetX = (offsetX + dragAmount.x * scale).coerceIn(-maxOffsetX, maxOffsetX)
        val newOffsetY = (offsetY + dragAmount.y * scale).coerceIn(-maxOffsetY, maxOffsetY)

        return copy(offsetX = newOffsetX, offsetY = newOffsetY)
    }
}

@Composable
fun ToolOverlay(
    selectedButtonIndex: Int = 0,
    lockState: Boolean = false,
    undoState: Boolean = true,
    redoState: Boolean = false,
    onHomeClick: () -> Unit,
    onLockClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onSettingClick: () -> Unit,
    onToolClick: (Int) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().zIndex(1f)) {
        ActionBar(
            buttons = listOf(
                ActionButton(iconRes = R.drawable.cv_home, contentDescription = "Home", onClick = onHomeClick)
            ),
            alignment = Alignment.TopStart,
            padding = PaddingValues(top = 40.dp, start = 20.dp)
        )
        ActionBar(
            buttons = listOf(
                ActionButton(iconRes = if (lockState) R.drawable.cv_unlock else R.drawable.cv_lock, contentDescription = "Lock", onClick = onLockClick)
            ),
            alignment = Alignment.TopStart,
            padding = PaddingValues(top = 40.dp, start = 80.dp),
            enabled = lockState
        )
        ActionBar(
            buttons = listOf(
                ActionButton(iconRes = R.drawable.cv_undo, contentDescription = "Undo", enable = undoState, onClick = onUndoClick),
                ActionButton(iconRes = R.drawable.cv_redo, contentDescription = "Redo", enable = redoState, onClick = onRedoClick)
            ),
            alignment = Alignment.TopEnd,
            padding = PaddingValues(top = 40.dp, end = 80.dp),
            iconSize = 16.dp
        )
        ActionBar(
            buttons = listOf(
                ActionButton(iconRes = R.drawable.ellipsis_vertical, contentDescription = "Menu", onClick = onSettingClick)
            ),
            alignment = Alignment.TopEnd,
            padding = PaddingValues(top = 40.dp, end = 20.dp)
        )
        val icons = listOf(
            R.drawable.cv_touch,
            R.drawable.cv_pen,
            R.drawable.cv_highlighter,
            R.drawable.cv_ruler,
            R.drawable.cv_eraser,
            R.drawable.cv_sticker,
            R.drawable.cv_selection
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 15.dp)
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(100.dp)
                    .align(Alignment.BottomCenter)
                    .padding(25.dp)
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(10.dp),
                shadowElevation = 3.dp,
                color = Color.White,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    icons.forEachIndexed { index, iconRes ->
                        PressEffectIconButton(
                            onClick = { onToolClick(index) },
                            selected = selectedButtonIndex == index,
                            icon = painterResource(iconRes),
                            selectedIconColor = Color(0xFF1966FF),
                            selectedBackgroundColor = Color.Blue.copy(alpha = 0.08f),
                            unselectedIconColor = Color.DarkGray,
                            iconPadding = 10.dp,
                            cornerShape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionBar(
    buttons: List<ActionButton>,
    alignment: Alignment = Alignment.Center,
    padding: PaddingValues = PaddingValues(0.dp),
    enabled: Boolean = true,
    iconSize: Dp = 20.dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Surface(
            modifier = Modifier
                .width((buttons.size * 50).dp)
                .height(50.dp)
                .align(alignment),
            shape = RoundedCornerShape(10.dp),
            color = if (enabled) Color.White else Color(0xFFF0F0F0),
            shadowElevation = 2.dp
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                buttons.forEach { button ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable(enabled = button.enable, onClick = button.onClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = button.iconRes),
                            contentDescription = button.contentDescription,
                            modifier = Modifier.size(iconSize),
                            tint = if (button.enable) Color.DarkGray else Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

data class ActionButton(
    @DrawableRes val iconRes: Int,
    val contentDescription: String,
    val enable: Boolean = true,
    val onClick: () -> Unit
)
