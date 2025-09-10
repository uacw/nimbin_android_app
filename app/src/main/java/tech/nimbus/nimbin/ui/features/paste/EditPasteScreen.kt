package tech.nimbus.nimbin.ui.features.paste

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import tech.nimbus.nimbin.R
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.dto.PasteVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasteScreen(
    navController: NavController,
    pasteId: String,
    viewModel: EditPasteViewModel = hiltViewModel()
) {
    LaunchedEffect(pasteId) {
        if (pasteId.isNotBlank()) viewModel.load(pasteId)
    }

    val loadState = viewModel.loadState
    val saveState = viewModel.saveState
    val conflict = viewModel.conflict
    val languagesState = viewModel.languagesState
    val syntaxLanguages = viewModel.syntaxLanguages
    val syntaxLanguage = viewModel.syntaxLanguage

    var visibilityExpanded by remember { mutableStateOf(false) }
    var syntaxExpanded by remember { mutableStateOf(false) }

    if (conflict) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConflict() },
            title = { Text(stringResource(id = R.string.edit_paste_conflict_title)) },
            text = { Text(stringResource(id = R.string.edit_paste_conflict_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.reloadAfterConflict() }) {
                    Text(stringResource(id = R.string.edit_paste_conflict_reload))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConflict() }) {
                    Text(stringResource(id = R.string.edit_paste_conflict_dismiss))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.edit_paste_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.paste_detail_nav_back_cd))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.save() }, enabled = saveState !is PasteResult.Loading) {
                        if (saveState is PasteResult.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = stringResource(id = R.string.edit_paste_save_cd))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (loadState) {
                is PasteResult.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is PasteResult.Error -> {
                    Column(Modifier.align(Alignment.TopStart).padding(16.dp)) {
                        Text(text = stringResource(id = R.string.edit_paste_error, loadState.message), color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.load(pasteId) }) { Text(stringResource(id = R.string.public_pastes_retry)) }
                    }
                }
                is PasteResult.Success, null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        OutlinedTextField(
                            value = viewModel.title,
                            onValueChange = viewModel::onTitleChange,
                            label = { Text(stringResource(id = R.string.note_title_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = viewModel.content,
                            onValueChange = viewModel::onContentChange,
                            label = { Text(stringResource(id = R.string.note_content_label)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                        Spacer(Modifier.height(12.dp))

                        // Visibility dropdown
                        ExposedDropdownMenuBox(
                            expanded = visibilityExpanded,
                            onExpandedChange = { visibilityExpanded = !visibilityExpanded }
                        ) {
                            OutlinedTextField(
                                value = visibilityLabel(viewModel.visibility),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(id = R.string.create_note_visibility_label)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = visibilityExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = visibilityExpanded,
                                onDismissRequest = { visibilityExpanded = false }
                            ) {
                                PasteVisibility.values().forEach { v ->
                                    DropdownMenuItem(
                                        text = { Text(visibilityLabel(v)) },
                                        onClick = {
                                            viewModel.onVisibilityChange(v)
                                            visibilityExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Syntax languages dropdown
                        when (languagesState) {
                            is PasteResult.Loading -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize))
                                    Spacer(Modifier.width(8.dp))
                                    Text(text = stringResource(id = R.string.edit_paste_syntax_label))
                                }
                            }
                            is PasteResult.Error -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = stringResource(id = R.string.public_pastes_error, languagesState.message), color = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(8.dp))
                                    TextButton(onClick = { viewModel.reloadLanguages() }) { Text(stringResource(id = R.string.public_pastes_retry)) }
                                }
                            }
                            is PasteResult.Success, null -> {
                                ExposedDropdownMenuBox(
                                    expanded = syntaxExpanded,
                                    onExpandedChange = { syntaxExpanded = !syntaxExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = syntaxLanguage,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(stringResource(id = R.string.edit_paste_syntax_label)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = syntaxExpanded) },
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = syntaxExpanded,
                                        onDismissRequest = { syntaxExpanded = false }
                                    ) {
                                        syntaxLanguages.forEach { lang ->
                                            DropdownMenuItem(
                                                text = { Text(lang) },
                                                onClick = {
                                                    viewModel.onSyntaxLanguageChange(lang)
                                                    syntaxExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // TODO: ExpiresAt picker (optional)

                        Spacer(Modifier.height(12.dp))

                        if (saveState is PasteResult.Error) {
                            Text(text = stringResource(id = R.string.edit_paste_error, saveState.message), color = MaterialTheme.colorScheme.error)
                        }
                        if (saveState is PasteResult.Success) {
                            LaunchedEffect(saveState) {
                                navController.previousBackStackEntry?.savedStateHandle?.set("paste_updated", true)
                                navController.popBackStack()
                            }
                            Text(text = stringResource(id = R.string.edit_paste_success), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
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
