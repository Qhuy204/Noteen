package com.example.noteen.ui.component

import android.util.Log
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import com.example.noteen.R
import com.example.noteen.data.LocalFileManager.FileManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ToolBarButton(
    val icon: Painter,
    val key: String,
    val jsCommand: String
)

data class ButtonState(
    val isActive: Boolean = false,
    val isEnabled: Boolean = false
)

@Composable
fun TextToolbar(
    jsonString: String,
    webView: WebView,
    modifier: Modifier = Modifier,
    onAddImageButtonClick: () -> Unit = {}
) {
    var showFloating by remember { mutableStateOf(false) }

    val floatBarHeight = 45.dp
    val bottomBarHeight = 45.dp
    val density = LocalDensity.current
    val bottomBarHeightPx = with(density) { bottomBarHeight.toPx().toInt() }

    val gson = remember { Gson() }
    val buttonMap = remember(jsonString) {
        val type = object : TypeToken<Map<String, ButtonState>>() {}.type
        runCatching {
            gson.fromJson<Map<String, ButtonState>>(jsonString, type)
        }.getOrElse { emptyMap() }
    }

    val buttons = listOf(
        ToolBarButton(painterResource(R.drawable.ic_h1), "heading1", "editor.chain().focus().toggleHeading({ level: 1 }).run();"),
        ToolBarButton(painterResource(R.drawable.ic_h2), "heading2", "editor.chain().focus().toggleHeading({ level: 2 }).run();"),
        ToolBarButton(painterResource(R.drawable.ic_h3), "heading3", "editor.chain().focus().toggleHeading({ level: 3 }).run();"),

        ToolBarButton(painterResource(R.drawable.ic_bold), "bold", "editor.chain().focus().toggleBold().run();"),
        ToolBarButton(painterResource(R.drawable.ic_italic), "italic", "editor.chain().focus().toggleItalic().run();"),
        ToolBarButton(painterResource(R.drawable.ic_underline), "underline", "editor.chain().focus().toggleUnderline().run();"),
        ToolBarButton(painterResource(R.drawable.ic_strike), "strikethrough", "editor.chain().focus().toggleStrike().run();"),
        ToolBarButton(painterResource(R.drawable.ic_mark), "highlight", "editor.chain().focus().toggleHighlight().run();"),
        ToolBarButton(painterResource(R.drawable.ic_code), "code", "editor.chain().focus().toggleCode().run();"),

        ToolBarButton(painterResource(R.drawable.ic_superscript), "superscript", "window.toggleSuperscript()"),
        ToolBarButton(painterResource(R.drawable.ic_subscript), "subscript", "window.toggleSubscript()"),

        ToolBarButton(painterResource(R.drawable.ic_bulletlist), "bulletList", "editor.chain().focus().toggleBulletList().run();"),
        ToolBarButton(painterResource(R.drawable.ic_numberlist), "orderedList", "editor.chain().focus().toggleOrderedList().run();"),
        ToolBarButton(painterResource(R.drawable.ic_quote), "blockquote", "editor.chain().focus().toggleBlockquote().run();"),

        ToolBarButton(painterResource(R.drawable.ic_align_left), "alignLeft", "editor.chain().focus().setTextAlign('left').run();"),
        ToolBarButton(painterResource(R.drawable.ic_align_center), "alignCenter", "editor.chain().focus().setTextAlign('center').run();"),
        ToolBarButton(painterResource(R.drawable.ic_align_right), "alignRight", "editor.chain().focus().setTextAlign('right').run();"),
        ToolBarButton(painterResource(R.drawable.ic_align_justify), "alignJustify", "editor.chain().focus().setTextAlign('justify').run();"),
    )
    val dividerIndices = setOf(2, 8, 10, 13)

    val rotation = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bottomBarHeight)
    ) {
        if (showFloating) {
            // Float bar
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, -bottomBarHeightPx) }
                    .height(floatBarHeight)
                    .fillMaxWidth()
                    .zIndex(1f),
                color = Color(0xFFF2F8FD),
                shape = RectangleShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PressEffectIconButton(
                        onClick = { onAddImageButtonClick() },
                        icon = painterResource(R.drawable.ic_image),
                        unselectedIconColor = Color(0xFF1966FF),
                        iconPadding = 6.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    PressEffectIconButton(
                        onClick = { webView.evaluateJavascript("editor.chain().focus().toggleTaskList().run();", null) },
                        icon = painterResource(R.drawable.checkbox2),
                        unselectedIconColor = Color(0xFF1966FF),
                        iconPadding = 7.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    PressEffectIconButton(
                        onClick = { webView.evaluateJavascript("editor.chain().focus().toggleCodeBlock().run();", null) },
                        icon = painterResource(R.drawable.ic_codeblock),
                        unselectedIconColor = Color(0xFF1966FF),
                        iconPadding = 6.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    PressEffectIconButton(
                        onClick = { },
                        icon = painterResource(R.drawable.draw),
                        unselectedIconColor = Color(0xFF1966FF),
                        iconPadding = 8.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    PressEffectIconButton(
                        onClick = { webView.evaluateJavascript("setAudioFromAndroid()", null) },
                        icon = painterResource(R.drawable.sound),
                        unselectedIconColor = Color(0xFF1966FF),
                        iconPadding = 7.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    PressEffectIconButton(
                        onClick = { webView.evaluateJavascript("insertCustomTable()", null) },
                        icon = painterResource(R.drawable.table),
                        unselectedIconColor = Color(0xFF1966FF),
                        iconPadding = 7.dp,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Bottom bar
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            shape = RectangleShape
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated Add/X Button
                Box(
                    modifier = Modifier
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LaunchedEffect(showFloating) {
                        val target = if (showFloating) 180f else 0f
                        rotation.animateTo(
                            targetValue = target,
                            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable {
                                showFloating = !showFloating
                            },
                        color = if (showFloating) Color.Transparent else Color(0xFF1966FF)
                    ) {
                        Icon(
                            imageVector = if (showFloating) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Toggle",
                            tint = if (showFloating) Color.Black else Color.White,
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.Center)
                                .rotate(rotation.value)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(Color(0xFFE0E0E0))
                )

                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    itemsIndexed(buttons) { index, btn ->
                        val state = buttonMap[btn.key]
                        val isActive = state?.isActive == true
                        val isEnabled = state?.isEnabled == true

                        PressEffectIconButton(
                            onClick = { webView.evaluateJavascript(btn.jsCommand, null) },
                            selected = isActive,
                            icon = btn.icon,
                            selectedIconColor = Color(0xFF1966FF),
                            selectedBackgroundColor = Color.Blue.copy(alpha = 0.08f),
                            unselectedIconColor = Color.DarkGray,
                            iconPadding = 8.dp,
                            isEnabled = isEnabled,
                            cornerShape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(36.dp)
                        )

                        if (index in dividerIndices) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp)
                                    .background(Color(0xFFE0E0E0))
                            )
                        }
                    }
                }
            }
        }
    }
}
