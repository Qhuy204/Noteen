package com.example.noteen.ui.component

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.noteen.R
import com.example.noteen.data.model.FolderTag
import kotlinx.coroutines.delay

@Composable
fun SortAndLayoutToggle(
    selectedSortType: Int,
    isGridLayout: Boolean,
    onSortTypeClick: () -> Unit,
    onLayoutToggleClick: () -> Unit
) {
    val sortTypeLabels = listOf("Last Modified", "Last Created", "Note title")
//    val sortTypeLabels = listOf("Sửa gần đây", "Tạo gần đây", "Tiêu đề")

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(135.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSortTypeClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.width(3.dp))

            IconButton(
                onClick = onLayoutToggleClick,
                modifier = Modifier
                    .size(36.dp)
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
                    text = "Search",
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

    LaunchedEffect(selectedChip, chipLists) {
        delay(200)
        val index = chipLists.indexOfFirst { it.name == selectedChip }
        if (index >= 0) {
            listState.animateScrollToItem(index, scrollOffset = -64)
        }
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
                items(chipLists) { (label, count) ->
                    TagChip(
                        text = label,
                        count = count,
                        isSelected = label == selectedChip,
                        onClick = { onChipClick(label) }
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
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New",
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

@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    size: Dp = 20.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(5.dp))
            .background(if (checked) Color(0xFF1966FF) else Color.Transparent)
            .border(1.5.dp, if (checked) Color(0xFF1966FF) else Color.Gray, RoundedCornerShape(5.dp))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked",
                tint = Color.White,
                modifier = Modifier.size(size * 0.7f)
            )
        }
    }
}

@SuppressLint("RememberReturnType")
@Composable
fun TaskBoard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(28.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val headerChecked = remember { mutableStateOf(false) }

            val items = listOf(
                "Buy groceries and cook dinner",
                "Finish the Compose layout",
                "Review PRs from the team",
                "Prepare slides for the meeting",
                "Respond to emails from clients"
            )

            val checkedStates = remember { mutableStateListOf(*Array(items.size) { false }) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 3.dp)
            ) {
                Text(
                    text = "Today's Tasks",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                CustomCheckbox(
                    checked = headerChecked.value,
                    onCheckedChange = {
                        headerChecked.value = it
                        // Cập nhật tất cả các checkbox con theo trạng thái của checkbox header
                        for (i in checkedStates.indices) {
                            checkedStates[i] = it
                        }
                    },
                    size = 24.dp // checkbox lớn hơn
                )
            }

            items.forEachIndexed { index, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 2.dp)
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    CustomCheckbox(
                        checked = checkedStates[index],
                        onCheckedChange = { checkedStates[index] = it }
                    )
                }
            }

            val progress = checkedStates.count { it } / items.size.toFloat()
            val percentage = (progress * 100).toInt()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(id = R.drawable.alarm_clock),
                        contentDescription = "Alarm Icon",
//                        tint = Color.Gray
                        tint = Color(0xFFF57C00)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Not set yet",
                        style = MaterialTheme.typography.bodySmall,
//                        color = Color.Gray
                        color = Color(0xFFF57C00)
                    )
                }

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1966FF),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(bottom = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .background(Color(0xFF1966FF), RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}
