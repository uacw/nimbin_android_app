package tech.nimbus.nimbin.ui.features.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import tech.nimbus.nimbin.R
import tech.nimbus.nimbin.ui.navigation.MainAppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    userId: String,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) { viewModel.init(userId) }
    val ui = viewModel.uiState
    val listState = rememberLazyListState()

    LaunchedEffect(listState, ui.pastes.size, ui.endReached) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { last ->
                val idx = last ?: return@collect
                val total = listState.layoutInfo.totalItemsCount
                if (total > 0 && idx >= total - 3) viewModel.loadNextPage()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(ui.headerTitle ?: stringResource(id = R.string.user_profile_title)) })
        }
    ) { padding ->
        when {
            ui.error != null && ui.pastes.isEmpty() && ui.profile == null -> FullErrorBlock(ui.error) { viewModel.refresh() }
            ui.loading && ui.profile == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    state = listState
                ) {
                    item { ProfileHeader(ui) }
                    items(ui.pastes, key = { it.id }) { paste ->
                        UserProfilePasteCard(paste) {
                            navController.navigate(MainAppScreen.PasteDetail.build(paste.id))
                        }
                    }
                    item {
                        when {
                            ui.loadingMore -> {
                                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                            }
                            ui.error != null -> {
                                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(ui.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    TextButton(onClick = { viewModel.loadNextPage(force = true) }) { Text(stringResource(id = R.string.user_profile_retry)) }
                                }
                            }
                            ui.endReached && ui.pastes.isNotEmpty() -> {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { Text(stringResource(id = R.string.user_profile_end_reached), style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(ui: UserProfileUiState) {
    val profile = ui.profile ?: return
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        val display = profile.user.displayName ?: profile.user.username
        Text(display, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text("@" + profile.user.username, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(id = R.string.user_profile_public_total_counts, profile.publicPastesCount, profile.totalPastesCount ?: 0), style = MaterialTheme.typography.labelMedium)
        Divider(Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun UserProfilePasteCard(paste: PasteDto, onClick: () -> Unit) {
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
            Text(paste.content.take(140), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Text(visibilityLabel(paste.visibility), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun FullErrorBlock(message: String, retry: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = retry) { Text(stringResource(id = R.string.user_profile_retry)) }
    }
}
