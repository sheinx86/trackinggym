package com.example.trackinggym.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.trackinggym.data.entities.Exercise
import com.example.trackinggym.data.entities.SetRecord
import com.example.trackinggym.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val exercises by viewModel.exercises.collectAsState()
    
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var expanded by remember { mutableStateOf(false) }
    
    var dateMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current
    
    var sets by remember { mutableStateOf(listOf(SetRecord(1, 0, 0f))) }
    var comments by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Entreno") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    selectedExercise?.let { ex ->
                        viewModel.saveWorkout(ex.id, dateMs, sets, comments.trim())
                        onNavigateBack()
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Guardar") },
                text = { Text("Guardar Entreno") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            var searchQuery by remember { mutableStateOf("") }
            val sortedExercises = exercises.sortedBy { it.name.lowercase(Locale.getDefault()) }
            val filteredExercises = sortedExercises.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }

            val focusRequester = remember { FocusRequester() }

            var showSelectExerciseDialog by remember { mutableStateOf(false) }

            // Campo de selección (solo lectura)
            OutlinedTextField(
                value = selectedExercise?.name ?: "Selecciona un ejercicio",
                onValueChange = {},
                readOnly = true,
                label = { Text("Ejercicio") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSelectExerciseDialog) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSelectExerciseDialog = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            if (showSelectExerciseDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showSelectExerciseDialog = false 
                        searchQuery = "" 
                    },
                    title = { Text("Seleccionar Ejercicio") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Buscar...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .focusRequester(focusRequester),
                                singleLine = true
                            )
                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }
                            
                            LazyColumn(
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                if (filteredExercises.isEmpty()) {
                                    item {
                                        Text("No se encontraron ejercicios", modifier = Modifier.padding(16.dp))
                                    }
                                } else {
                                    items(filteredExercises) { ex ->
                                        Text(
                                            text = ex.name,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedExercise = ex
                                                    searchQuery = ""
                                                    showSelectExerciseDialog = false
                                                }
                                                .padding(16.dp)
                                        )
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { 
                            showSelectExerciseDialog = false 
                            searchQuery = "" 
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selector de Fecha
            OutlinedTextField(
                value = formatter.format(Date(dateMs)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha") },
                trailingIcon = {
                    IconButton(onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = dateMs }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                dateMs = newCal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Icon(Icons.Default.DateRange, "Seleccionar Fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comentarios
            OutlinedTextField(
                value = comments,
                onValueChange = { comments = it },
                label = { Text("Comentarios (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Series", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { 
                    sets = sets + SetRecord(sets.size + 1, 0, 0f) 
                }) {
                    Text("+ Añadir serie")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Lista de series
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(sets) { index, setRecord ->
                    SetRow(
                        setNumber = index + 1,
                        reps = setRecord.reps,
                        weight = setRecord.weight,
                        onRepsChange = { newReps ->
                            val mut = sets.toMutableList()
                            mut[index] = setRecord.copy(reps = newReps)
                            sets = mut
                        },
                        onWeightChange = { newWeight ->
                            val mut = sets.toMutableList()
                            mut[index] = setRecord.copy(weight = newWeight)
                            sets = mut
                        },
                        onDelete = {
                            val mut = sets.toMutableList()
                            mut.removeAt(index)
                            // Recalculate set numbers
                            sets = mut.mapIndexed { i, record -> record.copy(setNumber = i + 1) }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // ESPACIO PARA EL FAB
            }
        }
    }
}

@Composable
fun SetRow(
    setNumber: Int,
    reps: Int,
    weight: Float,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Float) -> Unit,
    onDelete: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("S$setNumber", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.labelLarge)
            
            OutlinedTextField(
                value = if (reps == 0) "" else reps.toString(),
                onValueChange = { onRepsChange(it.toIntOrNull() ?: 0) },
                label = { Text("Reps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            OutlinedTextField(
                value = if (weight == 0f) "" else weight.toString(),
                onValueChange = { onWeightChange(it.toFloatOrNull() ?: 0f) },
                label = { Text("Kg") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar serie", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
