package com.example.noteen.ui.screen

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.example.noteen.data.model.Stroke
import com.example.noteen.ui.component.ToolOverlay
import com.example.noteen.ui.component.TransformState
import com.example.noteen.utils.detectMultipleGestures
import com.example.noteen.utils.drawStroke
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import com.example.noteen.data.LocalFileManager.FileManager
import com.example.noteen.data.LocalRepository.entity.NoteEntity
import com.example.noteen.data.model.createCanvasJson
import com.example.noteen.data.model.loadCanvasJson
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.noteen.R
import com.example.noteen.data.model.ConfirmAction
import com.example.noteen.ui.component.bottomsheet.DrawingBackGroundPickerBottomSheet
import com.example.noteen.ui.component.bottomsheet.NoteBackGroundPickerBottomSheet
import com.example.noteen.ui.component.contextmenu.AnchoredContextMenu
import com.example.noteen.ui.component.contextmenu.ContextMenuItem
import com.example.noteen.ui.component.contextmenu.FloatingContextMenu
import com.example.noteen.ui.component.dialog.ConfirmDialog
import com.example.noteen.viewmodel.NoteDetailViewModel

@Composable
fun DrawingScreen(
    selectedNote: NoteEntity,
    onGoBack: (jsonString: String, fileName: String) -> Unit = { _, _ -> }
) {
    // Resources
    val context = LocalContext.current
    val density = LocalDensity.current
    val minScale = 1f
    val maxScale = 8f
    val displayMetrics = remember {
        context.resources.displayMetrics
    }
    val screenWidthPx = displayMetrics.widthPixels
    val screenHeightPx = displayMetrics.heightPixels

    var showBitmapDialog by remember { mutableStateOf(false) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var backgroundColor by remember { mutableStateOf(Color.White) }

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
    LaunchedEffect(Unit) {
        loadCanvasJson(selectedNote.content, strokes)
        backgroundColor = Color(selectedNote.color.toColorInt())
    }

    var confirmAction by remember { mutableStateOf<ConfirmAction?>(null) }

    ConfirmDialog(
        action = confirmAction,
        onDismiss = { confirmAction = null }
    )

    var showBackgroundBottomSheet by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf("#FFFFFF") }
    var selectedPattern by remember { mutableStateOf("None") }

    DrawingBackGroundPickerBottomSheet(
        visible = showBackgroundBottomSheet,
        onDismiss = { showBackgroundBottomSheet = false },
        selectedColorHex = selectedColor,
        onColorSelected = { selectedColor = it },
        selectedPattern = selectedPattern,
        onPatternSelected = { selectedPattern = it }
    )

    var stickerDialogOffset by remember { mutableStateOf(DpOffset.Zero) }

    fun addShape() {
        stickerDialogOffset = DpOffset.Zero
    }
    fun addImage() {
        stickerDialogOffset = DpOffset.Zero
    }

    FloatingContextMenu(
        visible = stickerDialogOffset != DpOffset.Zero,
        offset = DpOffset(stickerDialogOffset.x - 30.dp, stickerDialogOffset.y - 120.dp),
        onDismiss = { stickerDialogOffset = DpOffset.Zero },
    ) {
        ContextMenuItem("Shape", R.drawable.cv_shape, width = 120.dp, onClick = { addShape() })
        ContextMenuItem("Image", R.drawable.cv_image, width = 120.dp, onClick = { addImage() })
    }

    var showMenuContext by remember { mutableStateOf(false) }

    fun pinNote() {
        showMenuContext = false
    }

    fun setBackground() {
        showMenuContext = false
        showBackgroundBottomSheet = true
    }

    fun lockNote() {
        showMenuContext = false
        confirmAction = ConfirmAction(
            title = "Lock Note",
            message = "Are you sure you want to lock this note?",
            action = {  }
        )
    }

    fun deleteNote() {
        showMenuContext = false
        confirmAction = ConfirmAction(
            title = "Delete Note",
            message = "Are you sure you want to delete this note?",
            action = {  }
        )
    }

    AnchoredContextMenu(
        visible = showMenuContext,
        onDismiss = { showMenuContext = false }
    ) {
        ContextMenuItem("Pin", R.drawable.pin, onClick = { pinNote() })
        ContextMenuItem("Set background", R.drawable.palette, onClick = { setBackground() })
        ContextMenuItem("Add to locked folder", R.drawable.lock_keyhole, onClick = { lockNote() })
        ContextMenuItem("Delete", R.drawable.trash, onClick = { deleteNote() }, contentColor = Color.Red)
    }

    fun saveNote() {
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

        val savedFile = FileManager.saveBitmap(bmp, Bitmap.CompressFormat.PNG)
        val jsonString = createCanvasJson(strokes)
        val fileName = savedFile?.name ?: ""

        onGoBack(jsonString, fileName)
    }

    ToolOverlay(
        selectedButtonIndex = selectedButtonIndex,
        lockState = canZoom,
        undoState = canUndo,
        redoState = canRedo,
        onHomeClick = {
            saveNote()
        },
        onLockClick = { canZoom = !canZoom },
        onUndoClick = {},
        onRedoClick = {},
        onSettingClick = { showMenuContext = true },
        onToolClick = { index, offset ->
            if (index != 5) selectedButtonIndex = index
            else stickerDialogOffset = offset
        }
    )

    BackHandler {
        if (!showBackgroundBottomSheet && !showMenuContext) saveNote()
        if (showBackgroundBottomSheet) showBackgroundBottomSheet = false
        if (showMenuContext) showMenuContext = false
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clipToBounds()
    ) {
        val canvasWidthDp = maxWidth
        val canvasHeightDp = maxHeight
        val canvasWidthPx = with(density) { canvasWidthDp.toPx() }
        val canvasHeightPx = with(density) { canvasHeightDp.toPx() }

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
                            transformState = transformState.updateTransform(centroid, zoom, pan, canvasWidthPx, canvasHeightPx, minScale, maxScale, canZoom)
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

@SuppressLint("WrongConstant")
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ImmersiveModeScreen(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            controller.hide(android.view.WindowInsets.Type.systemBars())
        }

        onDispose {
            activity?.window?.let { window ->
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.show(android.view.WindowInsets.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(window, true)
            }
        }
    }
    content()
}
