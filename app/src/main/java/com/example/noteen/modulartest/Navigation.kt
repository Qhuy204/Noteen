package com.example.noteen.modulartest

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.noteen.ui.screen.FoldersScreen
import com.example.noteen.viewmodel.NoteListViewModel

object AppRoutes {
    const val NOTES = "notes"
    const val FOLDERS = "folders"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TestNavigator() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.NOTES
    ) {
        composable(
            route = AppRoutes.NOTES,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(500)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(500)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(500)
                )
            }
        ) {
//            NotesScreen(
//                onNavigateToFolders = {
//                    navController.navigate(AppRoutes.FOLDERS)
//                }
//            )

            val context = LocalContext.current
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val viewModel: NoteListViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application))
            LaunchedEffect(currentRoute) {
                if (currentRoute == "notes") {
                    viewModel.loadData()
                }
            }
            com.example.noteen.ui.screen.NotesScreen(
                onNavigateToNote = {},
                onNavigateToFolders = {
                    navController.navigate(AppRoutes.FOLDERS)
                },
                viewModel
            )
        }
        composable(
            route = AppRoutes.FOLDERS,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(500)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(500)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(500)
                )
            }
        ) {
            FoldersScreen(
                onGoBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

//@Composable
//fun NotesScreen(onNavigateToFolders: () -> Unit) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Cyan),
//        contentAlignment = Alignment.Center
//    ) {
//        Button(onClick = onNavigateToFolders) {
//            Text("Đi tới FoldersScreen")
//        }
//    }
//}
//
//@Composable
//fun FoldersScreen(onGoBack: () -> Unit) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Magenta),
//        contentAlignment = Alignment.Center
//    ) {
//        Button(onClick = onGoBack) {
//            Text("Quay lại NotesScreen")
//        }
//    }
//}