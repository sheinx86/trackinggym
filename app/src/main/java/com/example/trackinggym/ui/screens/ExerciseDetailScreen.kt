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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trackinggym.data.entities.ExerciseLog
import com.example.trackinggym.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    viewModel: MainViewModel,
    exerciseId: Long,
    exerciseName: String,
    onNavigateBack: () -> Unit
) {
    val logsFlow = remember(exerciseId) { viewModel.getLogsForExercise(exerciseId) }
    val logs by logsFlow.collectAsState(initial = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exerciseName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay registros para este ejercicio.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Historial de Entrenamientos", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(logs) { log ->
                    LogRecordCard(log)
                }
            }
        }
    }
}

@Composable
fun LogRecordCard(log: ExerciseLog) {
    val formatter = remember { SimpleDateFormat("d 'de' MMMM yyyy", Locale.getDefault()) }
    val dateStr = formatter.format(Date(log.dateMs))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (log.sets.isEmpty()) {
                Text("Sin series registradas", style = MaterialTheme.typography.bodyMedium)
            } else {
                // Table Header
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Serie", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Reps", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Peso", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                
                // Table Rows
                log.sets.forEach { setRecord ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${setRecord.setNumber}", modifier = Modifier.weight(1f))
                        Text("${setRecord.reps}", modifier = Modifier.weight(1f))
                        Text("${setRecord.weight} kg", modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
