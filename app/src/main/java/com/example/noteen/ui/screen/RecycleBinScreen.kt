package com.example.noteen.ui.screen

import android.app.Application
import android.os.Build
import android.util.Log
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.noteen.R
import com.example.noteen.SettingLoader
import com.example.noteen.data.LocalRepository.entity.FolderEntity
import com.example.noteen.data.model.ConfirmAction
import com.example.noteen.ui.component.FolderCard
import com.example.noteen.ui.component.NoteCard
import com.example.noteen.ui.component.SearchBar
import com.example.noteen.ui.component.SortAndLayoutToggle
import com.example.noteen.ui.component.TaskBoard
import com.example.noteen.ui.component.contextmenu.ContextMenuItem
import com.example.noteen.ui.component.contextmenu.FloatingContextMenu
import com.example.noteen.ui.component.dialog.ConfirmDialog
import com.example.noteen.ui.component.dialog.CreateFolderDialog
import com.example.noteen.utils.formatNoteDate
import com.example.noteen.utils.formatTimestamp
import com.example.noteen.viewmodel.BinViewModel
import com.example.noteen.viewmodel.FolderListViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecycleBinScreen(onGoBack: () -> Unit = {}) {
    val context = LocalContext.current

    val viewModel: BinViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application))

    val deletedNotes by viewModel.notes.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf2f2f2))
            .padding(WindowInsets.statusBars.asPaddingValues())
            .zIndex(10f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
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
                        contentDescription = "Back"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recently Deleted",
                        modifier = Modifier,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalItemSpacing = 12.dp,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(deletedNotes, key = { it.id }) { note ->
                    NoteCard(
                        modifier = Modifier.animateItem(),
                        note = note,
                        onClick = { noteId ->

                        }
                    )
                }
                item(span = StaggeredGridItemSpan.FullLine) {
                    Spacer(Modifier.height(500.dp))
                }
            }
        }
    }
}
