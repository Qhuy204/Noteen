package com.example.noteen.ui.component.contextmenu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun FloatingContextMenu(
    visible: Boolean,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    dimBackground: Boolean = false,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .let {
                        if (dimBackground) {
                            it.background(Color.Black.copy(alpha = 0.3f))
                        } else it
                    }
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onDismiss()
                    }
            )
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(100, easing = LinearOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(100, easing = FastOutSlowInEasing),
                        transformOrigin = TransformOrigin(1f, 0f)
                    ),
            exit = fadeOut(animationSpec = tween(100, easing = FastOutLinearInEasing)) +
                    scaleOut(
                        targetScale = 0.8f,
                        animationSpec = tween(100, easing = FastOutLinearInEasing),
                        transformOrigin = TransformOrigin(1f, 0f)
                    ),
            modifier = Modifier
                .offset(x = offset.x, y = offset.y)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 4.dp,
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(vertical = 6.dp),
                    content = content
                )
            }
        }
    }
}