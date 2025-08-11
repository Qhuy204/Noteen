package com.example.noteen.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Spring
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import com.example.noteen.R
import kotlinx.coroutines.delay

@Composable
fun CustomFAB(
    onFirstClick: () -> Unit,
    onSecondClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    val interactionSource = remember { MutableInteractionSource() }

    var showFirstFAB by remember { mutableStateOf(false) }
    var showSecondFAB by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (expanded) {
            showFirstFAB = true
            delay(50)
            showSecondFAB = true
        } else {
            showSecondFAB = false
            delay(50)
            showFirstFAB = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null, // No ripple effect
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        // Collapse FABs
                        expanded = false
                    }
            )
        }
        AnimatedVisibility(
            visible = showSecondFAB,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)),
            exit = scaleOut(animationSpec = tween(durationMillis = 150)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 200.dp, end = 36.dp)
        ) {
            FloatingActionButton(
                onClick = { onFirstClick() },
                modifier = Modifier.size(48.dp),
                containerColor = Color(0xFF1966FF),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.file_pen),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showFirstFAB,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)),
            exit = scaleOut(animationSpec = tween(durationMillis = 150)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 136.dp, end = 36.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    onSecondClick()
                    expanded = false
                },
                modifier = Modifier.size(48.dp),
                containerColor = Color(0xFF1966FF),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.file_text),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        FloatingActionButton(
            onClick = {
                scope.launch {
                    scale.animateTo(0.85f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))
                    scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                expanded = !expanded
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 64.dp, end = 32.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
            interactionSource = interactionSource,
            containerColor = if (expanded) Color.White else Color(0xFF1966FF),
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Toggle",
                tint = if (expanded) Color(0xFF1966FF) else Color.White
            )
        }
    }
}
