package com.example.noteen.ui.component.dialog

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import androidx.core.graphics.toColorInt

@SuppressLint("RememberReturnType")
@Composable
fun CreateFileDialog(
    onConfirm: (fileName: String, selectedBackgroundColorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#FFFFFF") }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val colorOptions = listOf(
        "#FFFFFF", "#F2F2F2", "#FFF9D7", "#E9FFDE", "#D7F4FF", "#F3F3F3",
        "#FFE4E1", "#E0D7FF", "#D1FADF"
    )

    BackHandler { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(2f)
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    focusManager.clearFocus()
                    onDismiss()
                }
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .imePadding()
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
                    Text(
                        text = "Create a new drawing board",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    TextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("File name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Blue.copy(alpha = 0.03f),
                            unfocusedContainerColor = Color.Blue.copy(alpha = 0.03f),
                            disabledContainerColor = Color.Blue.copy(alpha = 0.03f),
                            focusedIndicatorColor = Color(0xFF1966FF),
                            unfocusedIndicatorColor = Color.LightGray,
                            disabledIndicatorColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Blue.copy(alpha = 0.03f), shape = TextFieldDefaults.shape)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Choose background color",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(colorOptions) { colorHex ->
                                val isSelected = colorHex.equals(selectedColorHex, ignoreCase = true)
                                val color = Color(colorHex.toColorInt())

                                Surface(
                                    onClick = { selectedColorHex = colorHex },
                                    shape = CircleShape,
                                    color = if (isSelected) Color.Black.copy(alpha = 0.1f) else Color.Transparent,
                                    border = if (isSelected) BorderStroke(2.dp, Color(0xFF1966FF)) else null,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color = color, shape = CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
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
                                focusManager.clearFocus()
                                onConfirm(fileName, selectedColorHex)
                            },
                            enabled = fileName.isNotBlank(),
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

    LaunchedEffect(Unit) {
        delay(150)
        focusRequester.requestFocus()
    }
}
