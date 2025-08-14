package com.example.noteen

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.noteen.ui.screen.FoldersScreen
import com.example.noteen.ui.screen.NoteEditorScreen
import com.example.noteen.ui.screen.NotesScreen
import com.example.noteen.viewmodel.NoteDetailViewModel
import com.example.noteen.viewmodel.NoteListViewModel

@Composable
fun rememberSafeNavigator(navController: NavHostController): (String) -> Unit {
    val lastNavigateTime = remember { mutableStateOf(0L) }
    val debounceDuration = 500L

    return remember(navController) {
        { route: String ->
            val now = System.currentTimeMillis()
            if (now - lastNavigateTime.value > debounceDuration) {
                lastNavigateTime.value = now
                navController.navigate(route) {
                    launchSingleTop = true
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigator() {
    val context = LocalContext.current

    val navController = rememberNavController()
    val navigateSafe = rememberSafeNavigator(navController)

    val animationDuration = 400
    val customEasing = CubicBezierEasing(0.38f, 0.01f, 0.22f, 1.0f)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavHost(navController = navController, startDestination = "notes") {
        composable("notes") {
            val viewModel: NoteListViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application))
            LaunchedEffect(currentRoute) {
                if (currentRoute == "notes") {
                    viewModel.loadFolderTags()
                    viewModel.loadNotes()
                }
            }
            NotesScreen(
                onNavigateToNote = { noteId ->
                    navigateSafe("editor/$noteId")
                },
                onNavigateToFolders = {
                    navigateSafe("folders")
                },
                viewModel
            )
        }
        composable(
            route = "editor/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(animationDuration, easing = customEasing)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(animationDuration, easing = customEasing)
                )
            }
        ) { backStackEntry ->
//            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
//
//            NoteEditorScreen(
//                noteId = noteId,
//                onGoBack = { navController.popBackStack() },
//                viewModel = viewModel
//            )
        }
        composable(
            route = "folders",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(animationDuration, easing = customEasing)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(animationDuration, easing = customEasing)
                )
            }
        ) {
//            LaunchedEffect(currentRoute) {
//                if (currentRoute == "folders") {
//                    Log.i("TEST", "FoldersScreen need to reload")
//                }
//            }
            FoldersScreen(onGoBack = { navController.popBackStack() })
        }
    }
}
