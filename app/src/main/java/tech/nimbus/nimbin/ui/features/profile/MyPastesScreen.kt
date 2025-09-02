package tech.nimbus.nimbin.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import tech.nimbus.shared.dto.PasteVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPastesScreen(
    navController: NavController,
    viewModel: MyPastesViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    val swipeRefreshState = rememberSwipeRefreshState(uiState.isRefreshing)
    val listState = rememberLazyListState()

    LaunchedEffect(listState, uiState.items.size, uiState.endReached) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                val last = lastVisible ?: return@collect
                val total = listState.layoutInfo.totalItemsCount
                if (total > 0 && last >= total - 3) viewModel.loadNextPage()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.my_pastes_title)) }
            )
        },
        bottomBar = { MainBottomNavBar(navController) }
    ) { padding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.tokenMissing -> TokenMissing()
                uiState.error != null && uiState.items.isEmpty() -> ErrorBlock(uiState.error) { viewModel.refresh() }
                uiState.isRefreshing && uiState.items.isEmpty() -> LoadingBlock()
                uiState.filtered.isEmpty() -> EmptyFiltered(uiState.filter) { viewModel.setFilter(MyPastesFilter.ALL) }
                else -> {
                    Column(Modifier.fillMaxSize()) {
                        FilterRow(current = uiState.filter, onChange = viewModel::setFilter)
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.filtered, key = { it.id }) { paste ->
                                PasteCardSmall(
                                    paste,
                                    onClick = { navController.navigate(MainAppScreen.PasteDetail.build(paste.id)) },
                                    onAuthor = { uid -> navController.navigate(MainAppScreen.UserProfile.build(uid)) }
                                )
                            }
                            item {
                                when {
                                    uiState.isLoadingMore -> {
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) { CircularProgressIndicator() }
                                    }
                                    uiState.error != null && uiState.items.isNotEmpty() -> InlineError(uiState.error) { viewModel.loadNextPage() }
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
private fun TokenMissing() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(id = R.string.my_pastes_login_required), color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ErrorBlock(message: String, retry: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(id = R.string.my_pastes_error, message), color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = retry) { Text(stringResource(id = R.string.my_pastes_retry)) }
    }
}

@Composable
private fun LoadingBlock() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun InlineError(message: String, retry: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(id = R.string.my_pastes_error, message), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = retry) { Text(stringResource(id = R.string.my_pastes_retry)) }
    }
}

@Composable
private fun FilterRow(current: MyPastesFilter, onChange: (MyPastesFilter) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(MyPastesFilter.values()) { filter ->
            ModernFilterChip(
                filter = filter,
                selected = current == filter,
                onClick = { onChange(filter) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernFilterChip(
    filter: MyPastesFilter,
    selected: Boolean,
    onClick: () -> Unit
) {
    val (label, icon) = when (filter) {
        MyPastesFilter.ALL -> stringResource(id = R.string.my_pastes_filter_all) to Icons.Default.List
        MyPastesFilter.PUBLIC -> stringResource(id = R.string.my_pastes_filter_public) to Icons.Default.Public
        MyPastesFilter.UNLISTED -> stringResource(id = R.string.my_pastes_filter_unlisted) to Icons.Default.Link
        MyPastesFilter.PRIVATE -> stringResource(id = R.string.my_pastes_filter_private) to Icons.Default.Lock
    }

    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        selected = selected,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.primaryContainer,
            borderWidth = 1.dp,
            selectedBorderWidth = 0.dp
        )
    )
}

@Composable
private fun EmptyFiltered(filter: MyPastesFilter, reset: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(id = R.string.my_pastes_empty))
        Spacer(Modifier.height(8.dp))
        if (filter != MyPastesFilter.ALL) TextButton(onClick = reset) { Text(stringResource(id = R.string.my_pastes_reset_filter)) }
    }
}

@Composable
private fun PasteCardSmall(paste: PasteDto, onClick: () -> Unit, onAuthor: ((String) -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(paste.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(paste.content.take(100), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val authorLabel = when {
                    paste.authorDisplayName != null -> paste.authorDisplayName + (if (paste.authorUsername != null) " (@${paste.authorUsername})" else "")
                    paste.authorUsername != null -> "@${paste.authorUsername}"
                    else -> null
                }
                val uid = paste.userId
                if (authorLabel != null && uid != null) {
                    Text(authorLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onAuthor?.invoke(uid) })
                } else {
                    Spacer(Modifier.width(0.dp))
                }
                Text(visibilityLabel(paste.visibility), style = MaterialTheme.typography.labelSmall)
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
