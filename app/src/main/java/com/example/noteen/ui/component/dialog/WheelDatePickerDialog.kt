package com.example.noteen.ui.component.dialog

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.noteen.R
import com.example.noteen.data.LocalRepository.entity.FolderEntity
import com.example.noteen.utils.formatNoteDate
import com.example.noteen.utils.formatTimestamp
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WheelDatePickerDialog(
    currentDateTime: Long?,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    BackHandler {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(bottom = 48.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onDismiss()
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) {}
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(25.dp)
                ) {
//                    NewDateTimePicker(
//                        initialDateTime = LocalDateTime.now()
//                    ) { }
                    NewDateTimePicker(
                        currentDateTime
                    ) {
                        selectedDate = it
                        Log.i("testtt", "Selected Time: ${formatNoteDate(context, formatTimestamp(it))} - Real time now: ${formatNoteDate(context, formatTimestamp(System.currentTimeMillis()))}")
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF0F0F0),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                onConfirm(selectedDate)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1966FF),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF1966FF).copy(alpha = 0.3f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

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
 * Enhanced DateTimePicker - Nhận Long timestamp, trả về Long timestamp
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewDateTimePicker(
    initialTimestamp: Long? = null,
    onDateTimeSelected: (Long) -> Unit
) {
    val zone = ZoneId.systemDefault()

    val nowMillis = remember { System.currentTimeMillis() }
    val initMillis = initialTimestamp ?: nowMillis

    val nowDateTime = remember {
        Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDateTime().withSecond(0).withNano(0)
    }
    val initDateTime = remember {
        Instant.ofEpochMilli(initMillis).atZone(zone).toLocalDateTime().withSecond(0).withNano(0)
    }

    val initialDayOffset = ChronoUnit.DAYS.between(nowDateTime.toLocalDate(), initDateTime.toLocalDate())
    val dateInitialIndex = (INFINITE_ITEMS / 2) + initialDayOffset.toInt()
    val center = INFINITE_ITEMS / 2

    val hourBase = center - center % 24
    val minuteBase = center - center % 60

    val hourInitialIndex = hourBase + initDateTime.hour
    val minuteInitialIndex = minuteBase + initDateTime.minute

    val dateState = rememberLazyListState(initialFirstVisibleItemIndex = dateInitialIndex)
    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = hourInitialIndex)
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = minuteInitialIndex)

    val dateSnapBehavior = rememberSnapFlingBehavior(lazyListState = dateState)
    val hourSnapBehavior = rememberSnapFlingBehavior(lazyListState = hourState)
    val minuteSnapBehavior = rememberSnapFlingBehavior(lazyListState = minuteState)

    var snappedDateIndex by remember { mutableIntStateOf(dateInitialIndex) }
    var snappedHourIndex by remember { mutableIntStateOf(hourInitialIndex) }
    var snappedMinuteIndex by remember { mutableIntStateOf(minuteInitialIndex) }

    var selectedDateTime by remember { mutableStateOf(initDateTime) }

    // --- 4. Theo dõi realtime khi scroll ---
    LaunchedEffect(
        dateState.firstVisibleItemIndex,
        dateState.firstVisibleItemScrollOffset,
        hourState.firstVisibleItemIndex,
        hourState.firstVisibleItemScrollOffset,
        minuteState.firstVisibleItemIndex,
        minuteState.firstVisibleItemScrollOffset
    ) {
        val dayOffset = getSnappedItemIndex(dateState) - (INFINITE_ITEMS / 2)
        val hour = getSnappedItemIndex(hourState) % 24
        val minute = getSnappedItemIndex(minuteState) % 60

        selectedDateTime = nowDateTime.toLocalDate().plusDays(dayOffset.toLong()).atTime(hour, minute)

        // --- Convert về millis để callback ---
        val millis = selectedDateTime.atZone(zone).toInstant().toEpochMilli()
        onDateTimeSelected(millis)
    }

    // --- 5. Callback khi snap xong ---
    val updateDateTime = {
        val dayOffset = snappedDateIndex - (INFINITE_ITEMS / 2)
        val hour = snappedHourIndex % 24
        val minute = snappedMinuteIndex % 60

        val dt = nowDateTime.toLocalDate().plusDays(dayOffset.toLong()).atTime(hour, minute)
        selectedDateTime = dt

        val millis = dt.atZone(zone).toInstant().toEpochMilli()
        onDateTimeSelected(millis)
    }

    // --- 6. UI ---
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        HeaderTimeDisplay(
            selectedDateTime = selectedDateTime,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CONTAINER_HEIGHT.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        // gradient mờ 2 đầu
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.White, Color.Transparent),
                                startY = 0f,
                                endY = size.height * 0.3f
                            )
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.Transparent, Color.White),
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
                    // Date
                    WheelDateColumn(
                        modifier = Modifier.weight(2.5f),
                        listState = dateState,
                        snapBehavior = dateSnapBehavior,
                        today = nowDateTime,
                        onSnapped = { idx ->
                            snappedDateIndex = idx
                            updateDateTime()
                        }
                    )

                    // Hour
                    WheelTimeColumn(
                        modifier = Modifier.weight(1f),
                        listState = hourState,
                        snapBehavior = hourSnapBehavior,
                        maxValue = 24,
                        onSnapped = { idx ->
                            snappedHourIndex = idx
                            updateDateTime()
                        }
                    )

                    // Minute
                    WheelTimeColumn(
                        modifier = Modifier.weight(1f),
                        listState = minuteState,
                        snapBehavior = minuteSnapBehavior,
                        maxValue = 60,
                        onSnapped = { idx ->
                            snappedMinuteIndex = idx
                            updateDateTime()
                        }
                    )
                }
            }
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
