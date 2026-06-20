package com.example.trackinggym.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trackinggym.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    viewModel: MainViewModel,
    routineId: Long,
    routineName: String,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long, String) -> Unit,
    onNavigateToAddWorkout: (Long) -> Unit
) {
    val routines by viewModel.routines.collectAsState()
    val routine = routines.find { it.id == routineId }
    
    // Si la rutina no está o no tiene ejercicios, mostramos vacío
    val exerciseIds = routine?.exerciseIds ?: emptyList()
    
    val exercisesFlow = remember(exerciseIds) { viewModel.getExercisesWithLatestLogByIds(exerciseIds) }
    val exercisesWithLogs by exercisesFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(routineName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (exercisesWithLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No hay ejercicios en esta rutina.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(exercisesWithLogs) { item ->
                    FavoriteExerciseItem(
                        item = item,
                        onClick = { onNavigateToDetail(item.exercise.id, item.exercise.name) }
                    )
                }
            }
        }
    }
}
