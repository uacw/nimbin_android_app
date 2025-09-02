package tech.nimbus.nimbin.ui.features.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.snapshotFlow
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import tech.nimbus.nimbin.R
import tech.nimbus.nimbin.domain.repository.AuthResult
import tech.nimbus.nimbin.ui.navigation.Graph
import tech.nimbus.nimbin.ui.navigation.MainAppScreen
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import androidx.compose.foundation.layout.Arrangement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val logoutState = viewModel.logoutState
    val uiState = viewModel.publicUiState

    LaunchedEffect(logoutState) {
        if (logoutState is AuthResult.Success) {
            navController.navigate(Graph.AUTHENTICATION) {
                popUpTo(Graph.ROOT) { inclusive = true }
                launchSingleTop = true
            }
            viewModel.resetLogoutState()
        }
    }

    val swipeRefreshState = rememberSwipeRefreshState(uiState.isRefreshing)
    val listState = rememberLazyListState()

    // Автоподгрузка следующей страницы
    LaunchedEffect(listState, uiState.items.size, uiState.endReached) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                val last = lastVisible ?: return@collect
                val total = listState.layoutInfo.totalItemsCount
                if (total > 0 && last >= total - 3) {
                    viewModel.loadNextPage()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.home_screen_title)) }
            )
        },
        bottomBar = { tech.nimbus.nimbin.ui.components.MainBottomNavBar(navController = navController) }
    ) { padding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.error != null && uiState.items.isEmpty() -> FullError(message = uiState.error) { viewModel.refresh() }
                uiState.isRefreshing && uiState.items.isEmpty() -> PlaceholderList(count = 6)
                uiState.items.isEmpty() -> EmptyState()
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), // Добавлены отступы по бокам
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp) // Красивые отступы между элементами
                    ) {
                        items(uiState.items, key = { it.id }) { paste ->
                            PasteCard(paste,
                                onClick = { navController.navigate(MainAppScreen.PasteDetail.build(paste.id)) },
                                navToAuthor = { uid -> navController.navigate(MainAppScreen.UserProfile.build(uid)) }
                            )
                        }
                        item {
                            when {
                                uiState.isLoadingMore -> {
                                    Spacer(Modifier.height(8.dp))
                                    PlaceholderList(count = 2, compact = true)
                                }
                                uiState.error != null -> {
                                    Spacer(Modifier.height(8.dp))
                                    InlineRetry(uiState.error) { viewModel.loadNextPage() }
                                }
                                uiState.endReached -> { EndReachedLabel() }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FullError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.public_pastes_error, message), color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text(stringResource(id = R.string.public_pastes_retry)) }
    }
}

@Composable
private fun InlineRetry(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.public_pastes_error, message), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(4.dp))
        TextButton(onClick = onRetry) { Text(stringResource(id = R.string.public_pastes_retry)) }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(id = R.string.public_pastes_empty))
    }
}

@Composable
private fun EndReachedLabel() {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text = stringResource(id = R.string.home_end_reached_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PlaceholderList(count: Int, compact: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // Добавляем отступы по бокам для consistency с загруженным контентом
    ) {
        repeat(count) { PastePlaceholderCard(compact) }
    }
}

@Composable
private fun PasteCard(paste: PasteDto, onClick: () -> Unit, navToAuthor: ((String) -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(paste.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(paste.content.take(160))
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val authorLabel = when {
                    paste.authorDisplayName != null -> paste.authorDisplayName + (if (paste.authorUsername != null) " (@${paste.authorUsername})" else "")
                    paste.authorUsername != null -> "@${paste.authorUsername}"
                    else -> null
                }
                val uid = paste.userId
                if (authorLabel != null && uid != null) {
                    Text(
                        text = authorLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { navToAuthor?.invoke(uid) }
                    )
                } else {
                    Spacer(Modifier.width(0.dp))
                }
                Text(visibilityLabel(paste.visibility), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun visibilityLabel(v: PasteVisibility): String = when (v) {
    PasteVisibility.PUBLIC -> stringResource(id = R.string.visibility_public)
    PasteVisibility.UNLISTED -> stringResource(id = R.string.visibility_unlisted)
    PasteVisibility.PRIVATE -> stringResource(id = R.string.visibility_private)
}

@Composable
private fun PastePlaceholderCard(compact: Boolean = false) {
    val shimmerAnim = rememberInfiniteTransition(label = "shimmer")
    val alpha = shimmerAnim.animateFloat(
         initialValue = 0.3f,
         targetValue = 0.9f,
         animationSpec = infiniteRepeatable(
             animation = tween(durationMillis = 900),
             repeatMode = RepeatMode.Reverse
         ), label = "alpha"
     )
    val heightMain = if (compact) 36.dp else 48.dp
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Box(
                Modifier
                    .fillMaxWidth(0.6f)
                    .height(18.dp)
                    .alpha(alpha.value)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(heightMain)
                    .alpha(alpha.value)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f), shape = MaterialTheme.shapes.small)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth(0.3f)
                    .height(14.dp)
                    .alpha(alpha.value)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(navController = rememberNavController())
    }
}
