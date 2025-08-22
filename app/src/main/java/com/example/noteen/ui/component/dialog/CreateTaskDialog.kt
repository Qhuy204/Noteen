package com.example.noteen.ui.component.dialog

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteen.R
import com.example.noteen.ui.component.CustomCheckbox
import com.example.noteen.ui.screen.keyboardAsState
import com.example.noteen.utils.formatNoteDate
import com.example.noteen.utils.formatTimestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class NewSubTask(
    val name: String,
    val isCompleted: Boolean = false
)

data class MainTask(
    val id: UUID? = null, // Thêm trường id
    val name: String,
    val isCompleted: Boolean = false,
    val subTasks: List<NewSubTask> = emptyList(),

    val dueDate: Long? = null,
    val repeatMode: Int = 0,
    val reminder: String = ""
)

@Composable
fun InfoRow(
    iconRes: Int,
    iconText: String,
    value: String,
    iconColor: Color,
    editable: Boolean = false,
    defaultString: String = "",
    onValueChange: (String) -> Unit = {},
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !editable,
                onClick = { if (!editable) onClick() },
                interactionSource = remember { MutableInteractionSource() },
                indication = if (!editable) LocalIndication.current else null
            )
            .padding(vertical = 4.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = iconText,
                    fontSize = 14.sp,
                    color = iconColor
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (editable) {
            var textState by remember { mutableStateOf(value) }
            val maxChars = 50
            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (textState.isEmpty()) {
                    Text(
                        text = stringResource(R.string.default_string),
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = textState,
                    onValueChange = { newText ->
                        if (newText.length <= maxChars) {
                            textState = newText
                            onValueChange(newText)
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.End
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    singleLine = true
                )
            }
        } else {
            Text(
                text = value,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 0.dp),
                fontSize = 14.sp,
                color = if (value == defaultString) Color.LightGray else Color.DarkGray,
                maxLines = 1,
                textAlign = TextAlign.End
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateTaskDialog(
    onConfirm: (MainTask) -> Unit,
    onDismiss: () -> Unit,
    defaultTask: MainTask? = null
) {
    val context = LocalContext.current

    var mainTaskName by remember { mutableStateOf(defaultTask?.name ?: "") }
    val subTasks = remember {
        mutableStateListOf<NewSubTask>().apply {
            if (defaultTask != null && defaultTask.subTasks.isNotEmpty()) {
                addAll(defaultTask.subTasks)
            } else {
                add(NewSubTask(""))
            }
        }
    }

    var dueDate by remember { mutableStateOf(defaultTask?.dueDate) }
    var reminder by remember { mutableStateOf(defaultTask?.reminder ?: "") }
    var repeat by remember { mutableStateOf(defaultTask?.repeatMode ?: 0) }

    val repeatOptions = listOf(
        stringResource(R.string.none),
        stringResource(R.string.every_day),
        stringResource(R.string.every_week),
        stringResource(R.string.every_month)
    )

    var isReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isReady = true
    }
    val isKeyboardVisible by keyboardAsState()
//    LaunchedEffect(isKeyboardVisible) {
//        if (!isKeyboardVisible && isReady) {
//            onDismiss()
//        }
//    }

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        WheelDatePickerDialog(
            dueDate,
            onConfirm = {
                dueDate = it
                showDatePicker = false
                focusRequester.requestFocus()
            },
            onDismiss = {
                showDatePicker = false
                focusRequester.requestFocus()
            }
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(20.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(vertical = 16.dp)
                .align(Alignment.BottomCenter)
        ) {
            BasicTextField(
                value = mainTaskName,
                onValueChange = { mainTaskName = it },
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, color = Color.Black),
                decorationBox = { innerTextField ->
                    if (mainTaskName.isEmpty()) {
                        Text(stringResource(R.string.enter_task), color = Color.Gray, fontSize = 18.sp)
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 16.dp)
                    .focusRequester(focusRequester)
            )

            NewSubTaskListEditor(
                subTasks = subTasks,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                InfoRow(
                    iconRes = R.drawable.alarm_clock,
                    iconText = stringResource(R.string.set_due_date),
                    value = if (dueDate == null) stringResource(R.string.not_set) else formatNoteDate(context, formatTimestamp(dueDate!!)),
                    defaultString = stringResource(R.string.not_set),
                    iconColor = Color(0xFFF57C00),
                    onValueChange = {  },
                    onClick = {
                        focusManager.clearFocus()
                        showDatePicker = true
                    }
                )

                InfoRow(
                    iconRes = R.drawable.repeat,
                    iconText = stringResource(R.string.repeat),
                    value = repeatOptions[repeat],
                    defaultString = stringResource(R.string.none),
                    iconColor = Color(0xFF388E3C),
                    onValueChange = { },
                    onClick = { repeat = (repeat + 1) % repeatOptions.size }
                )

                InfoRow(
                    iconRes = R.drawable.megaphone,
                    iconText = stringResource(R.string.reminder),
                    value = reminder,
                    iconColor = Color(0xFF1976D2),
                    editable = true,
                    onValueChange = { reminder = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val noRipple = remember { MutableInteractionSource() }

                Text(
                    text = stringResource(R.string.cancel),
                    color = Color.LightGray,
                    modifier = Modifier
                        .clickable(
                            interactionSource = noRipple,
                            indication = null
                        ) { onDismiss() }
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(R.string.done),
                    color = if (mainTaskName.isNotEmpty()) Color(0xFFFFA000) else Color.LightGray,
                    modifier = Modifier
                        .clickable(
                            enabled = mainTaskName.isNotEmpty(),
                            interactionSource = noRipple,
                            indication = null
                        ) {
                            onConfirm(
                                MainTask(
                                    id = defaultTask?.id, // Thêm trường id
                                    name = mainTaskName,
                                    isCompleted = false,
                                    subTasks = subTasks.filter { it.name.isNotBlank() },
                                    dueDate = dueDate,
                                    repeatMode = repeat,
                                    reminder = reminder
                                )
                            )
                        }
                        .padding(8.dp)
                )
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}


@Composable
fun NewSubTaskListEditor(
    subTasks: SnapshotStateList<NewSubTask>,
    modifier: Modifier = Modifier
) {
    val focusRequesters = remember { mutableStateListOf<FocusRequester>() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(subTasks) {
        if (subTasks.isEmpty()) {
            subTasks.add(NewSubTask(""))
        }
        while (focusRequesters.size < subTasks.size) {
            focusRequesters.add(FocusRequester())
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        subTasks.forEachIndexed { index, subTask ->
            if (focusRequesters.size <= index) {
                focusRequesters.add(FocusRequester())
            }
            val focusRequester = focusRequesters[index]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = subTask.name,
                    onValueChange = { newValue ->
                        subTasks[index] = subTask.copy(name = newValue.take(50))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onPreviewKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace) {
                                if (subTasks[index].name.isEmpty() && subTasks.size > 1 && index > 0) {
                                    coroutineScope.launch {
                                        subTasks.removeAt(index)
                                        focusRequesters.removeAt(index)
                                        focusRequesters[index - 1].requestFocus()
                                    }
                                    true
                                } else false
                            } else false
                        },
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.DarkGray),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (subTasks[index].name.isNotBlank()) {
                                coroutineScope.launch {
                                    subTasks.add(NewSubTask(""))
                                    val newFocusRequester = FocusRequester()
                                    focusRequesters.add(newFocusRequester)
                                    delay(50)
                                    newFocusRequester.requestFocus()
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        if (subTasks[index].name.isEmpty()) {
                            Text(
                                text = stringResource(R.string.enter_subtask),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )

                CustomCheckbox(
                    checked = subTask.isCompleted,
                    onCheckedChange = { checked ->
                        subTasks[index] = subTask.copy(isCompleted = checked)
                    }
                )
            }
        }
    }
}
