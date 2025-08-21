package com.example.noteen.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteen.R
import com.example.noteen.SettingLoader
import com.example.noteen.data.LocalRepository.model.FolderTag
import com.example.noteen.data.model.MainTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SortAndLayoutToggle(
    selectedSortType: Int,
    isGridLayout: Boolean,
    onSortTypeClick: () -> Unit,
    onLayoutToggleClick: () -> Unit,
    noteSort: Boolean = true
) {
    val sortForNote = listOf(stringResource(R.string.note_sort1), stringResource(R.string.note_sort2), stringResource(R.string.note_sort3))
    val sortForFolder = listOf(stringResource(R.string.folder_sort1), stringResource(R.string.folder_sort2), stringResource(R.string.folder_sort3))

    val sortTypeLabels = if (noteSort) sortForNote else sortForFolder

    var heightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    val heightDp = with(density) { heightPx.toDp() }

    val alpha = when {
        heightDp >= 50.dp -> 1f
        heightDp <= 36.dp -> 0f
        else -> ((heightDp - 36.dp) / (50.dp - 36.dp)).coerceIn(0f, 1f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size -> heightPx = size.height }
            .graphicsLayer { this.alpha = alpha },
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSortTypeClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.list_filter),
                        contentDescription = "Sort Icon",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = sortTypeLabels.getOrNull(selectedSortType) ?: "Sort",
                        color = Color.Black,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            IconButton(
                onClick = onLayoutToggleClick,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isGridLayout) R.drawable.layout_grid else R.drawable.layout_list
                    ),
                    contentDescription = "Toggle layout",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SearchBar() {
    val iconSize = 25.dp
    val iconSizePx = with(LocalDensity.current) { iconSize.toPx() }

    var boxHeightPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, shape = RoundedCornerShape(28.dp))
            .padding(horizontal = 28.dp)
            .onGloballyPositioned { layoutCoordinates ->
                boxHeightPx = layoutCoordinates.size.height.toFloat()
            },
        contentAlignment = Alignment.CenterStart
    ) {
        if (boxHeightPx >= iconSizePx) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search Icon",
                    tint = Color.Gray,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.search),
                    color = Color.Gray,
                    fontSize = 18.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun TagChip(
    text: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF1966FF) else Color.White
    val countBackgroundColor = if (isSelected) Color.White else Color(0xFFF2F2F2)
    val contentColor = if (isSelected) Color.White else Color.Black
    val countTextColor = if (isSelected) Color(0xFF1966FF) else Color.Black

    val displayCount = when {
        count > 99 -> "99+"
        else -> count.toString()
    }

    val height = 40.dp
    val textStyle = TextStyle(fontSize = 14.sp, lineHeight = 14.sp)
    val shape = RoundedCornerShape(15.dp)

    Surface(
        onClick = onClick,
        shape = shape,
        color = backgroundColor,
        tonalElevation = 0.dp,
        modifier = Modifier.height(height)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = contentColor,
                style = textStyle
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 24.dp)
                    .height(24.dp)
                    .clip(CircleShape)
                    .background(countBackgroundColor)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayCount,
                    style = textStyle,
                    color = countTextColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun LockedTagChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        color = Color.Black,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.lock_keyhole),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun CategoryBar(
    chipLists: List<FolderTag>,
    selectedChip: String,
    onChipClick: (String) -> Unit,
    onAddButtonClick: () -> Unit,
    onFolderClick: () -> Unit
) {
    val chipHeight = 40.dp
    val blueColor = Color(0xFF1966FF)
    val fadeColor = Color(0xFFF2F2F2)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val allText = stringResource(R.string.all)
    val uncategorizedText = stringResource(R.string.uncategorized)
    val mappedChipLists = chipLists.map { folder ->
        val displayName = when (folder.name) {
            "All" -> allText
            "Uncategorized" -> uncategorizedText
            else -> folder.name
        }
        folder.copy(name = displayName)
    }
    val mappedSelectedChip = when (selectedChip) {
        "All" -> allText
        "Uncategorized" -> uncategorizedText
        else -> selectedChip
    }

    val canScrollBackward by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    val canScrollForward by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != totalItems - 1
        }
    }

    LaunchedEffect(mappedSelectedChip, mappedChipLists) {
        delay(100)
        val index = mappedChipLists.indexOfFirst { it.name == mappedSelectedChip }
        if (index >= 0) {
            listState.animateScrollToItem(index, scrollOffset = -64)
        }
        else listState.animateScrollToItem(0)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(chipHeight)
        ) {
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedChip == "Locked") {
                    item {
                        LockedTagChip(stringResource(R.string.locked_folder)) { }
                    }
                }

                items(mappedChipLists) { (_, label, count) ->
                    TagChip(
                        text = label,
                        count = count,
                        isSelected = label == mappedSelectedChip,
                        onClick = {
                            scope.launch {
                                if (SettingLoader.currentFolder == "Locked") delay(600)
                                if (label == allText) onChipClick("All")
                                else if (label == uncategorizedText) onChipClick("Uncategorized")
                                else onChipClick(label)
                            }
                        }
                    )
                }

                item {
                    Surface(
                        onClick = onAddButtonClick,
                        shape = RoundedCornerShape(15.dp),
                        color = Color.Transparent,
                        modifier = Modifier.height(chipHeight),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(start = 6.dp, end = 12.dp)
                                .fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = blueColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.add_tag),
                                color = blueColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (canScrollBackward) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(16.dp)
                        .align(Alignment.CenterStart)
                        .background(
                            Brush.horizontalGradient(
                                listOf(fadeColor, Color.Transparent)
                            )
                        )
                )
            }

            if (canScrollForward) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(16.dp)
                        .align(Alignment.CenterEnd)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, fadeColor)
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            modifier = Modifier.size(chipHeight),
            shape = RoundedCornerShape(15.dp),
            color = Color.White,
            tonalElevation = 1.dp,
            onClick = onFolderClick
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.folder_minus),
                    contentDescription = "Folder Icon",
                    tint = blueColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
