package com.example.trackinggym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.trackinggym.theme.TrackingGymTheme
import com.example.trackinggym.ui.MainViewModel
import com.example.trackinggym.ui.screens.ExercisesScreen
import com.example.trackinggym.ui.screens.FavoritesScreen
import com.example.trackinggym.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackingGymTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MainViewModel, 
    onNavigateToAddWorkout: () -> Unit,
    onNavigateToDetail: (Long, String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Favoritos" to Icons.Default.Favorite, "Ejercicios" to Icons.Default.List)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Tracking Gym") },
                actions = {
                    TextButton(onClick = onNavigateToAddWorkout) {
                        Text("Añadir Entreno")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, pair ->
                    NavigationBarItem(
                        icon = { Icon(pair.second, contentDescription = pair.first) },
                        label = { Text(pair.first) },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTabIndex) {
                0 -> FavoritesScreen(viewModel, onNavigateToDetail)
                1 -> ExercisesScreen(viewModel, onNavigateToDetail)
            }
        }
    }
}
