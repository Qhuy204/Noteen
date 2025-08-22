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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp

@Composable
fun AnchoredContextMenu(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 56.dp

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
                .padding(top = topPadding, end = 16.dp)
                .align(Alignment.TopEnd)
                .then(modifier)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color.Black.copy(alpha = 0.6f),
                    spotColor = Color.Black.copy(alpha = 0.8f)
                )
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

@Composable
fun ContextMenuItem(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    contentColor: Color = Color.Black,
    width: Dp = 200.dp,
    isEnabled: Boolean = true
) {
    val displayColor = if (isEnabled) contentColor else Color.LightGray

    TextButton(
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .width(width)
            .heightIn(min = 48.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = displayColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(displayColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                color = displayColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
