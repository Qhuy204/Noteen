package com.example.noteen.ui.component.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.noteen.R

@Composable
fun NoteBackGroundPickerBottomSheet(
    modifier: Modifier = Modifier,
    visible: Boolean,
    colorPairs: List<Pair<Color, Color>>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
    ) {
        // Vùng bắt click ngoài để đóng BottomSheet
        if (visible) {
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
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ),
            modifier = modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        clip = false
                    )
                    .background(Color.White, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(vertical = 32.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(colorPairs) { (boxColor, iconBgColor) ->
                        val isSelected = boxColor == selectedColor

                        Box(
                            modifier = Modifier
                                .aspectRatio(3f / 4f)
                                .height(100.dp)
                                .border(
                                    width = if (isSelected) 3.dp else 0.1.dp,
                                    color = if (isSelected) Color(0xFF1966FF) else Color(0xFFCCCCCC),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(boxColor)
                                .clickable {
                                    onColorSelected(boxColor)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sample_text),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                colorFilter = ColorFilter.tint(iconBgColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
