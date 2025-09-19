package tech.nimbus.nimbin.ui.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import tech.nimbus.nimbin.R
import tech.nimbus.nimbin.ui.components.MainBottomNavBar
import tech.nimbus.nimbin.ui.navigation.MainAppScreen
import tech.nimbus.shared.dto.PasteDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val ui = viewModel.uiState
    val swipe = rememberSwipeRefreshState(ui.isRefreshing)
    val listState = rememberLazyListState()

    LaunchedEffect(listState, ui.items.size, ui.endReached) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                val last = lastVisible ?: return@collect
                val total = listState.layoutInfo.totalItemsCount
                if (total > 0 && last >= total - 3) viewModel.loadNextPage()
            }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.favorites_title)) }) },
        bottomBar = { MainBottomNavBar(navController) }
    ) { padding ->
        SwipeRefresh(
            state = swipe,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                ui.tokenMissing -> TokenMissingFavorites()
                ui.error != null && ui.items.isEmpty() -> FavoritesError(message = ui.error) { viewModel.refresh() }
                ui.isRefreshing && ui.items.isEmpty() -> FavoritesLoading()
                ui.items.isEmpty() -> FavoritesEmpty()
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ui.items, key = { it.id }) { paste ->
                            FavoriteItem(
                                paste = paste,
                                onOpen = { navController.navigate(MainAppScreen.PasteDetail.build(paste.id)) },
                                onToggleFavorite = { id, current -> viewModel.toggleFavorite(id, current) }
                            )
                        }
                        item {
                            if (ui.isLoadingMore) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center) {
                                    CircularProgressIndicator()
                                }
                            } else if (ui.error != null) {
                                FavoritesInlineError(ui.error) { viewModel.loadNextPage() }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteItem(paste: PasteDto, onOpen: () -> Unit, onToggleFavorite: (String, Boolean?) -> Unit) {
    Card(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(paste.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(paste.content.take(100), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(paste.authorDisplayName ?: paste.authorUsername?.let { "@" + it } ?: "", style = MaterialTheme.typography.labelSmall)
                IconButton(onClick = { onToggleFavorite(paste.id, paste.isFavorite) }) {
                    if (paste.isFavorite == true) Icon(Icons.Default.Star, contentDescription = null) else Icon(Icons.Outlined.StarBorder, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun FavoritesLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun TokenMissingFavorites() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = stringResource(id = R.string.favorites_login_required), color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun FavoritesError(message: String, retry: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(id = R.string.favorites_error, message), color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = retry) { Text(stringResource(id = R.string.favorites_retry)) }
    }
}

@Composable
private fun FavoritesInlineError(message: String, retry: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(id = R.string.favorites_error, message), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = retry) { Text(stringResource(id = R.string.favorites_retry)) }
    }
}

@Composable
private fun FavoritesEmpty() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(id = R.string.favorites_empty))
    }
}
