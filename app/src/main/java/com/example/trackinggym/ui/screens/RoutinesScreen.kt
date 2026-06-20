package com.example.trackinggym.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trackinggym.data.entities.Routine
import com.example.trackinggym.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@Composable
fun RoutinesScreen(
    viewModel: MainViewModel,
    onNavigateToCreateRoutine: () -> Unit,
    onNavigateToRoutineDetail: (Long, String) -> Unit,
    onNavigateToEditRoutine: (Long) -> Unit
) {
    val routines by viewModel.routines.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateRoutine) {
                Icon(Icons.Default.Add, contentDescription = "Crear Rutina")
            }
        }
    ) { innerPadding ->
        if (routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay rutinas creadas. ¡Crea una!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(routines) { routine ->
                    RoutineCard(
                        viewModel = viewModel,
                        routine = routine,
                        onClick = { onNavigateToRoutineDetail(routine.id, routine.name) },
                        onEdit = { onNavigateToEditRoutine(routine.id) },
                        onDelete = { viewModel.deleteRoutine(routine) },
                        onShare = { /* TODO implement share logic */ }
                    )
                }
            }
        }
    }
}

@Composable
fun RoutineCard(
    viewModel: MainViewModel,
    routine: Routine,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${routine.exerciseIds.size} ejercicios",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Borrar Rutina") },
            text = { Text("¿Estás seguro de que deseas borrar la rutina '${routine.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDatePicker) {
        val cal = Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            { _, y, m, d ->
                showDatePicker = false
                coroutineScope.launch {
                    val startCal = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
                    val endCal = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }
                    
                    val logs = viewModel.getLogsForExercisesByDateRange(
                        routine.exerciseIds, 
                        startCal.timeInMillis, 
                        endCal.timeInMillis
                    )
                    
                    val allExercises = viewModel.exercises.value
                    val pairs = logs.mapNotNull { log ->
                        allExercises.find { it.id == log.exerciseId }?.let { it to log }
                    }
                    
                    val data = mapOf("Ejercicios Completados" to pairs)
                    
                    if (pairs.isNotEmpty()) {
                        com.example.trackinggym.utils.ImageExportUtil.shareRoutineImage(
                            context = context,
                            routineName = routine.name,
                            dateMs = startCal.timeInMillis,
                            data = data
                        )
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "No hay entrenamientos para esta fecha.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { showDatePicker = false }
        }.show()
    }
}
