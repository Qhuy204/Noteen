package com.example.noteen.ui.screen

import android.app.Application
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.noteen.R
import com.example.noteen.TextEditorEngine
import com.example.noteen.data.LocalFileManager.FileManager
import com.example.noteen.ui.component.PressEffectIconButton
import com.example.noteen.ui.component.TextToolbar
import com.example.noteen.viewmodel.NoteDetailViewModel
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteEditorScreen(
    noteId: Int = 0,
    onGoBack: () -> Unit = {},
    viewModel: NoteDetailViewModel
) {
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

    fun saveNote() {
    }

    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible) {
            webView.clearFocus()
            webView.evaluateJavascript("getJson();", null)
        }
    }
    BackHandler {
        saveNote()
        onGoBack()
    }

    // For add image handler
    val fileManager = FileManager

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fileManager.saveImage(it)?.let { savedFile ->
                val virtualUrl = "https://myapp.local/external/${savedFile.name}"
                Log.d("ImageInsert", "Generated virtualUrl: $virtualUrl")
                TextEditorEngine.webView.evaluateJavascript(
                    "window.insertImageFromAndroid('$virtualUrl');", null
                )
            }
        }
    }
    //

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
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
                            saveNote()
                            onGoBack()
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
                            icon = painterResource(id = R.drawable.check),
                            unselectedIconColor = blueColor,
                            iconPadding = 8.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                } else {
                    Row {
                        PressEffectIconButton(
                            onClick = {},
                            icon = painterResource(id = R.drawable.share),
                            unselectedIconColor = blackColor,
                            iconPadding = 12.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        PressEffectIconButton(
                            onClick = {},
                            icon = painterResource(id = R.drawable.palette),
                            unselectedIconColor = blackColor,
                            iconPadding = 12.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        PressEffectIconButton(
                            onClick = {},
                            icon = painterResource(id = R.drawable.settings_2),
                            unselectedIconColor = blackColor,
                            iconPadding = 12.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth()
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
                        Modifier.align(Alignment.BottomCenter).zIndex(1f)
                    ) {
                        keyboardController?.hide()
                        Handler(Looper.getMainLooper()).postDelayed({
                            imagePickerLauncher.launch("image/*")
                        }, 200)
                    }
                }
            }
        }
    }
}
