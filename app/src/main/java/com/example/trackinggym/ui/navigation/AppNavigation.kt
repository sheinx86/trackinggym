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
import com.example.trackinggym.ui.screens.SplashScreen
import com.example.trackinggym.MainAppScreen

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onNavigateToHome = {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("home") {
            MainAppScreen(
                viewModel = viewModel,
                onNavigateToAddWorkout = { navController.navigate("add_workout") },
                onNavigateToDetail = { exerciseId, exerciseName ->
                    navController.navigate("detail/$exerciseId/$exerciseName")
                },
                onNavigateToCreateRoutine = { navController.navigate("create_routine") },
                onNavigateToRoutineDetail = { routineId, routineName ->
                    navController.navigate("routine_detail/$routineId/$routineName")
                },
                onNavigateToEditRoutine = { routineId ->
                    navController.navigate("edit_routine/$routineId")
                }
            )
        }
        composable("create_routine") {
            com.example.trackinggym.ui.screens.CreateRoutineScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            "edit_routine/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.LongType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
            com.example.trackinggym.ui.screens.CreateRoutineScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                routineIdToEdit = routineId
            )
        }
        composable(
            "routine_detail/{routineId}/{routineName}",
            arguments = listOf(
                navArgument("routineId") { type = NavType.LongType },
                navArgument("routineName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
            val routineName = backStackEntry.arguments?.getString("routineName") ?: ""
            com.example.trackinggym.ui.screens.RoutineDetailScreen(
                viewModel = viewModel,
                routineId = routineId,
                routineName = routineName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { exerciseId, exerciseName ->
                    navController.navigate("detail/$exerciseId/$exerciseName")
                },
                onNavigateToAddWorkout = { exId -> navController.navigate("add_workout?exerciseId=$exId") }
            )
        }
        composable(
            route = "add_workout?exerciseId={exerciseId}&logId={logId}",
            arguments = listOf(
                navArgument("exerciseId") { 
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("logId") { 
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val preselectedExerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: -1L
            val logId = backStackEntry.arguments?.getLong("logId") ?: -1L
            AddWorkoutScreen(
                viewModel = viewModel,
                preselectedExerciseId = if (preselectedExerciseId == -1L) null else preselectedExerciseId,
                logIdToEdit = if (logId == -1L) null else logId,
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddWorkout = { exId -> navController.navigate("add_workout?exerciseId=$exId") },
                onNavigateToEditWorkout = { logId -> navController.navigate("add_workout?logId=$logId") }
            )
        }
    }
}
