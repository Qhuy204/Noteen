package com.example.noteen.ui.component.dialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.commandiron.wheel_picker_compose.WheelDateTimePicker
import com.commandiron.wheel_picker_compose.core.TimeFormat
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import com.example.noteen.R
import com.example.noteen.ui.component.CustomCheckbox
import com.example.noteen.ui.component.CustomIconButton
import com.example.noteen.ui.component.NewDateTimePicker
import com.example.noteen.viewmodel.SubTask
import com.example.noteen.viewmodel.TaskGroup
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.LocalDateTime
import java.util.UUID

// --- Định nghĩa màu sắc và hằng số ---
private val brandBlue = Color(0xFF1966FF)
val screenBg = Color(0xFFf2f2f2)
private val textPrimary = Color(0xFF1F2937)
private val textSecondary = Color(0xFF6B7280)
private val colorRed = Color(0xFFEF4444)


@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddTaskDialog(
    existingTask: TaskGroup?,
    onDismiss: () -> Unit,
    onSaveTask: (TaskGroup) -> Unit,
    tasksCount: Int
) {
    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    val initialSubtasks = existingTask?.subTasks?.toMutableList() ?: mutableStateListOf()
    var subtasks by remember { mutableStateOf<MutableList<SubTask>>(initialSubtasks) }

    var newSubtaskText by remember { mutableStateOf("") }
    var showReminderSection by remember { mutableStateOf(existingTask?.dueDate != null) }
    var selectedDateTime by remember { mutableStateOf(existingTask?.dueDate ?: LocalDateTime.now().plusMinutes(1)) }

    val isSaveEnabled = title.isNotBlank()

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

            BasicTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(screenBg, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
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
                        .padding(12.dp)
                        .focusRequester(newSubtaskFocus),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
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
                        CustomIconButton(onClick = { showReminderSection = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Hủy nhắc nhở", tint = textSecondary)
                        }
                    }
                    NewDateTimePicker(
                        initialDateTime = selectedDateTime,
                        onDateTimeSelected = { selectedDateTime = it }
                    )
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
        CustomCheckbox(checked = subTask.isCompleted, onCheckedChange = onCheckedChange)
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
        CustomIconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = "Xóa", tint = textSecondary)
        }
    }
}
