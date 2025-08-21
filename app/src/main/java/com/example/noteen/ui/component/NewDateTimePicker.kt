package com.example.noteen.ui.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

// Brand colors
val brandBlue = Color(0xFF2979FF)

// Wheel picker constants
private const val INFINITE_ITEMS = 10000
private const val VISIBLE_ITEMS = 3
private const val ITEM_HEIGHT = 54f
private const val CONTAINER_HEIGHT = 162f // 3 * 54 = 162

/**
 * Tính toán trạng thái visual cho wheel item - real-time color update
 */
private fun calculateWheelItemState(
    itemIndex: Int,
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int
): Triple<Float, Float, Boolean> {
    val itemHeight = ITEM_HEIGHT
    val totalOffset = (itemIndex - firstVisibleItemIndex) * itemHeight - firstVisibleItemScrollOffset
    val centerOffset = (CONTAINER_HEIGHT - itemHeight) / 2
    val distanceFromCenter = totalOffset - centerOffset

    // Kiểm tra xem item này có phải là center item không (trong vùng giữa)
    val isInCenterRegion = abs(distanceFromCenter) < itemHeight / 2

    // Scale: center item = 1.0, others = 0.9
    val scale = if (isInCenterRegion) 1.0f else 0.9f

    // Alpha: center item = 1.0, others = 0.6
    val alpha = if (isInCenterRegion) 1.0f else 0.6f

    return Triple(scale, alpha, isInCenterRegion)
}

/**
 * Tìm item gần center nhất để snap.
 */
private fun getSnappedItemIndex(listState: LazyListState): Int {
    val layoutInfo = listState.layoutInfo
    if (layoutInfo.visibleItemsInfo.isEmpty()) return 0

    val centerY = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2
    return layoutInfo.visibleItemsInfo.minByOrNull { itemInfo ->
        abs((itemInfo.offset + itemInfo.size / 2) - centerY)
    }?.index ?: listState.firstVisibleItemIndex
}

/**
 * Custom Snap Effect cho wheel picker.
 */
@Composable
private fun WheelSnapEffect(
    listState: LazyListState,
    onSnapped: (Int) -> Unit
) {
    LaunchedEffect(listState.isScrollInProgress) {
        snapshotFlow {
            listState.isScrollInProgress
        }.collect { isScrolling ->
            if (!isScrolling) {
                delay(50) // Giảm delay để tăng tốc độ phản hồi
                val snappedIndex = getSnappedItemIndex(listState)
                onSnapped(snappedIndex)
            }
        }
    }
}

/**
 * Enhanced DateTimePicker - No time limit & real-time color update
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewDateTimePicker(
    modifier: Modifier = Modifier,
    initialDateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit
) {
    val today = remember { LocalDateTime.now().withSecond(0).withNano(0) }

    // Tính toán initial positions
    val initialDayOffset = ChronoUnit.DAYS.between(today.toLocalDate(), initialDateTime.toLocalDate())
    val dateInitialIndex = (INFINITE_ITEMS / 2) + initialDayOffset.toInt()
    val hourInitialIndex = (INFINITE_ITEMS / 2) + initialDateTime.hour
    val minuteInitialIndex = (INFINITE_ITEMS / 2) + initialDateTime.minute

    // LazyList states
    val dateState = rememberLazyListState(initialFirstVisibleItemIndex = dateInitialIndex)
    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = hourInitialIndex)
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = minuteInitialIndex)

    // Snap fling behaviors
    val dateSnapBehavior = rememberSnapFlingBehavior(lazyListState = dateState)
    val hourSnapBehavior = rememberSnapFlingBehavior(lazyListState = hourState)
    val minuteSnapBehavior = rememberSnapFlingBehavior(lazyListState = minuteState)

    // Tracked snapped values
    var snappedDateIndex by remember { mutableIntStateOf(dateInitialIndex) }
    var snappedHourIndex by remember { mutableIntStateOf(hourInitialIndex) }
    var snappedMinuteIndex by remember { mutableIntStateOf(minuteInitialIndex) }

    // Current selected datetime
    var selectedDateTime by remember { mutableStateOf(initialDateTime) }

    // Dùng LaunchedEffect để cập nhật realtime
    LaunchedEffect(
        dateState.firstVisibleItemIndex,
        dateState.firstVisibleItemScrollOffset,
        hourState.firstVisibleItemIndex,
        hourState.firstVisibleItemScrollOffset,
        minuteState.firstVisibleItemIndex,
        minuteState.firstVisibleItemScrollOffset
    ) {
        val dayOffset = getSnappedItemIndex(dateState) - (INFINITE_ITEMS / 2)
        val selectedHour = getSnappedItemIndex(hourState) % 24
        val selectedMinute = getSnappedItemIndex(minuteState) % 60
        selectedDateTime = today.toLocalDate().plusDays(dayOffset.toLong()).atTime(selectedHour, selectedMinute)
    }


    // Handler khi có snap mới - Removed time validation
    val updateDateTime = {
        val dayOffset = snappedDateIndex - (INFINITE_ITEMS / 2)
        val selectedHour = snappedHourIndex % 24
        val selectedMinute = snappedMinuteIndex % 60

        val proposedDateTime = today.toLocalDate()
            .plusDays(dayOffset.toLong())
            .atTime(selectedHour, selectedMinute)

        selectedDateTime = proposedDateTime
        onDateTimeSelected(proposedDateTime)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Header hiển thị thời gian được chọn
        HeaderTimeDisplay(
            selectedDateTime = selectedDateTime,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        // Wheel picker container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CONTAINER_HEIGHT.dp)
        ) {
            // Gradient overlay cho hiệu ứng fade
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()

                        // Top gradient
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = size.height * 0.3f
                            )
                        )

                        // Bottom gradient
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White
                                ),
                                startY = size.height * 0.7f,
                                endY = size.height
                            )
                        )
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date Wheel
                    WheelDateColumn(
                        modifier = Modifier.weight(2.5f),
                        listState = dateState,
                        snapBehavior = dateSnapBehavior,
                        today = today,
                        onSnapped = { index ->
                            snappedDateIndex = index
                            updateDateTime()
                        }
                    )

                    // Hour Wheel
                    WheelTimeColumn(
                        modifier = Modifier.weight(1f),
                        listState = hourState,
                        snapBehavior = hourSnapBehavior,
                        maxValue = 24,
                        onSnapped = { index ->
                            snappedHourIndex = index
                            updateDateTime()
                        }
                    )

                    // Minute Wheel
                    WheelTimeColumn(
                        modifier = Modifier.weight(1f),
                        listState = minuteState,
                        snapBehavior = minuteSnapBehavior,
                        maxValue = 60,
                        onSnapped = { index ->
                            snappedMinuteIndex = index
                            updateDateTime()
                        }
                    )
                }
            }

            // Center selection indicators (transparent lines) - Updated to transparent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.Center)
                    .offset(y = (-ITEM_HEIGHT / 2).dp)
                    .background(Color.Transparent)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.Center)
                    .offset(y = (ITEM_HEIGHT / 2).dp)
                    .background(Color.Transparent)
            )
        }
    }
}

/**
 * Header hiển thị thời gian được chọn ở định dạng đầy đủ
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HeaderTimeDisplay(
    selectedDateTime: LocalDateTime,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, dd 'Thg' MM yyyy - HH:mm")
    val formattedDateTime = selectedDateTime.format(formatter)
        .replace("Monday", "Thứ Hai")
        .replace("Tuesday", "Thứ Ba")
        .replace("Wednesday", "Thứ Tư")
        .replace("Thursday", "Thứ Năm")
        .replace("Friday", "Thứ Sáu")
        .replace("Saturday", "Thứ Bảy")
        .replace("Sunday", "Chủ Nhật")

    Text(
        text = formattedDateTime,
        modifier = modifier,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium,
        color = Color.Black.copy(alpha = 0.8f),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
}

/**
 * Wheel Date Column với real-time color update
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WheelDateColumn(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    snapBehavior: androidx.compose.foundation.gestures.FlingBehavior,
    today: LocalDateTime,
    onSnapped: (Int) -> Unit
) {
    WheelSnapEffect(listState, onSnapped)

    val itemHeight = ITEM_HEIGHT.dp
    val containerHeight = CONTAINER_HEIGHT.dp
    val verticalPadding = (containerHeight - itemHeight) / 2
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.fillMaxHeight(),
        state = listState,
        flingBehavior = snapBehavior,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = verticalPadding)
    ) {
        items(count = INFINITE_ITEMS) { index ->
            val dayOffset = index - (INFINITE_ITEMS / 2)
            val date = today.toLocalDate().plusDays(dayOffset.toLong())

            val isInCenterRegion = remember {
                derivedStateOf {
                    val visibleItems = listState.layoutInfo.visibleItemsInfo
                    if (visibleItems.isEmpty()) false
                    else {
                        val centerY = listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportSize.height / 2
                        val centerItem = visibleItems.minByOrNull { abs((it.offset + it.size / 2) - centerY) }
                        centerItem?.index == index
                    }
                }
            }

            val dayOfWeekText = when (date.dayOfWeek.value) {
                1 -> "Th 2"
                2 -> "Th 3"
                3 -> "Th 4"
                4 -> "Th 5"
                5 -> "Th 6"
                6 -> "Th 7"
                7 -> "CN"
                else -> ""
            }

            val dateText = when {
                date.isEqual(today.toLocalDate()) -> "Hôm nay"
                date.isEqual(today.toLocalDate().plusDays(1)) -> "Ngày mai"
                date.isEqual(today.toLocalDate().minusDays(1)) -> "Hôm qua"
                else -> "$dayOfWeekText, ${date.dayOfMonth} Thg ${date.monthValue}"
            }

            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isInCenterRegion.value) FontWeight.Bold else FontWeight.Normal,
                color = if (isInCenterRegion.value) brandBlue else Color.Gray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontSize = if (isInCenterRegion.value) 18.sp else 15.sp,
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

/**
 * Wheel Time Column với real-time color update
 */
@Composable
private fun WheelTimeColumn(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    snapBehavior: androidx.compose.foundation.gestures.FlingBehavior,
    maxValue: Int,
    onSnapped: (Int) -> Unit
) {
    WheelSnapEffect(listState, onSnapped)

    val itemHeight = ITEM_HEIGHT.dp
    val containerHeight = CONTAINER_HEIGHT.dp
    val verticalPadding = (containerHeight - itemHeight) / 2

    LazyColumn(
        modifier = modifier.fillMaxHeight(),
        state = listState,
        flingBehavior = snapBehavior,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = verticalPadding)
    ) {
        items(count = INFINITE_ITEMS) { index ->
            val value = index % maxValue

            val isInCenterRegion = remember {
                derivedStateOf {
                    val visibleItems = listState.layoutInfo.visibleItemsInfo
                    if (visibleItems.isEmpty()) false
                    else {
                        val centerY = listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportSize.height / 2
                        val centerItem = visibleItems.minByOrNull { abs((it.offset + it.size / 2) - centerY) }
                        centerItem?.index == index
                    }
                }
            }

            Text(
                text = String.format("%02d", value),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isInCenterRegion.value) FontWeight.Bold else FontWeight.Normal,
                color = if (isInCenterRegion.value) brandBlue else Color.Gray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontSize = if (isInCenterRegion.value) 20.sp else 16.sp,
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically)
            )
        }
    }
}
