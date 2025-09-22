package tech.nimbus.nimbin.ui.features.paste

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import tech.nimbus.nimbin.R
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.nimbin.ui.navigation.MainAppScreen
import tech.nimbus.nimbin.ui.components.CodeEditor
import tech.nimbus.nimbin.ui.components.CodeLangMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasteDetailScreen(
    navController: NavController,
    pasteId: String,
    viewModel: PasteDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(pasteId) {
        if (pasteId.isNotBlank()) viewModel.load(pasteId)
    }

    val state = viewModel.state
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        if (pasteId.isNotBlank()) viewModel.load(pasteId)
    }

    // Обновление после возврата с Edit экрана
    val handle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(handle) {
        val flow = handle?.getStateFlow("paste_updated", false)
        flow?.collectLatest { updated ->
            if (updated) {
                // короткая задержка, чтобы сервер гарантированно отдал свежие данные
                delay(250)
                refreshTrigger++
                handle.set("paste_updated", false)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = when (state) {
                    is PasteResult.Success -> state.data.title.takeIf { it.isNotBlank() } ?: pasteId
                    else -> pasteId
                }) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.paste_detail_nav_back_cd))
                    }
                },
                actions = {
                    IconButton(onClick = { refreshTrigger++ }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(id = R.string.paste_detail_refresh_cd))
                    }

                    // Избранное
                    val isFav = (state as? PasteResult.Success)?.data?.isFavorite
                    if (isFav != null) {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            if (isFav) Icon(Icons.Filled.Star, contentDescription = null) else Icon(Icons.Outlined.StarBorder, contentDescription = null)
                        }
                    }

                    // показываем Edit только владельцу
                    val canEdit = (state as? PasteResult.Success<PasteDto>)?.data?.userId == viewModel.currentUserId
                    if (canEdit) {
                        IconButton(onClick = { navController.navigate(MainAppScreen.EditPaste.build(pasteId)) }) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(id = R.string.paste_detail_edit_cd))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (state) {
                is PasteResult.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                is PasteResult.Error -> {
                    Column(Modifier.fillMaxWidth()) {
                        Text(stringResource(id = R.string.paste_detail_error, state.message), color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { refreshTrigger++ }) { Text(stringResource(id = R.string.public_pastes_retry)) }
                    }
                }
                is PasteResult.Success -> PasteContent(state.data, navController)
                null -> {
                    if (pasteId.isBlank()) {
                        Text(stringResource(id = R.string.paste_detail_id_blank), color = MaterialTheme.colorScheme.error)
                    } else {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
private fun PasteContent(paste: PasteDto, navController: NavController) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(paste.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            AssistChip(onClick = {}, label = { Text(paste.visibility.name) })
        }
        Spacer(Modifier.height(8.dp))
        val authorLabel = when {
            paste.authorDisplayName != null -> paste.authorDisplayName + (if (paste.authorUsername != null) " (@${paste.authorUsername})" else "")
            paste.authorUsername != null -> "@${paste.authorUsername}"
            else -> null
        }
        val uid = paste.userId
        if (authorLabel != null && uid != null) {
            Text(
                text = authorLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { navController.navigate(MainAppScreen.UserProfile.build(uid)) }
            )
            Spacer(Modifier.height(4.dp))
        }
        Text(text = stringResource(id = R.string.paste_detail_created, paste.createdAt), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(4.dp))
        Text(text = stringResource(id = R.string.paste_detail_views, paste.viewCount), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(4.dp))
        Text(text = "Syntax: ${paste.syntaxLanguage}", style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        // Code viewer area
        CodeEditor(
            value = paste.content,
            onValueChange = {},
            language = CodeLangMapper.fromString(paste.syntaxLanguage),
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
