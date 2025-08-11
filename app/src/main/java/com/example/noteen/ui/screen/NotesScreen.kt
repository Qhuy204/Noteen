package com.example.noteen.ui.screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteen.R
import com.example.noteen.TextEditorEngine
import com.example.noteen.data.LocalRepository.entity.NoteEntity
import com.example.noteen.ui.component.CategoryBar
import com.example.noteen.ui.component.CustomFAB
import com.example.noteen.ui.component.NoteCard
import com.example.noteen.ui.component.NoteCard2
import com.example.noteen.ui.component.SearchBar
import com.example.noteen.ui.component.SortAndLayoutToggle
import com.example.noteen.ui.component.TaskBoard
import com.example.noteen.ui.component.dialog.CreateFileDialog
import com.example.noteen.ui.component.dialog.CreateFolderDialog
import com.example.noteen.viewmodel.NoteListViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotesScreen(
    onNavigateToNote: (Int) -> Unit = {},
    onNavigateToFolders: () -> Unit = {},
    viewModel: NoteListViewModel
) {
    val context = LocalContext.current

//    var isGridLayout by remember { mutableStateOf(true) }
//    var sortMode by remember { mutableStateOf(0) }
//    val notes = remember { getSampleNoteEntities() }
//    val folderTags = listOf(
//        FolderTag("All", 123),
//        FolderTag("Documents", 90),
//        FolderTag("Images", 4),
//        FolderTag("Videos", 3),
//        FolderTag("Others", 26)
//    )
//    var selectedFolderName by remember { mutableStateOf("All") }


    val isGridLayout by viewModel.isGridLayout.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val folderTags by viewModel.folderTags.collectAsState()
    val selectedFolderName by viewModel.selectedFolderName.collectAsState()
    val notes by viewModel.notes.collectAsState()

    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    var showCreateFileDialog by remember { mutableStateOf(false) }

    val id by viewModel.id.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()
    val showOverlay by viewModel.showOverlay.collectAsState()

    LaunchedEffect(notes) {
        Log.i("Noteee", "Notes: ${notes.map { it.id }}")
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
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Noteen",
                        modifier = Modifier.padding(start = 15.dp),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(id = R.drawable.ellipsis_vertical),
                        contentDescription = "More options"
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

            CategoryBar(
                chipLists = folderTags,
                selectedChip = selectedFolderName,
                onChipClick = { viewModel.selectFolder(it) },
                onAddButtonClick = { showCreateCollectionDialog = true },
                onFolderClick = onNavigateToFolders
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(whiteBox2Height)
            ) {
                SortAndLayoutToggle(
                    selectedSortType = sortMode,
                    isGridLayout = isGridLayout,
                    onSortTypeClick = {
                        viewModel.setSortMode((sortMode + 1) % 3)
                    },
                    onLayoutToggleClick = {
                        viewModel.toggleLayout()
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
            ) { isGrid ->
                if (isGrid) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalItemSpacing = 12.dp,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            TaskBoard()
                        }
                        items(notes, key = { it.id }) { note ->
                            NoteCard(
                                modifier = Modifier.animateItem(),
                                note = note,
                                onClick = { noteId ->
                                    Log.i("Noteee", "ID: $noteId")
//                                    onNavigateToNote(noteId)
                                    viewModel.setId(noteId)
                                }
                            )
                        }
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Spacer(Modifier.height(500.dp))
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        item {
                            TaskBoard()
                        }
                        items(notes, key = { it.id }) { note ->
                            NoteCard2(
                                modifier = Modifier.animateItem(),
                                note = note,
                                onClick = { noteId ->
//                                    onNavigateToNote(noteId)
                                    viewModel.setId(noteId)
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
        CustomFAB(
            onFirstClick = { showCreateFileDialog = true },
            onSecondClick = {
                coroutineScope.launch {
                    val noteId = viewModel.insertNote(NoteEntity(type = "text"))
//                    onNavigateToNote(noteId.toInt())
                    viewModel.setId(noteId.toInt())
                }
            }
        )
        if (showCreateCollectionDialog) {
            CreateFolderDialog(
                onConfirm = { folder ->
                    viewModel.addFolder(folder.name, folder.description)
                    showCreateCollectionDialog = false
                },
                onDismiss = {
                    showCreateCollectionDialog = false
                }
            )
        }
        if (showCreateFileDialog) {
            CreateFileDialog(
                onConfirm = { name, selectedBackgroundColorHex ->
                    showCreateFileDialog = false
                    coroutineScope.launch {
                        val noteId = viewModel.insertNote(NoteEntity(name = name, type = "drawing", color = selectedBackgroundColorHex))
                        viewModel.setId(noteId.toInt())
                    }
                },
                onDismiss = {
                    showCreateFileDialog = false
                }
            )
        }
    }
    if (id != 0) {
        AnimatedVisibility(
            visible = showOverlay,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 400)
            )
        ) {
            selectedNote?.let {
                if (it.type == "text") {
                    OverlyTextEditor(id) {
                        coroutineScope.launch {
                            val (title, json, plain) = TextEditorEngine.waitForContentUpdate()
                            viewModel.saveEditorData(title, json, plain)
                            viewModel.hideOverlay()
                        }
                    }
                }
                else {
                    DrawingScreen(it) { content, thumbnail ->
                        viewModel.saveDrawingNote(content, thumbnail)
                        viewModel.hideOverlay()
                    }
                }
            }
        }
    }
}
