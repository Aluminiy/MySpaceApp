package ru.omc.myspaceapp.features.asteroids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import ru.omc.myspaceapp.data.model.AsteroidDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsteroidDetailsScreen(
    asteroidId: String,
    onBack: () -> Unit,
    viewModel: AsteroidDetailsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(asteroidId) {
        viewModel.processIntent(AsteroidDetailsIntent.Load(asteroidId))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали астероида") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (state.asteroid != null) {
                        IconButton(
                            onClick = {
                                viewModel.processIntent(AsteroidDetailsIntent.ToggleFavorite(asteroidId))
                            }
                        ) {
                            Icon(
                                imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                                contentDescription = "В избранное",
                                tint = if (state.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: ${state.error}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.processIntent(AsteroidDetailsIntent.Load(asteroidId)) }) {
                            Text("Повторить")
                        }
                    }
                }
            }
            state.asteroid != null -> {
                val asteroid = state.asteroid!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = asteroid.name, style = MaterialTheme.typography.headlineMedium)

                    if (asteroid.isPotentiallyHazardous) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("⚠️ Потенциально опасный") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("📏 Размер", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Мин: ${asteroid.estimatedDiameter.kilometers.min.toInt()} м")
                            Text("Макс: ${asteroid.estimatedDiameter.kilometers.max.toInt()} м")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("🛰️ Сближение", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    asteroid.closeApproachData.firstOrNull()?.let { approach ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Дата: ${approach.closeApproachDate}")
                                Text("Скорость: ${approach.relativeVelocity.kilometersPerHour} км/ч")
                                Text("Расстояние: ${approach.missDistance.kilometers} км")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}