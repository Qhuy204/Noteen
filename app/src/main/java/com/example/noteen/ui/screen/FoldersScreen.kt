package com.example.noteen.ui.screen

import android.app.Application
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.noteen.R
import com.example.noteen.data.LocalRepository.entity.FolderEntity
import com.example.noteen.data.model.ConfirmAction
import com.example.noteen.ui.component.FolderCard
import com.example.noteen.ui.component.SearchBar
import com.example.noteen.ui.component.SortAndLayoutToggle
import com.example.noteen.ui.component.contextmenu.ContextMenuItem
import com.example.noteen.ui.component.contextmenu.FloatingContextMenu
import com.example.noteen.ui.component.contextmenu.MenuState
import com.example.noteen.ui.component.dialog.ConfirmDialog
import com.example.noteen.ui.component.dialog.CreateFolderDialog
import com.example.noteen.utils.formatNoteDate
import com.example.noteen.utils.formatTimestamp
import com.example.noteen.viewmodel.FolderViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FoldersScreen(onGoBack: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: FolderViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application))
    val folders by viewModel.folders.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()

    viewModel.loadFolders()

    var isGridLayout by remember { mutableStateOf(true) }

    var menuState by remember { mutableStateOf(MenuState()) }

    var showCreateCollectionDialog by remember { mutableStateOf(false) }


    BackHandler {
        if (menuState.isVisible) menuState = menuState.copy(isVisible = false)
        onGoBack()
    }

    /// For Collapsing Header
    val whiteBox1Dp = 55.dp
    val whiteBox2Dp = 50.dp

    val density = LocalDensity.current
    val whiteBox1Px = with(density) { whiteBox1Dp.toPx() }
    val whiteBox2Px = with(density) { whiteBox2Dp.toPx() }
    val collapsableHeightPx = whiteBox1Px + whiteBox2Px

    val offsetAnimatable = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0) {
                    val newOffset = (offsetAnimatable.value + delta).coerceIn(-collapsableHeightPx, 0f)
                    val consumed = newOffset - offsetAnimatable.value
                    coroutineScope.launch {
                        offsetAnimatable.snapTo(newOffset)
                    }
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta > 0) {
                    val newOffset = (offsetAnimatable.value + delta).coerceIn(-collapsableHeightPx, 0f)
                    val postConsumed = newOffset - offsetAnimatable.value
                    coroutineScope.launch {
                        offsetAnimatable.snapTo(newOffset)
                    }
                    return Offset(0f, postConsumed)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val targetValue = if (offsetAnimatable.value < -collapsableHeightPx / 2) {
                    -collapsableHeightPx
                } else {
                    0f
                }

                coroutineScope.launch {
                    offsetAnimatable.animateTo(
                        targetValue = targetValue,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    )
                }

                return super.onPostFling(consumed, available)
            }
        }
    }

    val offsetPx = offsetAnimatable.value
    ///

    var confirmAction by remember { mutableStateOf<ConfirmAction?>(null) }

    ConfirmDialog(
        action = confirmAction,
        onDismiss = { confirmAction = null }
    )

    fun editFolder() {
        menuState = menuState.copy(isVisible = false)
        menuState.itemId?.let {
            viewModel.selectFolder(it)
            showCreateCollectionDialog = true
        }
    }

    fun addFolder() {
        viewModel.clearSelectedFolder()
        showCreateCollectionDialog = true
    }

    fun deleteFolder() {
        menuState = menuState.copy(isVisible = false)
        confirmAction = ConfirmAction(
            title = "Delete Folder",
            message = "Delete this folder?",
            action = { menuState.itemId?.let { viewModel.deleteFolderById(it) } }
        )
    }

    fun addOrUpdateFolder(folder: FolderEntity) {
        if (folder.id == 0) viewModel.addFolder(folder.name, folder.description)
        else {
            viewModel.updateFolder(folder)
            viewModel.clearSelectedFolder()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf2f2f2))
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .nestedScroll(nestedScrollConnection)
        ) {
            val whiteBox1Height = with(density) {
                (whiteBox1Px + (offsetPx + whiteBox2Px)).coerceIn(0f, whiteBox1Px).toDp()
            }

            val whiteBox2Height = with(density) {
                (whiteBox2Px + offsetPx).coerceIn(0f, whiteBox2Px).toDp()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onGoBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_left),
                        tint = Color.Black,
                        contentDescription = "More options"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Folders",
                        modifier = Modifier,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(whiteBox1Height)
            ) {
                SearchBar()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(whiteBox2Height)
            ) {
                SortAndLayoutToggle(
                    selectedSortType = sortMode,
                    isGridLayout = isGridLayout,
                    onSortTypeClick = {
                        viewModel.currentSortMode = (sortMode + 1) % 3
                    },
                    onLayoutToggleClick = {
                        isGridLayout = !isGridLayout
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Crossfade(
                targetState = isGridLayout,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) { isGrid ->
                if (isGrid) {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(folders, key = { it.id }) { folder ->
                            FolderCard(
                                modifier = Modifier.animateItem(),
                                id = folder.id,
                                name = folder.name,
                                createdDate = formatNoteDate(formatTimestamp(folder.createdAt)),
                                iconName = folder.description,
                                onMenuButtonClick = { id, offset ->
                                    menuState = MenuState(true, offset, id)
                                }
                            )
                        }
                        item {
                            Spacer(Modifier.height(500.dp))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(folders, key = { it.id }) { folder ->
                            FolderCard(
                                modifier = Modifier.animateItem(),
                                id = folder.id,
                                name = folder.name,
                                createdDate = formatNoteDate(formatTimestamp(folder.createdAt)),
                                iconName = folder.description,
                                isGridLayout = false,
                                onMenuButtonClick = { id, offset ->
                                    menuState = MenuState(true, offset, id)
                                }
                            )
                        }
                        item {
                            Spacer(Modifier.height(500.dp))
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { addFolder() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 64.dp, end = 32.dp),
            containerColor = Color(0xFF1966FF),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.folder_plus),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        if (showCreateCollectionDialog) {
            CreateFolderDialog(
                selectedFolder = selectedFolder,
                onConfirm = { folder ->
                    addOrUpdateFolder(folder)
                    showCreateCollectionDialog = false
                },
                onDismiss = {
                    showCreateCollectionDialog = false
                }
            )
        }
        FloatingContextMenu(
            visible = menuState.isVisible,
            offset = menuState.offset,
            onDismiss = { menuState = menuState.copy(isVisible = false) },
        ) {
            ContextMenuItem("Edit", R.drawable.pencil, width = 120.dp, onClick = { editFolder() }, contentColor = Color(0xFF1966FF))
            ContextMenuItem("Delete", R.drawable.trash, width = 120.dp, onClick = { deleteFolder() }, contentColor = Color.Red)
        }
    }
}
