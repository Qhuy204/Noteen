package com.example.noteen.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

data class Stroke(
    var points: List<Offset>,
    var type: StrokeType = StrokeType.Pen,
    var color: Color = Color.Black,
    var width: Float = 2f,
    var boundingBox: Rect = Rect.Zero,
    var centroid: Offset = Offset.Zero
) {
    @Transient
    var path: Path = buildPath(points)

    fun buildPath(points: List<Offset>): Path {
        return Path().apply {
            if (points.isEmpty()) return@apply
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val current = points[i]
                val midX = (prev.x + current.x) / 2
                val midY = (prev.y + current.y) / 2
                quadraticTo(prev.x, prev.y, midX, midY)
            }
            lineTo(points.last().x, points.last().y)
        }
    }

    fun rebuildPath() {
        path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val current = points[i]
                    val midX = (prev.x + current.x) / 2
                    val midY = (prev.y + current.y) / 2
                    quadraticTo(prev.x, prev.y, midX, midY)
                }
                lineTo(points.last().x, points.last().y)
            }
        }
    }

    enum class StrokeType {
        Pen, Highlighter
    }
}
