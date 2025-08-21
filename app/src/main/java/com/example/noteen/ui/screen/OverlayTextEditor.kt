package com.example.noteen.ui.screen

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.example.noteen.R
import com.example.noteen.TextEditorEngine
import com.example.noteen.data.LocalFileManager.FileManager
import com.example.noteen.ui.component.PressEffectIconButton
import com.example.noteen.ui.component.TextToolbar
import com.example.noteen.ui.component.bottomsheet.NoteBackGroundPickerBottomSheet
import kotlinx.coroutines.delay
import org.json.JSONObject
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.noteen.data.LocalRepository.entity.NoteEntity
import com.example.noteen.data.model.ConfirmAction
import com.example.noteen.ui.component.contextmenu.ContextMenuItem
import com.example.noteen.ui.component.contextmenu.AnchoredContextMenu
import com.example.noteen.ui.component.dialog.ConfirmDialog
import com.example.noteen.ui.component.dialog.SelectFolderDialog
import com.example.noteen.viewmodel.NoteDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun keyboardAsState(): State<Boolean> {
    val density = LocalDensity.current
    val imeTop = WindowInsets.ime.getTop(density)
    val imeBottom = WindowInsets.ime.getBottom(density)
    val imeHeight = imeBottom - imeTop

    val prevHeight = remember { mutableStateOf(imeHeight) }
    val isVisible = remember { mutableStateOf(false) }

    LaunchedEffect(imeHeight) {
        if (imeHeight > prevHeight.value) {
            isVisible.value = true
        } else if (imeHeight < prevHeight.value) {
            isVisible.value = false
        }
        prevHeight.value = imeHeight
    }

    return isVisible
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OverlayTextEditor(
    viewModel: NoteDetailViewModel,
    selectedNote: NoteEntity,
    onGoBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    //Work with ViewModel
    LaunchedEffect(Unit) {
        viewModel.loadNote(selectedNote)
        viewModel.loadFolderTags()
    }
    val note by viewModel.selectedNote.collectAsState()
    val folderTags by viewModel.folderTags.collectAsState()
    val isPinned by viewModel.isPinned.collectAsState()
    val isCategorized by viewModel.isCategorized.collectAsState()
    val folderId by viewModel.folder_id.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                scope.launch {
                    viewModel.saveTextNote()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    //

    val blueColor = Color(0xFF1966FF)
    val blackColor = Color.DarkGray

    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardVisible by keyboardAsState()

    val shouldShowToolbar by TextEditorEngine.shouldShowToolbar
    val buttonStatesJson by TextEditorEngine.buttonStatesJson
    val undoStatesJson by TextEditorEngine.undoStatesJson
    val webView = remember { TextEditorEngine.webView }

    val undoStates = remember(undoStatesJson) {
        runCatching {
            val json = JSONObject(undoStatesJson)
            val undo = json.getJSONObject("undo").getBoolean("isEnabled")
            val redo = json.getJSONObject("redo").getBoolean("isEnabled")
            undo to redo
        }.getOrElse { false to false }
    }

    var isReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isReady = true
    }
    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible && isReady) {
            webView.clearFocus()
            scope.launch {
                viewModel.saveTextNote()
            }
        }
    }

    var showSelectFolderDialog by remember { mutableStateOf(false) }

    // For add image handler
    val fileManager = FileManager

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fileManager.saveImage(it)?.let { savedFile ->
                val virtualUrl = "https://myapp.local/external/${savedFile.name}"
                TextEditorEngine.webView.evaluateJavascript(
                    "window.insertImageFromAndroid('$virtualUrl');", null
                )
            }
        }
    }
    //

    var confirmAction by remember { mutableStateOf<ConfirmAction?>(null) }

    ConfirmDialog(
        action = confirmAction,
        onDismiss = { confirmAction = null }
    )

    var showBottomSheet by remember { mutableStateOf(false) }

    val colorHexPairs = listOf(
        "#FFFFFF" to "#000000",
        "#FDF6E3" to "#657B83",
        "#BBDEFB" to "#0D47A1",
        "#C8E6C9" to "#1B5E20",
        "#FFECB3" to "#E65100",
        "#F8BBD0" to "#880E4F",
        "#ECEFF1" to "#37474F"
    )

    val colorPairs = colorHexPairs.map { (bg, icon) ->
        Color(bg.toColorInt()) to Color(icon.toColorInt())
    }

    var selectedColor by remember { mutableStateOf(colorPairs[0].first) }
    val animatedBackgroundColor by animateColorAsState(targetValue = selectedColor)

    NoteBackGroundPickerBottomSheet(
        visible = showBottomSheet,
        colorPairs = colorPairs,
        selectedColor = selectedColor,
            onColorSelected = {
            selectedColor = it
            showBottomSheet = false
        },
        onDismiss = { showBottomSheet = false }
    )

    var showMenuContext by remember { mutableStateOf(false) }

    fun togglePinNote() {
        showMenuContext = false
        viewModel.togglePin()
    }

    fun uncategorizeNote() {
        showMenuContext = false
        viewModel.uncategorize()
    }

    fun showSelectFolderDialog() {
        showMenuContext = false
        showSelectFolderDialog = true
    }

    fun lockNote() {
        showMenuContext = false
        confirmAction = ConfirmAction(
            title = "Lock Note",
            message = "Are you sure you want to lock this note?",
            action = {
                scope.launch {
                    viewModel.toggleLock()
                    onGoBack()
                }
            }
        )
    }

    fun deleteNote() {
        showMenuContext = false
        confirmAction = ConfirmAction(
            title = "Delete Note",
            message = "Are you sure you want to delete this note?",
            action = {
                scope.launch {
                    viewModel.softDeleteNote()
                    onGoBack()
                }
            }
        )
    }

    AnchoredContextMenu(
        visible = showMenuContext,
        onDismiss = { showMenuContext = false }
    ) {
        if (isPinned) ContextMenuItem(stringResource(id = R.string.unpin), R.drawable.pin_off, onClick = { togglePinNote() })
        else ContextMenuItem(stringResource(id = R.string.pin), R.drawable.pin, onClick = { togglePinNote() })
        ContextMenuItem(stringResource(id = R.string.uncategorize), R.drawable.folder_open, onClick = { uncategorizeNote() }, isEnabled = isCategorized)
        ContextMenuItem(stringResource(id = R.string.move_to_new_folder), R.drawable.folder_input, onClick = { showSelectFolderDialog() })
        if (isLocked) ContextMenuItem(stringResource(id = R.string.unlock_note), R.drawable.lock_keyhole, onClick = { lockNote() })
        else ContextMenuItem(stringResource(id = R.string.lock_note), R.drawable.lock_keyhole, onClick = { lockNote() })
        ContextMenuItem(stringResource(id = R.string.delete), R.drawable.trash, onClick = { deleteNote() }, contentColor = Color.Red)
    }

    BackHandler {
        if (!showBottomSheet && !showMenuContext) scope.launch {
            viewModel.saveTextNote()
            onGoBack()
        }
        if (showBottomSheet) showBottomSheet = false
        if (showMenuContext) showMenuContext = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBackgroundColor)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        if (showSelectFolderDialog) {
            SelectFolderDialog(
                folderTags = folderTags,
                selectedFolderId = folderId,
                onConfirm = {
                    viewModel.addToNewFolder(it.id, it.name)
                    showSelectFolderDialog = false
                },
                onDismiss = { showSelectFolderDialog = false }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    PressEffectIconButton(
                        onClick = {
                            keyboardController?.hide()
                            scope.launch {
                                viewModel.saveTextNote()
                                onGoBack()
                            }
                        },
                        icon = painterResource(id = R.drawable.chevron_left),
                        unselectedIconColor = blueColor,
                        iconPadding = 8.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
                if (isKeyboardVisible && shouldShowToolbar) {
                    Row {
                        PressEffectIconButton(
                            onClick = { webView.evaluateJavascript("editor.chain().focus().undo().run()", null) },
                            icon = painterResource(id = R.drawable.undo),
                            unselectedIconColor = blackColor,
                            iconPadding = 13.dp,
                            isEnabled = undoStates.first,
                            modifier = Modifier.size(48.dp)
                        )

                        PressEffectIconButton(
                            onClick = { webView.evaluateJavascript("editor.chain().focus().redo().run()", null) },
                            icon = painterResource(id = R.drawable.redo),
                            unselectedIconColor = blackColor,
                            iconPadding = 13.dp,
                            isEnabled = undoStates.second,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        PressEffectIconButton(
                            onClick = {
                                keyboardController?.hide()
                            },
                            icon = painterResource(id = R.drawable.check_thin),
                            unselectedIconColor = blueColor,
                            iconPadding = 8.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                } else {
                    Row {
                        PressEffectIconButton(
                            onClick = {  },
                            icon = painterResource(id = R.drawable.share),
                            unselectedIconColor = blackColor,
                            iconPadding = 12.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        PressEffectIconButton(
                            onClick = { showBottomSheet = true },
                            icon = painterResource(id = R.drawable.palette),
                            unselectedIconColor = blackColor,
                            iconPadding = 12.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        PressEffectIconButton(
                            onClick = { showMenuContext = true },
                            icon = painterResource(id = R.drawable.settings_2),
                            unselectedIconColor = blackColor,
                            iconPadding = 12.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
            AnimatedVisibility(
                visible = isReady,
                enter = fadeIn(animationSpec = tween(durationMillis = 200))
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            TextEditorEngine.webView.also { webView ->
                                (webView.parent as? ViewGroup)?.removeView(webView)
                            }
                        }

                    )
                    if (isKeyboardVisible && shouldShowToolbar) {
                        TextToolbar(
                            buttonStatesJson, webView,
                            Modifier
                                .align(Alignment.BottomCenter)
                                .zIndex(1f)
                        ) {
                            keyboardController?.hide()
                            Handler(Looper.getMainLooper()).postDelayed({
                                imagePickerLauncher.launch("image/*")
                            }, 200)
                        }
                    }
                }
            }
            if (!isReady) {

            }
        }
    }
}
