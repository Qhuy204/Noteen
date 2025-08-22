package com.example.noteen.ui.component

import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import com.example.noteen.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("RememberReturnType")
@Composable
fun FolderCard(
    modifier: Modifier = Modifier,
    id: Int,
    name: String,
    createdDate: String,
    iconName: String,
    isGridLayout: Boolean = true,
    onCardClick: () -> Unit = {},
    onCardLongPress: () -> Unit = {},
    onMenuButtonClick: (id: Int, offset: DpOffset) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val iconResId = remember(iconName) {
        context.resources.getIdentifier(iconName, "drawable", context.packageName)
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    var iconButtonCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        scope.launch {
                            delay(200)
                            onCardClick()
                        }
                    },
                    onLongPress = { onCardLongPress() }
                )
            }
            .padding(20.dp)
    ) {
        if (isGridLayout) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp),
                shape = CircleShape,
                color = Color.Transparent
            ) {
                if (createdDate != "") IconButton(
                    onClick = {
                        iconButtonCoordinates?.let { coords ->
                            val pxOffset = coords.localToRoot(Offset.Zero)
                            val dpOffset = with(density) { DpOffset(pxOffset.x.toDp(), pxOffset.y.toDp()) }
                            onMenuButtonClick(id, dpOffset)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coordinates -> iconButtonCoordinates = coordinates }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ellipsis_vertical),
                        contentDescription = "More options",
                        tint = Color.Black
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                if (iconResId != 0) {
                    Image(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = createdDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconResId != 0) {
                    Image(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 16.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (createdDate != "") Text(
                        text = createdDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                if (createdDate != "") IconButton(
                    onClick = {
                        iconButtonCoordinates?.let { coords ->
                            val pxOffset = coords.localToRoot(Offset.Zero)
                            val dpOffset = with(density) { DpOffset(pxOffset.x.toDp(), pxOffset.y.toDp()) }
                            onMenuButtonClick(id, dpOffset)
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .onGloballyPositioned { coordinates -> iconButtonCoordinates = coordinates }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ellipsis_vertical),
                        contentDescription = "More options",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

fun Density.toDpOffset(offset: Offset): DpOffset {
    return DpOffset(offset.x.toDp(), offset.y.toDp())
}
