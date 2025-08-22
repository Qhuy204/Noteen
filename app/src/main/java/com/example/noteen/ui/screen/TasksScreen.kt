package com.example.noteen.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.noteen.R
import com.example.noteen.ui.component.dialog.CreateTaskDialog
import com.example.noteen.ui.component.dialog.MainTask
import com.example.noteen.ui.component.dialog.NewSubTask
import com.example.noteen.utils.AlarmScheduler
import com.example.noteen.utils.AlarmSchedulerImpl
import com.example.noteen.viewmodel.DueDateStatus
import com.example.noteen.viewmodel.SubTask
import com.example.noteen.viewmodel.TaskGroup
import com.example.noteen.viewmodel.TasksViewModel
import com.example.noteen.viewmodel.convertDialogTaskToViewModelTask
import com.example.noteen.viewmodel.convertViewModelTaskToDialogTask
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import com.example.noteen.ui.component.CustomCheckbox as CustomCheckboxComponent
import com.example.noteen.ui.component.CustomIconButton as CustomIconButtonComponent
import com.example.noteen.ui.component.CustomProgressBar as CustomProgressBarComponent
import com.example.noteen.ui.component.NewDateTimePicker as CustomDateTimePicker

// Colors and constants
val brandBlue = Color(0xFF1966FF)
private val brandGreen = Color(0xFF10B981)
private val brandYellow = Color(0xFFFFC107)
val screenBg = Color(0xFFf2f2f2)
private val cardBg = Color.White
private val textPrimary = Color(0xFF1F2937)
val textSecondary = Color(0xFF6B7280)
private val colorOrange = Color(0xFFF97316)
private val colorRed = Color(0xFFEF4444)

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TasksScreen(
    navController: NavController,
//    sharedTransitionScope: SharedTransitionScope,
//    animatedVisibilityScope: AnimatedVisibilityScope,
    tasksViewModel: TasksViewModel = viewModel()
) {
    BackHandler {
        navController.popBackStack()
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    var showCreateTaskDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                showCreateTaskDialog = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val tasks by tasksViewModel.taskGroups.collectAsState()
    val isEditMode by tasksViewModel.isEditMode.collectAsState()
    val selectedTaskIds by tasksViewModel.selectedTaskIds.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    val context = LocalContext.current
    val alarmScheduler: AlarmScheduler = remember { AlarmSchedulerImpl(context) }

    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> tasksViewModel.moveTask(from.index, to.index) },
        onDragEnd = { _, _ -> tasksViewModel.saveTaskOrder() }
    )

    var currentMainTask by remember {
        mutableStateOf<MainTask?>(null)
    }


    Box(
        Modifier
            .fillMaxSize()
            .background(screenBg)) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(screenBg)) {
            AnimatedContent(targetState = isEditMode, label = "Header Animation") { inEditMode ->
                if (inEditMode) {
                    EditModeHeader(
                        selectedCount = selectedTaskIds.size,
                        onCancel = { tasksViewModel.exitEditMode() },
                        onSelectAll = { tasksViewModel.selectAll() }
                    )
                } else {
                    TaskScreenHeader()
                }
            }

            LazyColumn(
                state = reorderState.listState,
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .reorderable(reorderState)
            ) {
                itemsIndexed(tasks, key = { _, item -> item.id }) { index, task ->
                    ReorderableItem(reorderableState = reorderState, key = task.id) { isDragging ->
                        LaunchedEffect(isDragging) {
                            if (isDragging) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                        val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "scaleAnim")
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevationAnim")

                        // Use a different card composable based on edit mode
                        if (isEditMode) {
                            TaskCardForEditMode(
                                modifier = Modifier
                                    .animateItemPlacement(tween(durationMillis = 300, easing = LinearOutSlowInEasing))
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        shadowElevation = elevation.toPx()
                                    },
                                taskGroup = task,
                                isSelected = task.id in selectedTaskIds,
                                onClick = { tasksViewModel.toggleSelection(task.id) }
                            )
                        } else {
                            TaskCard(
                                modifier = Modifier
                                    .animateItemPlacement(tween(durationMillis = 300, easing = LinearOutSlowInEasing))
                                    .detectReorderAfterLongPress(reorderState)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        shadowElevation = elevation.toPx()
                                    },
                                taskGroup = task,
                                onMasterCheckChange = { isChecked ->
                                    tasksViewModel.toggleMasterCheckbox(task.id, isChecked)
                                    if (isChecked) {
                                        alarmScheduler.cancel(task)
                                    } else {
                                        task.dueDate?.let { alarmScheduler.schedule(task) }
                                    }
                                },
                                onCardClick = {
                                    currentMainTask = convertViewModelTaskToDialogTask(task)
                                    showCreateTaskDialog = true

                                },
                                onSubTaskCheckChange = { subTaskId -> tasksViewModel.toggleSubTaskCheckbox(task.id, subTaskId) }
                            )
                        }
                    }
                }
            }
        }


        FloatingActionButton(
            onClick = {
                currentMainTask = null
                showCreateTaskDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 64.dp, end = 32.dp),
            containerColor = Color(0xFF1966FF),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Toggle",
                tint = Color.White
            )
        }




        if (showCreateTaskDialog) {
            CreateTaskDialog(
                onConfirm = { dialogTask ->
                    // Log the data from the dialog for debugging purposes
                    Log.i("TasksScreen-DEBUG", "onConfirm: dialogTask.id = ${dialogTask.id}")
                    Log.i("TasksScreen-DEBUG", "onConfirm: dialogTask.name = ${dialogTask.name}")
                    Log.i("TasksScreen-DEBUG", "onConfirm: dialogTask.isCompleted = ${dialogTask.isCompleted}")
                    Log.i("TasksScreen-DEBUG", "onConfirm: dialogTask.subTasks = ${dialogTask.subTasks.map { it.name }}")
                    Log.i("TasksScreen-DEBUG", "onConfirm: dialogTask.dueDate = ${dialogTask.dueDate}")
                    Log.i("TasksScreen-DEBUG", "onConfirm: dialogTask.repeatMode = ${dialogTask.repeatMode}")
                    Log.i("TasksScreen-DEBUG", "onConfirm: dialogTask.reminder = ${dialogTask.reminder}")

                    val viewModelTask = convertDialogTaskToViewModelTask(dialogTask, tasks.size)

                    Log.i("TasksScreen-DEBUG", "onConfirm: viewModelTask.id = ${viewModelTask.id}")
                    Log.i("TasksScreen-DEBUG", "onConfirm: viewModelTask.title = ${viewModelTask.title}")
                    Log.i("TasksScreen-DEBUG", "onConfirm: viewModelTask.subTasks.size = ${viewModelTask.subTasks.size}")
                    Log.i("TasksScreen-DEBUG", "onConfirm: viewModelTask.dueDate = ${viewModelTask.dueDate}")
                    Log.i("TasksScreen-DEBUG", "onConfirm: viewModelTask.repeatMode = ${viewModelTask.repeatMode}")
                    Log.i("TasksScreen-DEBUG", "onConfirm: viewModelTask.reminder = ${viewModelTask.reminder}")

                    tasksViewModel.upsertTask(viewModelTask)

                    if (viewModelTask.dueDate != null && !viewModelTask.isCompleted) {
                        alarmScheduler.schedule(viewModelTask)
                    } else {
                        alarmScheduler.cancel(viewModelTask)
                    }

                    showCreateTaskDialog = false
                    currentMainTask = null
                },
                onDismiss = {
                    showCreateTaskDialog = false
                    currentMainTask = null
                },
                defaultTask = currentMainTask
            )
        }

        if (showDeleteConfirmDialog) {
            DeleteConfirmationDialog(
                count = selectedTaskIds.size,
                onConfirm = {
                    val tasksToDelete = tasks.filter { it.id in selectedTaskIds }
                    tasksToDelete.forEach { alarmScheduler.cancel(it) }
                    tasksViewModel.deleteSelected()
                    showDeleteConfirmDialog = false
                },
                onDismiss = { showDeleteConfirmDialog = false }
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) },
        text = { Text("Bạn có chắc chắn muốn xóa $count nhiệm vụ đã chọn?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Xóa", color = colorRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun TaskScreenHeader() {
    val tasksViewModel: TasksViewModel = viewModel()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 40.dp, end = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Nhiệm vụ",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = textPrimary
        )
        Spacer(Modifier.weight(1f))
        CustomIconButtonComponent(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tasksViewModel.enterEditMode()
                }
            }
        ) {
            Icon(Icons.Default.Edit, "Chỉnh sửa", tint = textSecondary)
        }
    }
}

@Composable
private fun EditModeHeader(selectedCount: Int, onCancel: () -> Unit, onSelectAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 40.dp, end = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CustomIconButtonComponent(onClick = onCancel) { Icon(Icons.Default.Close, "Hủy") }
        Text(
            text = if (selectedCount == 0) "Chọn mục" else "Đã chọn $selectedCount mục",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        CustomIconButtonComponent(onClick = onSelectAll) { Icon(Icons.Default.List, "Chọn tất cả") }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TaskCard(
    modifier: Modifier = Modifier,
    taskGroup: TaskGroup,
    onMasterCheckChange: (Boolean) -> Unit,
    onCardClick: () -> Unit,
    onSubTaskCheckChange: (UUID) -> Unit
) {
    val completedCount = taskGroup.subTasks.count { it.isCompleted }
    val totalCount = taskGroup.subTasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val cardAlpha by animateFloatAsState(if (taskGroup.isCompleted) 0.7f else 1f, animationSpec = tween(durationMillis = 300), label = "cardAlpha")

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "press_scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onCardClick() }
                )
            }
            .padding(28.dp)
            .graphicsLayer(alpha = cardAlpha),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with max 3 lines
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 3.dp)
        ) {
            Text(
                text = taskGroup.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = textPrimary),
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            CustomCheckboxComponent(
                checked = taskGroup.isCompleted,
                onCheckedChange = onMasterCheckChange,
                checkedBackgroundColor = brandBlue,
                checkedBorderColor = brandBlue,
                size = 24.dp
            )
        }

        // Subtasks (limited to 4 lines)
        if (taskGroup.subTasks.isNotEmpty()) {
            taskGroup.subTasks.take(4).forEach { subTask ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 1.dp)
                ) {
                    Text(
                        text = subTask.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (subTask.isCompleted) textSecondary else textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else null,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    CustomCheckboxComponent(
                        checked = subTask.isCompleted,
                        onCheckedChange = { onSubTaskCheckChange(subTask.id) }
                    )
                }
            }
        }

        // Progress bar and due date
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
                    tint = colorOrange
                )
                Spacer(modifier = Modifier.width(4.dp))
                val tasksViewModel: TasksViewModel = viewModel()
                Text(
                    text = taskGroup.dueDate?.let {
                        val status = tasksViewModel.getDueDateStatus(it)
                        // Sửa đổi cách hiển thị thời gian
                        val dueDateAsLocalDateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(it),
                            ZoneId.systemDefault()
                        )
                        when (status) {
                            DueDateStatus.OVERDUE -> "Đã hết hạn: ${dueDateAsLocalDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                            DueDateStatus.UPCOMING -> "Hết hạn: ${dueDateAsLocalDateTime.format(DateTimeFormatter.ofPattern("HH:mm, E, dd MMM", Locale("vi")))}"
                            DueDateStatus.LONG_TERM -> "Hết hạn: ${dueDateAsLocalDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                            else -> "Chưa đặt"
                        }
                    } ?: "Chưa đặt",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorOrange
                )
            }
            val percentage = (progress * 100).toInt()
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = brandBlue,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(bottom = 4.dp)
            )
            CustomProgressBarComponent(
                progress = progress,
                isCompleted = taskGroup.isCompleted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
private fun TaskCardForEditMode(
    modifier: Modifier = Modifier,
    taskGroup: TaskGroup,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val completedCount = taskGroup.subTasks.count { it.isCompleted }
    val totalCount = taskGroup.subTasks.size

    val cardBackgroundColor by animateColorAsState(if (isSelected) brandBlue.copy(alpha = 0.1f) else Color.White, label = "card_color_anim")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBackgroundColor)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.List,
                contentDescription = "Reorder",
                tint = textSecondary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = taskGroup.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = textPrimary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (totalCount > 0) {
                    Text(
                        text = "$completedCount/$totalCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }

            CustomCheckboxComponent(
                checked = isSelected,
                onCheckedChange = { onClick() },
                checkedBackgroundColor = brandYellow,
                checkedBorderColor = brandYellow,
                size = 24.dp,
            )
        }
    }
}

@Composable
private fun SubTaskDisplayItem(subTask: SubTask, onCheckedChange: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CustomCheckboxComponent(
            checked = subTask.isCompleted,
            onCheckedChange = { onCheckedChange() },
            size = 18.dp
        )
        Text(
            text = subTask.title,
            modifier = Modifier.weight(1f),
            color = if (subTask.isCompleted) textSecondary else textPrimary,
            textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else null,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DueDateInfo(dueDate: Long?) {
    val viewModel: TasksViewModel = viewModel()
    val status = viewModel.getDueDateStatus(dueDate)
    if (status != null && dueDate != null) {
        val (iconRes, color, text) = when (status) {
            DueDateStatus.OVERDUE -> Triple(R.drawable.alarm_clock, colorRed, "Đã hết hạn: ${LocalDateTime.ofInstant(Instant.ofEpochSecond(dueDate), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
            DueDateStatus.UPCOMING -> Triple(R.drawable.alarm_clock, colorOrange, "Hết hạn: ${LocalDateTime.ofInstant(Instant.ofEpochSecond(dueDate), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm, E, dd MMM", Locale("vi")))}")
            DueDateStatus.LONG_TERM -> Triple(R.drawable.alarm_clock, textSecondary, "Hết hạn: ${LocalDateTime.ofInstant(Instant.ofEpochSecond(dueDate), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Trạng thái",
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = color
            )
        }
    }
}

@Composable
private fun SubTaskItem(
    modifier: Modifier = Modifier,
    subTask: SubTask,
    onCheckedChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CustomCheckboxComponent(checked = subTask.isCompleted, onCheckedChange = onCheckedChange)
        BasicTextField(
            value = subTask.title,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else null,
                color = if (subTask.isCompleted) textSecondary else textPrimary
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        CustomIconButtonComponent(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = "Xóa", tint = textSecondary)
        }
    }
}

@Composable
private fun AddTaskFab(modifier: Modifier = Modifier, isVisible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(visible = isVisible, enter = scaleIn(), exit = scaleOut()) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(brandBlue)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm nhiệm vụ", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}
