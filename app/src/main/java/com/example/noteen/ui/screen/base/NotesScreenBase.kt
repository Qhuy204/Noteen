package com.example.noteen.ui.screen.base

import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.example.noteen.R
import com.example.noteen.data.LocalRepository.model.FolderTag
import com.example.noteen.data.model.parseMainTasks
import com.example.noteen.ui.component.CategoryBar
import com.example.noteen.ui.component.CustomFAB
import com.example.noteen.ui.component.NoteCard
import com.example.noteen.ui.component.NoteCard2
import com.example.noteen.ui.component.SearchBar
import com.example.noteen.ui.component.SortAndLayoutToggle
import com.example.noteen.ui.component.TaskBoard
import com.example.noteen.ui.component.TaskCard
import com.example.noteen.ui.component.getSampleNoteEntities
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotesScreenBase() {
    var isGridLayout by remember { mutableStateOf(true) }
    var sortMode by remember { mutableStateOf(0) }
    val notes = remember { getSampleNoteEntities() }
    val folderTags = listOf(
        FolderTag(0,"All", 123),
        FolderTag(0,"Documents", 90),
        FolderTag(0,"Images", 4),
        FolderTag(0,"Videos", 3),
        FolderTag(0,"Others", 26)
    )
    var selectedFolderName by remember { mutableStateOf("All") }

    /// For Collapsing Header
    val header1Dp = 55.dp
    val header2Dp = 50.dp

    val density = LocalDensity.current
    val header1Px = with(density) { header1Dp.toPx() }
    val header2Px = with(density) { header2Dp.toPx() }
    val collapsableHeightPx = header1Px + header2Px

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

    val tasks = parseMainTasks("[]")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf2f2f2))
            .padding(WindowInsets.statusBars.asPaddingValues())
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .nestedScroll(nestedScrollConnection)
        ) {
            val header1Height = with(density) {
                (header1Px + (offsetPx + header2Px)).coerceIn(0f, header1Px).toDp()
            }

            val header2Height = with(density) {
                (header2Px + offsetPx).coerceIn(0f, header2Px).toDp()
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
                    .height(header1Height)
            ) {
                SearchBar()
            }

            CategoryBar(
                chipLists = folderTags,
                selectedChip = selectedFolderName,
                onChipClick = { selectedFolderName = it },
                onAddButtonClick = { },
                onFolderClick = {}
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(header2Height)
            ) {
                SortAndLayoutToggle(
                    selectedSortType = sortMode,
                    isGridLayout = isGridLayout,
                    onSortTypeClick = {
                        sortMode = (sortMode + 1) % 3
                    },
                    onLayoutToggleClick = {
                        isGridLayout = !isGridLayout
                    }
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
                            TaskCard()
                        }
                        items(notes, key = { it.id }) { note ->
                            val currentNoteState by rememberUpdatedState(newValue = note)
                            NoteCard(
                                modifier = Modifier.animateItem(),
                                note = note,
                                onClick = { currentNote ->
                                },
                                onLongPress = {
                                    Log.i("testtt", "Yes")
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
                            TaskBoard(tasks)
                        }
                        items(notes, key = { it.id }) { note ->
                            val currentNoteState by rememberUpdatedState(newValue = note)
                            NoteCard2(
                                modifier = Modifier.animateItem(),
                                note = note,
                                onClick = { currentNote ->
                                },
                                onLongPress = {
                                    Log.i("testtt", "Yes1")
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
            onFirstClick = { },
            onSecondClick = {
            }
        )
    }
}
