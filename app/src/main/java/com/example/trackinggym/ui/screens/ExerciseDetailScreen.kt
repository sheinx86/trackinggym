package com.example.trackinggym.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.trackinggym.data.entities.ExerciseLog
import com.example.trackinggym.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    viewModel: MainViewModel,
    exerciseId: Long,
    exerciseName: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddWorkout: (Long) -> Unit,
    onNavigateToEditWorkout: (Long) -> Unit
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

    var currentPage by remember { mutableIntStateOf(0) }
    val itemsPerPage = 5

    LaunchedEffect(filterDateMs) {
        currentPage = 0
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddWorkout(exerciseId) },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Entreno")
            }
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
                    // Última marca
                    item {
                        Text("Última Marca", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        LogRecordCard(
                            log = filteredLogs.first(),
                            onDelete = { logToDelete = filteredLogs.first() },
                            onEdit = { onNavigateToEditWorkout(filteredLogs.first().id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                    }

                    // Gráfica de evolución
                    if (filteredLogs.size >= 2) {
                        item {
                            Text("Evolución", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            WeightHistoryChart(
                                logs = filteredLogs,
                                modifier = Modifier.fillMaxWidth().height(200.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                        }
                    }

                    // Historial Completo con paginación
                    item {
                        Text("Historial Completo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    val totalPages = (filteredLogs.size + itemsPerPage - 1) / itemsPerPage
                    val pagedLogs = filteredLogs.drop(currentPage * itemsPerPage).take(itemsPerPage)

                    items(pagedLogs) { log ->
                        LogRecordCard(
                            log = log,
                            onDelete = { logToDelete = log },
                            onEdit = { onNavigateToEditWorkout(log.id) }
                        )
                    }

                    // Paginator Controls
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { if (currentPage > 0) currentPage-- },
                                enabled = currentPage > 0
                            ) {
                                Text("Anterior")
                            }
                            Text("Página ${currentPage + 1} de $totalPages")
                            TextButton(
                                onClick = { if (currentPage < totalPages - 1) currentPage++ },
                                enabled = currentPage < totalPages - 1
                            ) {
                                Text("Siguiente")
                            }
                        }
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
fun LogRecordCard(log: ExerciseLog, onDelete: () -> Unit, onEdit: () -> Unit) {
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
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
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

@Composable
fun WeightHistoryChart(logs: List<ExerciseLog>, modifier: Modifier = Modifier) {
    if (logs.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No hay suficientes datos para la gráfica")
        }
        return
    }

    // Sort ascending for chart
    val sortedLogs = logs.sortedBy { it.dateMs }
    
    // Calculate average weight for each log
    val logWeights = sortedLogs.map { log ->
        if (log.sets.isEmpty()) 0f else log.sets.map { it.weight }.average().toFloat()
    }
    
    val maxWeight = logWeights.maxOrNull() ?: 0f
    val minWeight = logWeights.minOrNull() ?: 0f
    val weightRange = if (maxWeight == minWeight) 1f else maxWeight - minWeight

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelSmall.copy(color = onSurfaceColor)

    var tappedPoint by remember { mutableStateOf<Pair<Int, Offset>?>(null) }
    val formatter = remember { SimpleDateFormat("dd/MM/yy", Locale.getDefault()) }

    Canvas(modifier = modifier.pointerInput(Unit) {
        detectTapGestures { tapOffset ->
            val paddingX = 80f
            val width = size.width
            val chartWidth = width - paddingX
            val stepX = chartWidth / (sortedLogs.size - 1).coerceAtLeast(1).toFloat()
            
            // Calculate closest index
            val index = ((tapOffset.x - paddingX) / stepX).roundToInt().coerceIn(0, sortedLogs.size - 1)
            
            val y = size.height - 60f - (((logWeights[index] - minWeight) / weightRange) * (size.height - 60f) * 0.8f + (size.height - 60f) * 0.1f)
            val pointOffset = Offset(paddingX + index * stepX, y)
            
            // Check distance to point to ensure it was actually tapped near the point
            if ((tapOffset - pointOffset).getDistance() < 100f) {
                tappedPoint = index to pointOffset
            } else {
                tappedPoint = null
            }
        }
    }) {
        val width = size.width
        val height = size.height
        
        // Add padding for axes
        val paddingX = 80f
        val paddingY = 60f
        val chartWidth = width - paddingX
        val chartHeight = height - paddingY

        val stepX = chartWidth / (sortedLogs.size - 1).coerceAtLeast(1).toFloat()

        // Draw Axes
        drawLine(
            color = onSurfaceColor,
            start = Offset(paddingX, 0f),
            end = Offset(paddingX, chartHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = onSurfaceColor,
            start = Offset(paddingX, chartHeight),
            end = Offset(width, chartHeight),
            strokeWidth = 2f
        )

        // Draw Y Axis labels (Max and Min)
        drawText(
            textMeasurer = textMeasurer,
            text = String.format("%.1f", maxWeight),
            style = textStyle,
            topLeft = Offset(0f, chartHeight * 0.1f - 20f)
        )
        drawText(
            textMeasurer = textMeasurer,
            text = String.format("%.1f", minWeight),
            style = textStyle,
            topLeft = Offset(0f, chartHeight * 0.9f - 20f)
        )

        // Draw path and points
        val path = Path()
        val points = mutableListOf<Offset>()
        
        sortedLogs.forEachIndexed { index, log ->
            val w = logWeights[index]
            val normalizedY = (w - minWeight) / weightRange
            // Y is inverted
            val y = chartHeight - (normalizedY * chartHeight * 0.8f + chartHeight * 0.1f)
            val x = paddingX + index * stepX

            val point = Offset(x, y)
            points.add(point)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            // Draw points
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = point
            )
            
            // Draw X Axis labels for first and last point
            if (index == 0 || index == sortedLogs.size - 1) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = formatter.format(Date(log.dateMs)),
                    style = textStyle,
                    topLeft = Offset(x - 30f, chartHeight + 10f)
                )
            }
        }
        
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Draw popup if tapped
        tappedPoint?.let { (index, offset) ->
            val log = sortedLogs[index]
            val weight = logWeights[index]
            val text = "${formatter.format(Date(log.dateMs))}\n${String.format("%.1f", weight)} kg"
            
            val textLayoutResult = textMeasurer.measure(text, textStyle)
            val popupWidth = textLayoutResult.size.width + 30f
            val popupHeight = textLayoutResult.size.height + 20f
            
            var popupX = offset.x - popupWidth / 2
            if (popupX < paddingX) popupX = paddingX
            if (popupX + popupWidth > width) popupX = width - popupWidth
            
            var popupY = offset.y - popupHeight - 20f
            if (popupY < 0) popupY = offset.y + 20f
            
            drawRoundRect(
                color = surfaceVariantColor,
                topLeft = Offset(popupX, popupY),
                size = androidx.compose.ui.geometry.Size(popupWidth, popupHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )
            
            drawText(
                textLayoutResult = textLayoutResult,
                color = onSurfaceColor,
                topLeft = Offset(popupX + 15f, popupY + 10f)
            )
        }
    }
}
