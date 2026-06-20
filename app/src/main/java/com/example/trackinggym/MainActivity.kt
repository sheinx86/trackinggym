package com.example.trackinggym

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.trackinggym.theme.TrackingGymTheme
import com.example.trackinggym.ui.MainViewModel
import com.example.trackinggym.ui.screens.ExercisesScreen
import com.example.trackinggym.ui.screens.FavoritesScreen
import com.example.trackinggym.ui.navigation.AppNavigation
import com.example.trackinggym.utils.PreferencesManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onNavigateToDetail: (Long, String) -> Unit,
    onNavigateToCreateRoutine: () -> Unit,
    onNavigateToRoutineDetail: (Long, String) -> Unit,
    onNavigateToEditRoutine: (Long) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Rutinas" to Icons.Default.List,
        "Favoritos" to Icons.Default.Favorite, 
        "Ejercicios" to Icons.Default.List
    )
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefsManager = remember { PreferencesManager(context) }
    
    var showMenu by remember { mutableStateOf(false) }
    var showImportConfirm by remember { mutableStateOf(false) }
    
    val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
    val now = System.currentTimeMillis()
    var showReminder by remember { 
        mutableStateOf(
            (prefsManager.lastExportTimeMs > 0 && now - prefsManager.lastExportTimeMs > sevenDaysMs) 
            && (now - prefsManager.lastReminderTimeMs > sevenDaysMs)
        ) 
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { 
            coroutineScope.launch {
                val json = viewModel.getExportDataJson()
                context.contentResolver.openOutputStream(it)?.use { os ->
                    os.write(json.toByteArray())
                }
                prefsManager.lastExportTimeMs = System.currentTimeMillis()
                Toast.makeText(context, "Datos exportados correctamente", Toast.LENGTH_SHORT).show()
                showReminder = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            coroutineScope.launch {
                val json = context.contentResolver.openInputStream(it)?.bufferedReader().use { reader ->
                    reader?.readText()
                }
                if (json != null) {
                    val success = viewModel.importDataFromJson(json)
                    if (success) {
                        Toast.makeText(context, "Datos importados. Reiniciando lista...", Toast.LENGTH_SHORT).show()
                        prefsManager.lastExportTimeMs = System.currentTimeMillis()
                    } else {
                        Toast.makeText(context, "Error: archivo no válido", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Tracking Gym") },
                actions = {
                    TextButton(onClick = onNavigateToAddWorkout) {
                        Text("Añadir Entreno")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Exportar Datos") },
                            onClick = {
                                showMenu = false
                                val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                exportLauncher.launch("TrackingGymBackup_$dateStr.json")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Importar Datos") },
                            onClick = {
                                showMenu = false
                                showImportConfirm = true
                            }
                        )
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
                0 -> com.example.trackinggym.ui.screens.RoutinesScreen(
                        viewModel = viewModel, 
                        onNavigateToCreateRoutine = onNavigateToCreateRoutine, 
                        onNavigateToRoutineDetail = onNavigateToRoutineDetail,
                        onNavigateToEditRoutine = onNavigateToEditRoutine
                     )
                1 -> FavoritesScreen(viewModel, onNavigateToDetail, onNavigateToAddWorkout)
                2 -> ExercisesScreen(viewModel, onNavigateToDetail)
            }
        }
        
        if (showImportConfirm) {
            AlertDialog(
                onDismissRequest = { showImportConfirm = false },
                title = { Text("Importar Datos") },
                text = { Text("¡Atención! Importar una copia de seguridad sobrescribirá TODOS tus ejercicios y registros actuales. ¿Estás seguro de que deseas continuar?") },
                confirmButton = {
                    TextButton(onClick = {
                        showImportConfirm = false
                        importLauncher.launch(arrayOf("application/json"))
                    }) {
                        Text("Sí, importar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportConfirm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showReminder) {
            AlertDialog(
                onDismissRequest = { 
                    showReminder = false
                    prefsManager.lastReminderTimeMs = System.currentTimeMillis()
                },
                title = { Text("Recordatorio de Seguridad") },
                text = { Text("Hace más de 7 días que no haces una copia de seguridad de tus entrenamientos. ¿Deseas exportar tus datos ahora?") },
                confirmButton = {
                    TextButton(onClick = {
                        showReminder = false
                        val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        exportLauncher.launch("TrackingGymBackup_$dateStr.json")
                    }) {
                        Text("Sí, exportar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showReminder = false 
                        prefsManager.lastReminderTimeMs = System.currentTimeMillis()
                    }) {
                        Text("Recordar más tarde")
                    }
                }
            )
        }
    }
}
