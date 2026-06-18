package com.example.trackinggym.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trackinggym.data.entities.Exercise
import com.example.trackinggym.data.entities.ExerciseWithLatestLog
import com.example.trackinggym.ui.MainViewModel
import com.example.trackinggym.ui.components.AddLogDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: MainViewModel, onNavigateToDetail: (Long, String) -> Unit) {
    val favorites by viewModel.favoriteExercises.collectAsState()

    Scaffold { paddingValues ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(
                    "No tienes ejercicios favoritos aún.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { item ->
                    FavoriteExerciseItem(
                        item = item,
                        onClick = { onNavigateToDetail(item.exercise.id, item.exercise.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteExerciseItem(
    item: ExerciseWithLatestLog,
    onClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateStr = item.latestLogDateMs?.let { formatter.format(Date(it)) } ?: "Sin registro"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = item.exercise.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Último: $dateStr", style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val setsInfo = item.latestLogSets
                if (setsInfo.isNullOrEmpty()) {
                    Text("Sin series registradas", style = MaterialTheme.typography.bodyMedium)
                } else {
                    val totalSets = setsInfo.size
                    val totalReps = setsInfo.sumOf { it.reps }
                    val maxWeight = setsInfo.maxOfOrNull { it.weight } ?: 0f
                    LogMetric(label = "Series", value = "$totalSets")
                    LogMetric(label = "Reps Totales", value = "$totalReps")
                    LogMetric(label = "Peso Máx", value = "$maxWeight kg")
                }
            }
        }
    }
}

@Composable
fun LogMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
