package com.example.noteen.ui.component.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.core.graphics.toColorInt

@Composable
fun DrawingBackGroundPickerBottomSheet(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismiss: () -> Unit,
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    selectedPattern: String,
    onPatternSelected: (String) -> Unit
) {
    val colorOptions = listOf(
        "#FFFFFF", "#F2F2F2", "#FFF9D7", "#E9FFDE", "#D7F4FF", "#F3F3F3",
        "#FFE4E1", "#E0D7FF", "#D1FADF"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
    ) {
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
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        clip = false
                    )
                    .background(Color.White, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.Transparent)
                ) {
                    Text(
                        text = "Background color",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Blue.copy(alpha = 0.03f), shape = TextFieldDefaults.shape)
                            .padding(vertical = 16.dp, horizontal = 12.dp)
                    ) {
                        ColorPicker(
                            colorOptions = colorOptions,
                            selectedColorHex = selectedColorHex,
                            onColorSelected = onColorSelected,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Pattern",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Blue.copy(alpha = 0.03f), shape = TextFieldDefaults.shape)
                            .padding(vertical = 16.dp, horizontal = 12.dp)
                    ) {
                        PatternPicker(
                            selectedPattern = selectedPattern,
                            onPatternSelected = onPatternSelected,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PatternPicker(
    patternOptions: List<String> = listOf("None", "Line", "Grid", "Dot"),
    selectedPattern: String,
    onPatternSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(patternOptions) { pattern ->
            val isSelected = pattern == selectedPattern

            Surface(
                onClick = { onPatternSelected(pattern) },
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent, // Bỏ màu nền khi chọn
                border = if (isSelected) BorderStroke(2.dp, Color(0xFF1966FF)) else BorderStroke(1.dp, Color(0xFFE0E0E0)), // Viền xanh khi chọn, viền xám nhạt bình thường
                modifier = Modifier
                    .size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        when (pattern) {
                            "None" -> {
                                // Không vẽ gì bên trong
                            }

                            "Line" -> {
                                val strokeWidth = 2.dp.toPx()
                                val lineYPositions = listOf(size.height / 6, size.height / 2, size.height * 5 / 6)

                                for (y in lineYPositions) {
                                    drawLine(
                                        color = Color.Gray,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                }
                            }

                            "Grid" -> {
                                val strokeWidth = 1.dp.toPx()
                                val stepX = size.width / 3
                                val stepY = size.height / 3

                                // 2 đường dọc
                                drawLine(
                                    color = Color.Gray,
                                    start = Offset(stepX, 0f),
                                    end = Offset(stepX, size.height),
                                    strokeWidth = strokeWidth
                                )
                                drawLine(
                                    color = Color.Gray,
                                    start = Offset(stepX * 2, 0f),
                                    end = Offset(stepX * 2, size.height),
                                    strokeWidth = strokeWidth
                                )

                                // 2 đường ngang
                                drawLine(
                                    color = Color.Gray,
                                    start = Offset(0f, stepY),
                                    end = Offset(size.width, stepY),
                                    strokeWidth = strokeWidth
                                )
                                drawLine(
                                    color = Color.Gray,
                                    start = Offset(0f, stepY * 2),
                                    end = Offset(size.width, stepY * 2),
                                    strokeWidth = strokeWidth
                                )
                            }
                            "Dot" -> {
                                val radius = 2.dp.toPx()
                                val paddingX = size.width / 6f    // Khoảng cách từ viền trái/phải
                                val paddingY = size.height / 6f   // Khoảng cách từ viền trên/dưới
                                val usableWidth = size.width - 2 * paddingX
                                val usableHeight = size.height - 2 * paddingY

                                val stepX = usableWidth / 2
                                val stepY = usableHeight / 2

                                val positionsX = listOf(paddingX, paddingX + stepX, paddingX + stepX * 2)
                                val positionsY = listOf(paddingY, paddingY + stepY, paddingY + stepY * 2)

                                for (x in positionsX) {
                                    for (y in positionsY) {
                                        drawCircle(
                                            color = Color.Gray,
                                            radius = radius,
                                            center = Offset(x, y)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPicker(
    colorOptions: List<String>,
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(colorOptions) { colorHex ->
            val isSelected = colorHex.equals(selectedColorHex, ignoreCase = true)
            val color = Color(colorHex.toColorInt())

            Surface(
                onClick = { onColorSelected(colorHex) },
                shape = CircleShape,
                color = if (isSelected) Color.Black.copy(alpha = 0.1f) else Color.Transparent,
                border = if (isSelected) BorderStroke(2.dp, Color(0xFF1966FF)) else null,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = color, shape = CircleShape)
                )
            }
        }
    }
}
