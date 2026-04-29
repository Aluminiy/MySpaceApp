package ru.omc.myspaceapp.features.asteroids

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.processIntent(AsteroidsIntent.Load)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(
                state = pullToRefreshState,
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.processIntent(AsteroidsIntent.Refresh) }
            )
    ) {
        when {
            state.isRefreshing -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.testTag("loading_indicator")
                    )
                }
            }
            state.isLoading && !state.isRefreshing -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.testTag("loading_indicator")
                    )
                }
            }
            state.error != null && state.asteroids.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("За неделю найдено: ${state.asteroids.size}")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(state.asteroids) { asteroid ->
                        AsteroidCard(
                            asteroid = asteroid,
                            isFavorite = state.favoriteIds.contains(asteroid.id),
                            onToggleFavorite = { id ->
                                viewModel.processIntent(AsteroidsIntent.ToggleFavorite(id))
                            },
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
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = asteroid.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (asteroid.isPotentiallyHazardous) {
                    AssistChip(
                        onClick = {},
                        label = { Text("⚠️ Опасный") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                val minMeters = (asteroid.estimatedDiameter.kilometers.min * 1000).toInt()
                val maxMeters = (asteroid.estimatedDiameter.kilometers.max * 1000).toInt()

                Text(
                    text = if (minMeters == 0 && maxMeters == 0) {
                        "Размер: неизвестен"
                    } else {
                        "Размер: $minMeters - $maxMeters м"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(
                onClick = { onToggleFavorite(asteroid.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                    contentDescription = if (isFavorite) "Удалить из избранного" else "В избранное",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
