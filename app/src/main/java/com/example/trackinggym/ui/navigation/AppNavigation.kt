package com.example.trackinggym.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.trackinggym.ui.MainViewModel
import com.example.trackinggym.ui.screens.AddWorkoutScreen
import com.example.trackinggym.ui.screens.ExerciseDetailScreen
import com.example.trackinggym.MainAppScreen

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            MainAppScreen(
                viewModel = viewModel,
                onNavigateToAddWorkout = { navController.navigate("add_workout") },
                onNavigateToDetail = { exerciseId, exerciseName ->
                    navController.navigate("detail/$exerciseId/$exerciseName")
                }
            )
        }
        composable("add_workout") {
            AddWorkoutScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            "detail/{exerciseId}/{exerciseName}",
            arguments = listOf(
                navArgument("exerciseId") { type = NavType.LongType },
                navArgument("exerciseName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("exerciseId") ?: 0L
            val name = backStackEntry.arguments?.getString("exerciseName") ?: ""
            ExerciseDetailScreen(
                viewModel = viewModel,
                exerciseId = id,
                exerciseName = name,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
