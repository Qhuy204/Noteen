package com.example.noteen.data.model

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.GsonBuilder

data class CanvasData(
    val strokes: List<Stroke> = emptyList(),
)

fun createCanvasJson(strokes: List<Stroke>): String {
    val canvasData = CanvasData(strokes = strokes)

    val gson: Gson = GsonBuilder()
        .serializeNulls()
//        .setPrettyPrinting()
        .create()

    return gson.toJson(canvasData)
}

fun loadCanvasJson(jsonString: String, strokes: SnapshotStateList<Stroke>) {
    val gson = Gson()
    try {
        val data = gson.fromJson(jsonString, CanvasData::class.java)
        strokes.clear()
        data.strokes.forEach {
            it.rebuildPath()
        }
        strokes.addAll(data.strokes)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
