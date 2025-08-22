package com.example.noteen.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteen.R
import com.example.noteen.data.model.MainTask

@Composable
fun TaskBoard(
    tasks: List<MainTask>,
    onClick: () -> Unit = {}
) {
    val blueColor = Color(0xFF1966FF)
    val whiteSmoke = Color(0xFFF5F5F5)

    val checkedStates = remember {
        mutableStateListOf(*tasks.map { it.isCompleted }.toTypedArray())
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(230.dp, 290.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        if (tasks.isEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.task_board),
                contentDescription = "Task Board Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(28.dp)
        ) {
            Text(
                text = stringResource(R.string.to_do_list),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (tasks.isEmpty()) FontWeight.ExtraBold else FontWeight.Bold
                ),
                color = if (tasks.isEmpty()) Color.White else Color.Black
            )

            if (tasks.isNotEmpty()) {
                tasks.take(5).forEachIndexed { index, task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = task.title,
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
            }

            if (tasks.isEmpty() || tasks.size <= 5) {
                Row(
                    modifier = Modifier.offset(x = (-4).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = if (tasks.isEmpty()) whiteSmoke else blueColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.add_new_task),
                        color = if (tasks.isEmpty()) whiteSmoke else blueColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.see_more),
                    color = blueColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    onClick: () -> Unit = {}
) {
    val brandBlue = Color(0xFF1966FF)
    val textPrimary = Color(0xFF1F2937)
    val textSecondary = Color(0xFF6B7280)
    val colorOrange = Color(0xFFF97316)
    val screenBg = Color(0xFFf2f2f2)

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(28.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
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
                    color = textPrimary,
                    modifier = Modifier.weight(1f)
                )

                CustomCheckbox(
                    checked = headerChecked.value,
                    onCheckedChange = {
                        headerChecked.value = it
                        for (i in checkedStates.indices) {
                            checkedStates[i] = it
                        }
                    },
                    size = 24.dp
                )
            }

            items.forEachIndexed { index, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 1.dp)
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (checkedStates[index]) textSecondary else textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (checkedStates[index]) TextDecoration.LineThrough else null,
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
            val progress = checkedStates.count { it }.toFloat() / items.size.toFloat()
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
                        tint = colorOrange
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Not set yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorOrange
                    )
                }

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = brandBlue,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(bottom = 4.dp)
                )

                CustomProgressBar(
                    progress = progress,
                    isCompleted = headerChecked.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .align(Alignment.BottomStart)
                )
            }
        }
    }
}


@Composable
fun CustomProgressBar(progress: Float, isCompleted: Boolean, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress", animationSpec = tween(400))
    val color = if (isCompleted) Color.Gray.copy(alpha = 0.5f) else Color(0xFF1966FF)
    Box(modifier = modifier
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(Color(0xFFE0E0E0))) {
        Box(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(animatedProgress)
            .clip(RoundedCornerShape(4.dp))
            .background(color))
    }
}
