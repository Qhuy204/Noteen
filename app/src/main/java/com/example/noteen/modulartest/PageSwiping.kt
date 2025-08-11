package com.example.noteen.modulartest

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TestNav() {
    val navController = rememberNavController()
    val navigateSafe = rememberSafeNavigator(navController)

    val animationDuration = 400
    val customEasing = CubicBezierEasing(0.38f, 0.01f, 0.22f, 1.0f)

    NavHost(navController = navController, startDestination = "screen1") {
        composable("screen1") {
            Screen1(
                onNavigateToScreen2 = { value ->
                    navigateSafe("screen2/$value")
                },
                onNavigateToScreen3 = {
                    navigateSafe("screen3")
                }
            )
        }

        composable(
            route = "screen2/{value}",
            arguments = listOf(navArgument("value") { type = NavType.IntType }),
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
            val value = backStackEntry.arguments?.getInt("value") ?: 0
            Screen2(value = value, onGoBack = { navController.popBackStack() })
        }

        composable(
            route = "screen3",
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
            Screen3(onGoBack = { navController.popBackStack() })
        }
    }
}

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

@Composable
fun Screen1(
    onNavigateToScreen2: (Int) -> Unit,
    onNavigateToScreen3: () -> Unit
) {
    var sliderValue by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Screen 1", color = Color.White, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Text("Value: ${sliderValue.toInt()}", color = Color.White)

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = 0f..100f
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { onNavigateToScreen2(sliderValue.toInt()) }) {
            Text("Go to Screen 2 with value")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = onNavigateToScreen3) {
            Text("Go to Screen 3")
        }
    }
}

@Composable
fun Screen2(value: Int, onGoBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Green)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Screen 2", color = Color.White, fontSize = 24.sp)
        Text("Received value: $value", color = Color.White)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onGoBack) {
            Text("Go Back")
        }
    }
}

@Composable
fun Screen3(onGoBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Screen 3", color = Color.White, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onGoBack) {
            Text("Go Back")
        }
    }
}
