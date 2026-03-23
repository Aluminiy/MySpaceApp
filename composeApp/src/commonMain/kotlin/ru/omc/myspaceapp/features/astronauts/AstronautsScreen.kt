package ru.omc.myspaceapp.features.astronauts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import ru.omc.myspaceapp.data.model.AstronautDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstronautsScreen(
    onNavigateToDetails: (String) -> Unit,
    viewModel: AstronautsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.processIntent(AstronautsIntent.Load)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("👨‍ Космонавты") }) }
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
                        Button(onClick = { viewModel.processIntent(AstronautsIntent.Load) }) {
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
                            text = "На орбите: ${state.astronauts.size} человек",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(state.astronauts) { astronaut ->
                        AstronautCard(
                            astronaut = astronaut,
                            isFavorite = state.favoriteNames.contains(astronaut.name),
                            onToggleFavorite = { name ->
                                viewModel.processIntent(AstronautsIntent.ToggleFavorite(name))
                            },
                            onClick = { onNavigateToDetails(astronaut.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AstronautCard(
    astronaut: AstronautDto,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🚀 ", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = astronaut.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        text = astronaut.craft,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            IconButton(
                onClick = {
                    onToggleFavorite(astronaut.name)
                },
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