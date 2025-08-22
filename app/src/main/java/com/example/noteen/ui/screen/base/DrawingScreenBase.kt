package com.example.noteen.ui.screen.base

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import com.example.noteen.data.model.Stroke
import com.example.noteen.ui.component.ToolOverlay
import com.example.noteen.ui.component.TransformState
import com.example.noteen.ui.screen.ImmersiveModeScreen
import com.example.noteen.utils.detectMultipleGestures
import com.example.noteen.utils.drawStroke
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.drawscope.withTransform

@Composable
fun DrawingScreenBase() {
    // Resources
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val minScale = 1f
    val maxScale = 8f
    val displayMetrics = remember {
        context.resources.displayMetrics
    }
    val screenWidthPx = displayMetrics.widthPixels
    val screenHeightPx = displayMetrics.heightPixels

    var showBitmapDialog by remember { mutableStateOf(false) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var backgroundColor by remember { mutableStateOf(Color(0xfff3f3f3)) }

    // Layout states
    var selectedButtonIndex by remember { mutableStateOf(0) }

    var canZoom by remember { mutableStateOf(true) }
    var canUndo by remember { mutableStateOf(true) }
    var canRedo by remember { mutableStateOf(false) }

    // Canvas states
    var transformState by remember { mutableStateOf(TransformState(3f, 0f, 0f)) }

    val strokes = remember { mutableStateListOf<Stroke>() }
    val currentStroke = remember { mutableStateListOf<Offset>() }

    // Function
    fun saveNote() {
        scope.launch {
            val bmp = createBitmap(screenWidthPx, screenHeightPx)
            val canvas = android.graphics.Canvas(bmp)
            val drawScope = CanvasDrawScope()
            drawScope.draw(
                density = density,
                layoutDirection = LayoutDirection.Ltr,
                canvas = androidx.compose.ui.graphics.Canvas(canvas),
                size = Size(screenWidthPx.toFloat(), screenHeightPx.toFloat())
            ) {
                drawRect(color = backgroundColor, size = size)
                strokes.forEach { stroke -> drawStroke(stroke) }
            }
//            val jsonString = createCanvasJson(strokes)
//            val fileName = FileManager.saveBitmap(bmp, Bitmap.CompressFormat.PNG)?.name ?: ""

        }
    }
    //

    ToolOverlay(
        selectedButtonIndex = selectedButtonIndex,
        lockState = canZoom,
        undoState = canUndo,
        redoState = canRedo,
        onHomeClick = {
            saveNote()
        },
        onLockClick = { canZoom = !canZoom },
        onUndoClick = { },
        onRedoClick = { },
        onSettingClick = { },
        onToolClick = { index, offset ->
            if (index != 5) selectedButtonIndex = index
        }
    )

    ImmersiveModeScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .clipToBounds()
        )
        {
            if (showBitmapDialog && previewBitmap != null) {
                Dialog(onDismissRequest = { showBitmapDialog = false }) {
                    Box(
                        modifier = Modifier
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "Canvas Preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                        )
                    }
                }
            }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = transformState.scale,
                        scaleY = transformState.scale,
                        translationX = transformState.offsetX,
                        translationY = transformState.offsetY
                    )
                    .pointerInput(Unit) {
                        detectMultipleGestures(
                            onTap = { offset ->
                                val newStroke = Stroke(
                                    listOf(offset)
                                )
                                strokes.add(newStroke)
                            },
                            onDrag = { initialPosition, changePosition, dragAmount ->
                                currentStroke.add(changePosition)
                            },
                            onDragEnd = {
                                val newStroke = Stroke(
                                    currentStroke.toList()
                                )
                                strokes.add(newStroke)
                            },
                            onDragCancel = {
                                currentStroke.clear()
                            },
                            onTransform = { centroid, zoom, pan ->
                                transformState = transformState.updateTransform(centroid, zoom, pan, screenWidthPx.toFloat(), screenHeightPx.toFloat(), minScale, maxScale, canZoom)
                            },
                            onGesturesEnd = {
                                currentStroke.clear()
                            }
                        )
                    }
            ) {
                if (currentStroke.isNotEmpty()) {
                    drawStroke(Stroke(currentStroke))
                }
                strokes.forEach { stroke -> drawStroke(stroke) }
            }
        }
    }
}
