package com.example.noteen.ui.component

import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import java.io.File
import com.example.noteen.R
import com.example.noteen.data.LocalRepository.entity.NoteEntity
import com.example.noteen.utils.formatNoteDate
import com.example.noteen.utils.formatTimestamp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: NoteEntity,
    isSelected: Boolean = false,
    editMode: Boolean = false,
    onClick: (NoteEntity) -> Unit = {},
    onLongPress: () -> Unit = {},
    onClickInEditMode: (NoteEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val displayDate = remember(note.createdAt) {
        formatNoteDate(context, formatTimestamp(note.createdAt))
    }

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "cardScale"
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
    ) {
        // Nền + nội dung
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White)
                .pointerInput(editMode) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onLongPress = { onLongPress() },
                        onTap = {
                            if (editMode) {
                                onClickInEditMode(note)
                            } else {
                                onClick(note)
                            }
                        }
                    )
                }
                .padding(12.dp)
        ) {
            Column {
                if (note.thumbnail.isNotBlank()) {
                    val painter = imageBitmap?.let { remember { BitmapPainter(it) } }
                        ?: painterResource(id = R.drawable.default_image)

                    Image(
                        painter = painter,
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
                    Spacer(modifier = Modifier.height(10.dp))
                }

                val noteTitle = if (note.name.isBlank()) "Untitled note" else note.name
                Text(
                    text = noteTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (note.thumbnail.isBlank() && note.type != "drawing") {
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

            if (note.pinnedAt != null) {
                Icon(
                    painter = painterResource(id = R.drawable.pin_filled),
                    contentDescription = "Pinned",
                    tint = Color(0xFF1966FF),
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color.Gray.copy(alpha = 0.08f))
            )
        }

        if (editMode) {
            CustomCheckbox(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 3.dp, y = (-3).dp),
                checked = isSelected,
                shape = CircleShape,
                size = 16.dp,
                uncheckedBorderColor = Color.Transparent,
                uncheckedBackgroundColor = Color(0xFFe5e5e5),
                onCheckedChange = {
                    onClickInEditMode(note)
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteCard2(
    modifier: Modifier = Modifier,
    note: NoteEntity,
    onClick: (NoteEntity) -> Unit,
    onLongPress: () -> Unit
) {
    val context = LocalContext.current
    val displayDate = remember(note.createdAt) {
        formatNoteDate(context, formatTimestamp(note.createdAt))
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onLongPress = { onLongPress() },
                            onTap = { onClick(note) }
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

                        Row(
                            modifier = Modifier.height(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (note.pinnedAt != null) {
                                Icon(
                                    painter = painterResource(id = R.drawable.pin_filled),
                                    contentDescription = "Pinned",
                                    tint = Color(0xFF1966FF),
                                    modifier = Modifier
                                        .size(22.dp)
                                        .padding(start = 6.dp)
                                )
                            }
                        }
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
                    .width(78.dp)
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
