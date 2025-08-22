package com.example.noteen.ui.component.dialog

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.zIndex
import com.example.noteen.R
import com.example.noteen.data.LocalRepository.entity.FolderEntity
import kotlinx.coroutines.delay

@SuppressLint("RememberReturnType")
@Composable
fun CreateFolderDialog(
    selectedFolder: FolderEntity? = null,
    onConfirm: (folder: FolderEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var folderName by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFolderIconName by remember { mutableStateOf("folder_1") }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Gán dữ liệu nếu có thư mục đang chọn (edit)
    LaunchedEffect(selectedFolder) {
        selectedFolder?.let {
            folderName = TextFieldValue(
                text = it.name,
                selection = TextRange(it.name.length) // Đặt con trỏ cuối dòng
            )
            selectedFolderIconName = it.description
        }
    }

    // Focus ô input sau khi hiện dialog
    LaunchedEffect(Unit) {
        delay(150)
        focusRequester.requestFocus()
    }

    BackHandler {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(2f)
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
                onDismiss()
            }
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
                        text = if (selectedFolder == null) stringResource(id = R.string.create_new_folder) else stringResource(id = R.string.edit_folder),
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    TextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        label = { Text(stringResource(id = R.string.folder_name)) },
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
                            text = stringResource(id = R.string.choose_folder_icon),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val iconList = listOf(
                            "folder_1" to R.drawable.folder_1,
                            "folder_2" to R.drawable.folder_2,
                            "folder_3" to R.drawable.folder_3,
                            "folder_4" to R.drawable.folder_4,
                            "folder_5" to R.drawable.folder_5,
                            "folder_6" to R.drawable.folder_6,
                            "folder_7" to R.drawable.folder_7
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(iconList) { (name, resId) ->
                                val isSelected = name == selectedFolderIconName

                                Surface(
                                    onClick = { selectedFolderIconName = name },
                                    shape = CircleShape,
                                    color = if (isSelected) Color.Blue.copy(alpha = 0.1f) else Color.Transparent,
                                    tonalElevation = if (isSelected) 2.dp else 0.dp,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = resId),
                                            contentDescription = "Folder Icon $name",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
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
                                if (selectedFolder == null) {
                                    onConfirm(
                                        FolderEntity(
                                            name = folderName.text,
                                            description = selectedFolderIconName
                                        )
                                    )
                                } else {
                                    onConfirm(
                                        selectedFolder.copy(
                                            name = folderName.text,
                                            description = selectedFolderIconName,
                                            updatedAt = System.currentTimeMillis()
                                        )
                                    )
                                }
                            },
                            enabled = folderName.text.isNotBlank(),
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
