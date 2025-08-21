package com.example.noteen.ui.screen

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.noteen.R
import com.example.noteen.ui.component.dialog.AddTaskDialog
import com.example.noteen.utils.AlarmScheduler
import com.example.noteen.utils.AlarmSchedulerImpl
import com.example.noteen.viewmodel.DueDateStatus
import com.example.noteen.viewmodel.SubTask
import com.example.noteen.viewmodel.TaskGroup
import com.example.noteen.viewmodel.TasksViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.LocalDateTime
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
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    tasksViewModel: TasksViewModel = viewModel()
) {
    BackHandler {
        navController.popBackStack()
    }

    val tasks by tasksViewModel.taskGroups.collectAsState()
    val isEditMode by tasksViewModel.isEditMode.collectAsState()
    val selectedTaskIds by tasksViewModel.selectedTaskIds.collectAsState()

    var showTaskDetailsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskGroup?>(null) }
    val haptics = LocalHapticFeedback.current

    val context = LocalContext.current
    val alarmScheduler: AlarmScheduler = remember { AlarmSchedulerImpl(context) }

    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> tasksViewModel.moveTask(from.index, to.index) },
        onDragEnd = { _, _ -> tasksViewModel.saveTaskOrder() }
    )

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
                                        alarmScheduler.schedule(task)
                                    }
                                },
                                onCardClick = {
                                    taskToEdit = task
                                    showTaskDetailsDialog = true
                                },
                                onSubTaskCheckChange = { subTaskId -> tasksViewModel.toggleSubTaskCheckbox(task.id, subTaskId) }
                            )
                        }
                    }
                }
            }
        }

        // --- Bottom action bar for edit mode ---
        if (isEditMode) {
//            CustomBottomActionBar(
//                modifier = Modifier.align(Alignment.BottomCenter),
//                isDeleteEnabled = selectedTaskIds.isNotEmpty(),
//                onDelete = { showDeleteConfirmDialog = true }
//            )
        }

        // --- FAB to add task ---
        val fabSize = 56.dp
        val paddingEnd = 24.dp
        val paddingBottom = 24.dp
        val xOffset by animateDpAsState(targetValue = if (isEditMode) 0.dp else LocalConfiguration.current.screenWidthDp.dp - fabSize - paddingEnd, label = "xOffset")
        val yOffset by animateDpAsState(targetValue = if (isEditMode) 0.dp else LocalConfiguration.current.screenHeightDp.dp - fabSize - paddingBottom, label = "yOffset")

        AddTaskFab(
            modifier = Modifier
                .absoluteOffset(x = xOffset, y = yOffset)
                .size(fabSize),
            isVisible = !isEditMode,
            onClick = {
                taskToEdit = null
                showTaskDetailsDialog = true
            }
        )

        AnimatedVisibility(
            visible = showTaskDetailsDialog || showDeleteConfirmDialog,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false, onClick = {})
            )
        }

        AnimatedVisibility(
            visible = showTaskDetailsDialog,
            enter = slideInVertically(animationSpec = tween(400, easing = LinearOutSlowInEasing)) { it },
            exit = slideOutVertically(animationSpec = tween(400, easing = LinearOutSlowInEasing)) { it }
        ) {
            AddTaskDialog(
                existingTask = taskToEdit,
                onDismiss = { showTaskDetailsDialog = false },
                onSaveTask = { taskToSave ->
                    tasksViewModel.upsertTask(taskToSave)
                    if (taskToSave.dueDate != null && !taskToSave.isCompleted) {
                        alarmScheduler.schedule(taskToSave)
                    } else {
                        taskToEdit?.let { alarmScheduler.cancel(it) }
                    }
                    showTaskDetailsDialog = false
                },
                tasksCount = tasks.size
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
                        when (status) {
                            DueDateStatus.OVERDUE -> "Đã hết hạn: ${it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                            DueDateStatus.UPCOMING -> "Hết hạn: ${it.format(DateTimeFormatter.ofPattern("HH:mm, E, dd MMM", Locale("vi")))}"
                            DueDateStatus.LONG_TERM -> "Hết hạn: ${it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                            else -> "Not set yet"
                        }
                    } ?: "Not set yet",
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
private fun DueDateInfo(dueDate: LocalDateTime?) {
    val viewModel: TasksViewModel = viewModel()
    val status = viewModel.getDueDateStatus(dueDate)
    if (status != null && dueDate != null) {
        val (iconRes, color, text) = when (status) {
            DueDateStatus.OVERDUE -> Triple(R.drawable.alarm_clock, colorRed, "Đã hết hạn: ${dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
            DueDateStatus.UPCOMING -> Triple(R.drawable.alarm_clock, colorOrange, "Hết hạn: ${dueDate.format(DateTimeFormatter.ofPattern("HH:mm, E, dd MMM", Locale("vi")))}")
            DueDateStatus.LONG_TERM -> Triple(R.drawable.alarm_clock, textSecondary, "Hết hạn: ${dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
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

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AddTaskDialog(
    existingTask: TaskGroup?,
    onDismiss: () -> Unit,
    onSaveTask: (TaskGroup) -> Unit,
    tasksCount: Int
) {
    // State for the main title, now supporting multi-line input
    var title by remember { mutableStateOf(existingTask?.title ?: "") }

    val initialSubtasks = existingTask?.subTasks?.toMutableList() ?: mutableStateListOf()
    var subtasks by remember { mutableStateOf<MutableList<SubTask>>(initialSubtasks) }

    var newSubtaskText by remember { mutableStateOf("") }
    var showReminderSection by remember { mutableStateOf(existingTask?.dueDate != null) }
    var selectedDateTime by remember { mutableStateOf(existingTask?.dueDate ?: LocalDateTime.now().plusMinutes(1)) }

    val isSaveEnabled = title.isNotBlank() || subtasks.isNotEmpty()

    val focusManager = LocalFocusManager.current
    val newSubtaskFocus = remember { FocusRequester() }

    val subtaskReorderState = rememberReorderableLazyListState(onMove = { from, to ->
        subtasks = subtasks.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    })

    val resetState = {
        title = ""
        subtasks = mutableStateListOf()
        newSubtaskText = ""
        showReminderSection = false
        selectedDateTime = LocalDateTime.now().plusMinutes(1)
    }

    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
            if (isGranted) showReminderSection = true
        }
    )

    var showExactAlarmPermissionDialog by remember { mutableStateOf(false) }
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val settingsLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            val finalDueDate = if (showReminderSection) selectedDateTime else null
            val taskGroupId = existingTask?.id ?: UUID.randomUUID()
            val finalSubtasks = subtasks.mapIndexed { index, subTask -> subTask.copy(order = index, taskGroupId = taskGroupId) }
            val allCompleted = finalSubtasks.isNotEmpty() && finalSubtasks.all { it.isCompleted }
            val taskToSave = TaskGroup(
                id = taskGroupId, title = title, subTasks = finalSubtasks, dueDate = finalDueDate,
                isExpanded = existingTask?.isExpanded ?: true, isCompleted = allCompleted, order = existingTask?.order ?: tasksCount
            )
            onSaveTask(taskToSave)
            resetState()
        }
    }

    if (showExactAlarmPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showExactAlarmPermissionDialog = false },
            title = { Text("Yêu cầu quyền", fontWeight = FontWeight.Bold) },
            text = { Text("Để đảm bảo bạn không bỏ lỡ nhiệm vụ, ứng dụng cần quyền \"Báo thức và lời nhắc\" để gửi thông báo đúng giờ.") },
            confirmButton = {
                TextButton(onClick = {
                    showExactAlarmPermissionDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                            settingsLauncher.launch(this)
                        }
                    }
                }) {
                    Text("Đến cài đặt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarmPermissionDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(bottom = 5.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
                .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .pointerInput(Unit) { detectTapGestures(onTap = {}) }
        ) {
            Text(
                if (existingTask == null) "Thêm nhiệm vụ mới" else "Chỉnh sửa nhiệm vụ",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Main task title field with multi-line paste logic
            BasicTextField(
                value = title,
                onValueChange = { newValue ->
                    // Check if the pasted text contains multiple lines
                    val lines = newValue.split("\n")
                    if (lines.size > 1) {
                        // First line becomes the title
                        title = lines.first()
                        // Remaining lines become sub-tasks
                        val newSubtasks = lines.drop(1).filter { it.isNotBlank() }.mapIndexed { index, subtaskTitle ->
                            SubTask(
                                id = UUID.randomUUID(),
                                taskGroupId = existingTask?.id ?: UUID.randomUUID(),
                                title = subtaskTitle.trim(),
                                isCompleted = false,
                                order = subtasks.size + index
                            )
                        }
                        subtasks = (subtasks + newSubtasks).toMutableList()
                    } else {
                        title = newValue
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(screenBg, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) Text("Tên nhiệm vụ...", color = textSecondary, style = MaterialTheme.typography.bodyLarge)
                    innerTextField()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Nhiệm vụ con", style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                state = subtaskReorderState.listState,
                modifier = Modifier
                    .reorderable(subtaskReorderState)
                    .heightIn(max = 200.dp)
            ) {
                itemsIndexed(subtasks, key = { _, item -> item.id }) { index, subtask ->
                    ReorderableItem(reorderableState = subtaskReorderState, key = subtask.id) {
                        val focusRequester = remember { FocusRequester() }
                        SubTaskItem(
                            modifier = Modifier.detectReorderAfterLongPress(subtaskReorderState),
                            subTask = subtask,
                            onCheckedChange = { subtasks = subtasks.toMutableList().apply { set(index, subtask.copy(isCompleted = it)) } },
                            onValueChange = { newText -> subtasks = subtasks.toMutableList().apply { set(index, subtask.copy(title = newText)) } },
                            onRemove = { subtasks = subtasks.toMutableList().apply { removeAt(index) } },
                            focusRequester = focusRequester,
                        )
                    }
                }
            }

            Row(modifier = Modifier.padding(top = 8.dp)) {
                BasicTextField(
                    value = newSubtaskText,
                    onValueChange = { newSubtaskText = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(screenBg, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (newSubtaskText.isNotBlank()) {
                            val newSub = SubTask(id = UUID.randomUUID(), taskGroupId = existingTask?.id ?: UUID.randomUUID(), title = newSubtaskText, isCompleted = false, order = subtasks.size)
                            subtasks = (subtasks + newSub) as MutableList<SubTask>
                            newSubtaskText = ""
                            newSubtaskFocus.requestFocus()
                        } else {
                            focusManager.clearFocus()
                        }
                    }),
                    decorationBox = { innerTextField ->
                        if (newSubtaskText.isEmpty()) Text("Thêm mục...", color = textSecondary, style = MaterialTheme.typography.bodyMedium)
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
                    .clickable {
                        if (newSubtaskText.isNotBlank()) {
                            val newSub = SubTask(id = UUID.randomUUID(), taskGroupId = existingTask?.id ?: UUID.randomUUID(), title = newSubtaskText, isCompleted = false, order = subtasks.size)
                            subtasks = (subtasks + newSub) as MutableList<SubTask>
                            newSubtaskText = ""
                            newSubtaskFocus.requestFocus()
                        }
                    }
                    .padding(12.dp)) {
                    Text("Thêm", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = !showReminderSection) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (!hasNotificationPermission) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    showReminderSection = true
                                }
                            } else {
                                showReminderSection = true
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = "Thêm nhắc nhở", tint = textSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thêm nhắc nhở", color = textSecondary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            AnimatedVisibility(
                visible = showReminderSection,
                enter = expandVertically(animationSpec = tween(400)) + fadeIn(animationSpec = tween(200, delayMillis = 200)),
                exit = shrinkVertically(animationSpec = tween(400)) + fadeOut(animationSpec = tween(200))
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Đặt nhắc nhở", style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                        CustomIconButtonComponent(onClick = { showReminderSection = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Hủy nhắc nhở", tint = textSecondary)
                        }
                    }
//                    CustomDateTimePicker(
//                        initialDateTime = selectedDateTime,
//                        onDateTimeSelected = { selectedDateTime = it }
//                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.LightGray)
                    .clickable {
                        resetState()
                        onDismiss()
                    }, contentAlignment = Alignment.Center) {
                    Text("Hủy", fontWeight = FontWeight.Bold, color = textPrimary)
                }
                val saveButtonColor by animateColorAsState(if (isSaveEnabled) brandBlue else Color.Gray, label = "save_button_color")
                Box(modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(saveButtonColor)
                    .clickable(enabled = isSaveEnabled) {
                        val finalDueDate = if (showReminderSection) selectedDateTime else null
                        val proceedToSave = {
                            val taskGroupId = existingTask?.id ?: UUID.randomUUID()
                            val finalSubtasks = subtasks.mapIndexed { index, subTask -> subTask.copy(order = index, taskGroupId = taskGroupId) }
                            val allCompleted = finalSubtasks.isNotEmpty() && finalSubtasks.all { it.isCompleted }
                            val taskToSave = TaskGroup(
                                id = taskGroupId, title = title, subTasks = finalSubtasks, dueDate = finalDueDate,
                                isExpanded = existingTask?.isExpanded ?: true, isCompleted = allCompleted, order = existingTask?.order ?: tasksCount
                            )
                            onSaveTask(taskToSave)
                            resetState()
                        }

                        if (finalDueDate != null) {
                            if(finalDueDate.isAfter(LocalDateTime.now())){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                    if (alarmManager.canScheduleExactAlarms()) {
                                        proceedToSave()
                                    } else {
                                        showExactAlarmPermissionDialog = true
                                    }
                                } else {
                                    proceedToSave()
                                }
                            } else {
                                proceedToSave()
                            }
                        } else {
                            proceedToSave()
                        }
                    }, contentAlignment = Alignment.Center) {
                    Text("Lưu", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
