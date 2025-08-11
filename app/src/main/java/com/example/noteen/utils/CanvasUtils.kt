package com.example.noteen.utils

import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.positionChange
import com.example.noteen.data.model.Stroke

suspend fun PointerInputScope.detectMultipleGestures(
    onTap: (Offset) -> Unit = {},
    onDragStart: (initialPosition: Offset) -> Unit = {},
    onDrag: (initialPosition: Offset, changePosition: Offset, dragAmount: Offset) -> Unit = { _, _, _ -> },
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onTransform: (centroid: Offset, zoom: Float, pan: Offset) -> Unit,
    onGesturesEnd: () -> Unit = {}
) {
    awaitPointerEventScope {
        var changeCount = 0
        var isZooming = false
        var initialTouch: Offset? = null
        var hasDragStarted = false

        while (true) {
            val event = awaitPointerEvent()
            val changes = event.changes

            if (changes.any { it.changedToUp() }) {
                if (!isZooming) {
                    if (changeCount == 1) {
                        initialTouch?.let { onTap(it) }
                    } else if (changeCount > 1) {
                        onDragEnd()
                    }
                }
                isZooming = false
                changeCount = 0
                initialTouch = null
                hasDragStarted = false
                onGesturesEnd()
            }

            when (changes.size) {
                1 -> {
                    val dragChange = changes.first()
                    if (dragChange.pressed && !isZooming) {
                        if (initialTouch == null) {
                            initialTouch = dragChange.position
                        }
                        changeCount++
                        if (changeCount > 1) {
                            // Đảm bảo onDragStart chỉ được gọi một lần
                            if (!hasDragStarted) {
                                onDragStart(initialTouch!!)
                                hasDragStarted = true
                            }
                            onDrag(initialTouch!!, dragChange.position, dragChange.positionChange())
                        }
                    }
                    dragChange.consume()
                }
                2 -> {
                    changeCount++
                    isZooming = true
                    onDragCancel()

                    val centroid = event.calculateCentroid()
                    val zoom = event.calculateZoom()
                    val pan = event.calculatePan()

                    if (zoom != 1f || pan != Offset.Zero) {
                        onTransform(centroid, zoom, pan)
                    }

                    changes.forEach { it.consume() }
                }
            }
        }
    }
}

fun DrawScope.drawStroke(stroke: Stroke) {
    drawPath(
        path = stroke.path,
        color = stroke.color,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = stroke.width,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        ),
    )
}
