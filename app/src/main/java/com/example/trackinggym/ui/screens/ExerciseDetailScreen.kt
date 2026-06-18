package com.example.trackinggym.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    
    var filterDateMs by remember { mutableStateOf<Long?>(null) }
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    var logToDelete by remember { mutableStateOf<ExerciseLog?>(null) }

    val filteredLogs = remember(logs, filterDateMs) {
        if (filterDateMs == null) {
            logs.sortedByDescending { it.dateMs }
        } else {
            logs.filter { log ->
                val calLog = Calendar.getInstance().apply { timeInMillis = log.dateMs }
                val calFilter = Calendar.getInstance().apply { timeInMillis = filterDateMs!! }
                calLog.get(Calendar.YEAR) == calFilter.get(Calendar.YEAR) &&
                calLog.get(Calendar.DAY_OF_YEAR) == calFilter.get(Calendar.DAY_OF_YEAR)
            }.sortedByDescending { it.dateMs }
        }
    }
    
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = filterDateMs?.let { formatter.format(Date(it)) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filtrar por fecha") },
                    trailingIcon = {
                        Row {
                            if (filterDateMs != null) {
                                IconButton(onClick = { filterDateMs = null }) {
                                    Icon(Icons.Default.Delete, "Limpiar") // Usando Delete como clear provisorio
                                }
                            }
                            IconButton(onClick = {
                                val cal = Calendar.getInstance()
                                filterDateMs?.let { cal.timeInMillis = it }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                        filterDateMs = newCal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Icon(Icons.Default.DateRange, "Seleccionar Fecha")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (filteredLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay registros para este ejercicio.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("Historial de Entrenamientos", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(filteredLogs) { log ->
                        LogRecordCard(
                            log = log,
                            onDelete = { logToDelete = log }
                        )
                    }
                }
            }
        }

        logToDelete?.let { log ->
            AlertDialog(
                onDismissRequest = { logToDelete = null },
                title = { Text("Eliminar registro") },
                text = { Text("¿Estás seguro de que quieres eliminar el registro del ${formatter.format(Date(log.dateMs))}?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteExerciseLog(log)
                        logToDelete = null
                    }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { logToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun LogRecordCard(log: ExerciseLog, onDelete: () -> Unit) {
    val formatter = remember { SimpleDateFormat("d 'de' MMMM yyyy", Locale.getDefault()) }
    val dateStr = formatter.format(Date(log.dateMs))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            if (log.comments.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Comentarios: ${log.comments}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
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
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
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
