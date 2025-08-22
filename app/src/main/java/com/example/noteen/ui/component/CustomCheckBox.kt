package com.example.noteen.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.noteen.R

@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    shape: Shape = RoundedCornerShape(5.dp),
    checkedBackgroundColor: Color = Color(0xFF1966FF),
    uncheckedBackgroundColor: Color = Color.Transparent,
    checkedBorderColor: Color = Color(0xFF1966FF),
    uncheckedBorderColor: Color = Color(0xFFACACAC)
) {
    val duration = 150
    val density = LocalDensity.current
    val checkPainter = painterResource(R.drawable.check)
    val haptic = LocalHapticFeedback.current

    var isFirstLaunch by remember { mutableStateOf(true) }

    // Scale animation when toggled
    val scale = remember { Animatable(1f) }
    LaunchedEffect(checked) {
        if (!isFirstLaunch) {
            scale.snapTo(0.85f)
            scale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // Progress animation for check icon
    val progress = remember { Animatable(if (checked) 1f else 0f) }
    LaunchedEffect(checked) {
        if (isFirstLaunch) {
            progress.snapTo(if (checked) 1f else 0f)
            isFirstLaunch = false
        } else {
            if (checked) {
                progress.snapTo(0f)
                progress.animateTo(
                    1f,
                    animationSpec = tween(duration, easing = LinearEasing)
                )
            } else {
                progress.snapTo(0f)
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(shape)
            .background(if (checked) checkedBackgroundColor else uncheckedBackgroundColor)
            .border(
                1.8.dp,
                if (checked) checkedBorderColor else uncheckedBorderColor,
                shape
            )
            .clickable {
                onCheckedChange(!checked)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            val iconSize = size * 0.7f
            Icon(
                painter = checkPainter,
                contentDescription = "Checked",
                tint = Color.White.copy(alpha = progress.value),
                modifier = Modifier
                    .size(iconSize)
                    .drawWithContent {
                        val iconWidthPx = with(density) { iconSize.toPx() }
                        val clipWidth = iconWidthPx * progress.value
                        clipRect(right = clipWidth) {
                            this@drawWithContent.drawContent()
                        }
                    }
            )
        }
    }
}
