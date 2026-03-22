package ru.omc.myspaceapp.features.asteroids

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun AsteroidsScreen(
    onNavigateToDetails: (String) -> Unit,
    viewModel: AsteroidsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.processIntent(AsteroidsIntent.Load)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("☄️ Астероиды") })
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: ${state.error}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.processIntent(AsteroidsIntent.Load) }) {
                            Text("Повторить")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "За неделю найдено: ${state.asteroids.size}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(state.asteroids) { asteroid ->
                        AsteroidCard(
                            asteroid = asteroid,
                            onClick = { onNavigateToDetails(asteroid.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AsteroidCard(
    asteroid: AsteroidDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (asteroid.isPotentiallyHazardous)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = asteroid.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                if (asteroid.isPotentiallyHazardous) {
                    Text(
                        text = "⚠️ ОПАСНЫЙ",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Размер: ${(asteroid.estimatedDiameter.kilometers.min * 1000).toInt()} - " +
                        "${(asteroid.estimatedDiameter.kilometers.max * 1000).toInt()} м",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}