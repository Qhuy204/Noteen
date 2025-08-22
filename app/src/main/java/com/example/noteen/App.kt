package com.example.noteen

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.noteen.ui.screen.FoldersScreen
import com.example.noteen.ui.screen.NotesScreen
import com.example.noteen.ui.screen.TasksScreen
import com.example.noteen.ui.screen.base.NotesScreenBase
import com.example.noteen.viewmodel.FolderListViewModel
import com.example.noteen.viewmodel.NoteListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.noteen.ui.screen.TasksScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App() {
    val navController = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = "main_screen",
            modifier = Modifier.fillMaxSize()
        ) {
            composable(route = "main_screen") {
                val pagerState = rememberPagerState(initialPage = 0) { 2 }
                val coroutineScope = rememberCoroutineScope()

                val context = LocalContext.current
                val notesViewModel: NoteListViewModel = viewModel(
                    factory = ViewModelProvider.AndroidViewModelFactory(
                        context.applicationContext as Application
                    )
                )
                val foldersViewModel: FolderListViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application))

                var overlayVisible by remember { mutableStateOf(false) }
                fun navigateToPage(page: Int) {
                    coroutineScope.launch {
                        delay(300)
                        pagerState.animateScrollToPage(
                            page = page,
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = LinearOutSlowInEasing
                            )
                        )
                        delay(100)
                        if (page == 0) {
                            notesViewModel.loadData()
                        }
                        else if (page == 1) {
                            foldersViewModel.loadFolders()
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = !overlayVisible,
                        beyondViewportPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (page == 0) {
                                NotesScreen(
                                    onNavigateToNote = { overlayVisible = it },
                                    onNavigateToFolders = { navigateToPage(1) },
                                    onNavigateToTasks = { navController.navigate("tasks_screen") },
                                    notesViewModel
                                )
                            } else {
                                FoldersScreen(
                                    onGoBack = { navigateToPage(0) },
                                    onNavigateToBin = { overlayVisible = it },
                                    foldersViewModel
                                )
                            }
                        }
                    }
                }
            }
            composable(route = "tasks_screen") {
                TasksScreen(navController)
            }
        }
    }
}
