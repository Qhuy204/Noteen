package com.example.noteen.ui.component

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.noteen.R
import com.example.noteen.data.LocalRepository.entity.NoteEntity
import com.example.noteen.utils.formatNoteDate
import com.example.noteen.utils.formatTimestamp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val createdDate: String,
    val imageFileName: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: NoteEntity,
    onClick: (Int) -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val context = LocalContext.current
    val displayDate = formatNoteDate(formatTimestamp(note.createdAt))

    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            coroutineScope.launch {
                                scale.animateTo(0.96f, animationSpec = tween(100))
                                scale.animateTo(1f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                            }
                            onClick(note.id)
                        },
                        onLongPress = {
                            coroutineScope.launch {
                                scale.animateTo(0.96f, animationSpec = tween(100))
                                scale.animateTo(1f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                            }
                            onLongPress()
                        }
                    )
                }
                .padding(12.dp)
        ) {
            Column {
                if (note.thumbnail.isNotBlank()) {
                    val imageFile = File(context.getExternalFilesDir("images"), note.thumbnail)
                    val bitmap = imageFile.takeIf { it.exists() }?.let {
                        BitmapFactory.decodeFile(it.absolutePath)?.asImageBitmap()
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Note Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .border(
                                    width = 0.5.dp,
                                    color = Color(0xFFEDEDED),
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .clip(RoundedCornerShape(15.dp))
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.default_image),
                            contentDescription = "Default Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .border(
                                    width = 0.5.dp,
                                    color = Color(0xFFEDEDED),
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .clip(RoundedCornerShape(15.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
                val noteTitle = if (note.name == "") "Untitled note" else note.name

                if (noteTitle.isNotEmpty()) {
                    Text(
                        text = noteTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (note.thumbnail == "" && note.type != "drawing") {
                    val textContent = note.plaintext.ifBlank { "No content" }

                    Spacer(modifier = Modifier.height(8.dp))

                    val contentLength = textContent.length
                    val maxLines = when {
                        contentLength < 50 -> 1
                        contentLength < 99 -> 3
                        else -> 5
                    }

                    Text(
                        text = textContent,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray),
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

fun getSampleNoteEntities(): List<NoteEntity> {
    val now = System.currentTimeMillis()
    return listOf(
        NoteEntity(
            id = 1,
            name = "Short text note",
            type = "text",
            folderId = null,
            content = "Quick idea",
            plaintext = "Quick idea",
            thumbnail = "",
            color = "#FFFFFF",
            background = "",
            createdAt = now - 10 * 60 * 1000, // 10 minutes ago
            updatedAt = now - 10 * 60 * 1000
        ),
        NoteEntity(
            id = 2,
            name = "Yesterday's journal",
            type = "text",
            folderId = null,
            content = "This is a short journal entry I wrote yesterday about how my day went.",
            plaintext = "This is a short journal entry I wrote yesterday about how my day went.",
            thumbnail = "",
            color = "#FFFFFF",
            background = "",
            createdAt = now - 1 * 24 * 60 * 60 * 1000 + 18 * 60 * 60 * 1000 + 30 * 60 * 1000, // yesterday 18:30
            updatedAt = now - 1 * 24 * 60 * 60 * 1000 + 18 * 60 * 60 * 1000 + 30 * 60 * 1000
        ),
        NoteEntity(
            id = 3,
            name = "Weekend Photo",
            type = "drawing",
            folderId = null,
            content = "",
            plaintext = "",
            thumbnail = "weekend_photo.jpg",
            color = "#FFFFFF",
            background = "",
            createdAt = now - 2 * 24 * 60 * 60 * 1000,
            updatedAt = now - 2 * 24 * 60 * 60 * 1000
        ),
        NoteEntity(
            id = 4,
            name = "Long Note",
            type = "text",
            folderId = null,
            content = "This is a very long note intended to test the max lines logic in the UI layout. " +
                    "It should stretch multiple lines and trigger the ellipsis after the fifth line " +
                    "to prevent overflow and ensure that the user interface remains consistent and clean.",
            plaintext = "This is a very long note intended to test the max lines logic in the UI layout. " +
                    "It should stretch multiple lines and trigger the ellipsis after the fifth line " +
                    "to prevent overflow and ensure that the user interface remains consistent and clean.",
            thumbnail = "",
            color = "#FFFFFF",
            background = "",
            createdAt = now - 7 * 24 * 60 * 60 * 1000, // 1 week ago
            updatedAt = now - 7 * 24 * 60 * 60 * 1000
        ),
        NoteEntity(
            id = 5,
            name = "Travel Memory",
            type = "drawing",
            folderId = null,
            content = "",
            plaintext = "",
            thumbnail = "beach_trip.jpg",
            color = "#FFFFFF",
            background = "",
            createdAt = now - 60L * 24 * 60 * 60 * 1000, // approx. 2 months ago
            updatedAt = now - 60L * 24 * 60 * 60 * 1000
        ),
        NoteEntity(
            id = 6,
            name = "Meeting Summary",
            type = "text",
            folderId = null,
            content = "Summary of the Monday meeting discussing quarterly goals, current progress, blockers, and next steps.",
            plaintext = "Summary of the Monday meeting discussing quarterly goals, current progress, blockers, and next steps.",
            thumbnail = "",
            color = "#FFFFFF",
            background = "",
            createdAt = now - 3 * 24 * 60 * 60 * 1000,
            updatedAt = now - 3 * 24 * 60 * 60 * 1000
        ),
        NoteEntity(
            id = 7,
            name = "Old Image Note",
            type = "drawing",
            folderId = null,
            content = "",
            plaintext = "",
            thumbnail = "old_memory.jpg",
            color = "#FFFFFF",
            background = "",
            createdAt = now - 365L * 24 * 60 * 60 * 1000,
            updatedAt = now - 365L * 24 * 60 * 60 * 1000
        )
    )
}

@SuppressLint("RememberReturnType")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteCard2(
    modifier: Modifier = Modifier,
    note: NoteEntity,
    onClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val displayDate = remember(note.createdAt) {
        formatNoteDate(formatTimestamp(note.createdAt))
    }
    val density = LocalDensity.current.density

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "cardScale"
    )

    val rotationY by animateFloatAsState(
        targetValue = if (isPressed) 0f else 15f,
        animationSpec = tween(150),
        label = "rotationY"
    )

    val rotationZ by animateFloatAsState(
        targetValue = if (isPressed) 0f else 8f,
        animationSpec = tween(150),
        label = "rotationZ"
    )

    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 12.dp,
        animationSpec = tween(150),
        label = "offsetY"
    )

    val imageBitmap: ImageBitmap? = remember(note.thumbnail) {
        note.thumbnail.takeIf { it.isNotBlank() }?.let { fileName ->
            val imageFile = File(context.getExternalFilesDir("images"), fileName)
            imageFile.takeIf { it.exists() }?.let {
                BitmapFactory.decodeFile(it.absolutePath)?.asImageBitmap()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clipToBounds()
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        Log.i("TEST", "Clicked")
                        onClick(note.id)
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                val released = tryAwaitRelease()
                                isPressed = false

                                if (released) {
                                    onClick(note.id)
                                }
                            }
                        )
                    }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp)
                    ) {
                        Text(
                            text = if (note.name.isNotBlank()) note.name else "Untitled note",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = displayDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(60.dp))
                }
            }
        }

        if (note.thumbnail.isNotBlank()) {
            val painter = imageBitmap?.let { remember { BitmapPainter(it) } }
                ?: painterResource(id = R.drawable.default_image)

            Image(
                painter = painter,
                contentDescription = "Note Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(72.dp)
                    .aspectRatio(3f / 4f)
                    .align(Alignment.CenterEnd)
                    .offset(x = (-24).dp, y = offsetY)
                    .graphicsLayer {
                        this.rotationZ = rotationZ
                        this.rotationY = rotationY
                        this.transformOrigin = TransformOrigin(0.5f, 1f)
                        this.cameraDistance = 12f * density
                    }
                    .border(
                        width = 0.5.dp,
                        color = Color(0xFFEDEDED),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .zIndex(1f)
            )
        }
    }
}
