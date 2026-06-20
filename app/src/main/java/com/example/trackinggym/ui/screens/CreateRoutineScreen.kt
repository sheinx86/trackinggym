package com.example.trackinggym.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.trackinggym.data.entities.Exercise
import com.example.trackinggym.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    routineIdToEdit: Long = 0L // 0L means create new
) {
    val context = LocalContext.current
    val isEditing = routineIdToEdit != 0L
    
    val allExercises by viewModel.exercises.collectAsState()
    val routines by viewModel.routines.collectAsState()
    
    var routineName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    val selectedExerciseIds = remember { mutableStateListOf<Long>() }
    
    // Load existing routine if editing
    LaunchedEffect(isEditing, routines) {
        if (isEditing) {
            routines.find { it.id == routineIdToEdit }?.let { existingRoutine ->
                if (routineName.isEmpty()) { // Only set once
                    routineName = existingRoutine.name
                    selectedExerciseIds.clear()
                    selectedExerciseIds.addAll(existingRoutine.exerciseIds)
                }
            }
        }
    }

    val filteredExercises = remember(allExercises, searchQuery) {
        if (searchQuery.isBlank()) allExercises
        else allExercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Rutina" else "Crear Rutina") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (routineName.isBlank()) {
                            Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }
                        if (selectedExerciseIds.isEmpty()) {
                            Toast.makeText(context, "Selecciona al menos 1 ejercicio", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }
                        
                        // We reuse createRoutine for edit as well since insertRoutine has REPLACE strategy, 
                        // but we need to ensure we pass the correct ID if editing.
                        // Actually, createRoutine in viewModel hardcodes id=0. 
                        // Let's call DAO directly or add updateRoutine in viewModel.
                        // For simplicity, let's use the viewmodel's create routine if new, or a new update routine method.
                        // Wait, I didn't add updateRoutine to ViewModel. Let's add it.
                        // I will update ViewModel to handle id in createRoutine, or just use insert routine.
                        if (isEditing) {
                            viewModel.createRoutineWithId(routineIdToEdit, routineName, selectedExerciseIds.toList())
                        } else {
                            viewModel.createRoutine(routineName, selectedExerciseIds.toList())
                        }
                        Toast.makeText(context, "Rutina guardada", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = routineName,
                onValueChange = { routineName = it },
                label = { Text("Nombre de la rutina") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar ejercicios") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ejercicios (${selectedExerciseIds.size} seleccionados)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredExercises) { exercise ->
                    val isSelected = selectedExerciseIds.contains(exercise.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    selectedExerciseIds.remove(exercise.id)
                                } else {
                                    selectedExerciseIds.add(exercise.id)
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    selectedExerciseIds.add(exercise.id)
                                } else {
                                    selectedExerciseIds.remove(exercise.id)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = exercise.name, style = MaterialTheme.typography.bodyLarge)
                            Text(text = exercise.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
