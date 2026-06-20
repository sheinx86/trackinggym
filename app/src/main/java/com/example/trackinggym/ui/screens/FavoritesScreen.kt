package com.example.trackinggym.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trackinggym.data.entities.ExerciseWithLatestLog
import com.example.trackinggym.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: MainViewModel, onNavigateToDetail: (Long, String) -> Unit, onNavigateToAddWorkout: () -> Unit) {
    val allFavorites by viewModel.favoriteExercises.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val categories = listOf("Brazos", "Cardio", "Core", "Espalda", "Hombros", "Pecho", "Piernas")

    var currentPage by remember { mutableIntStateOf(0) }
    val itemsPerPage = 5

    val filteredFavorites = allFavorites.filter { item ->
        val matchesSearch = item.exercise.name.contains(searchQuery, ignoreCase = true) || item.exercise.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == null || item.exercise.category == selectedCategory
        matchesSearch && matchesCategory
    }.sortedBy { it.exercise.name.lowercase(Locale.getDefault()) }

    LaunchedEffect(searchQuery, selectedCategory) {
        currentPage = 0
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddWorkout,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Entreno")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar favorito o categoría...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Category Filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("Todos") }
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredFavorites.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay favoritos que coincidan.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val totalPages = (filteredFavorites.size + itemsPerPage - 1).coerceAtLeast(0) / itemsPerPage
                    val pagedFavorites = filteredFavorites.drop(currentPage * itemsPerPage).take(itemsPerPage)

                    items(pagedFavorites) { item ->
                        FavoriteExerciseItem(
                            item = item,
                            onClick = { onNavigateToDetail(item.exercise.id, item.exercise.name) }
                        )
                    }

                    if (totalPages > 1) {
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
        }
    }
}

@Composable
fun FavoriteExerciseItem(
    item: ExerciseWithLatestLog,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        text = item.exercise.category,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            if (item.latestLogDateMs != null && item.latestLogSets != null) {
                val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                val dateStr = formatter.format(Date(item.latestLogDateMs))
                
                Text(
                    text = "Último: $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                val setsSummary = item.latestLogSets.joinToString(", ") { "s${it.setNumber}: ${it.reps}x${it.weight}kg" }
                Text(
                    text = setsSummary.ifEmpty { "Sin series" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "Sin entrenamientos previos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
