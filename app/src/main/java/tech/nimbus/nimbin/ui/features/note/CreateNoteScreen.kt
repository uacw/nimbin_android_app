package tech.nimbus.nimbin.ui.features.note

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import tech.nimbus.nimbin.R
import tech.nimbus.nimbin.ui.components.MainBottomNavBar
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.dto.PasteVisibility
import kotlinx.coroutines.delay
import tech.nimbus.nimbin.ui.components.CodeEditor
import tech.nimbus.nimbin.ui.components.CodeLangMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteScreen(
    navController: NavController,
    viewModel: CreateNoteViewModel = hiltViewModel()
) {
    val title = viewModel.title
    val content = viewModel.content
    val visibility = viewModel.visibility
    val createState = viewModel.createState
    val syntaxLanguage = viewModel.syntaxLanguage
    val languagesState = viewModel.languagesState
    val syntaxLanguages = viewModel.syntaxLanguages

    var dropdownExpanded by remember { mutableStateOf(false) }
    var syntaxExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(createState) {
        if (createState is PasteResult.Success) {
            // Небольшая задержка и очистка формы
            delay(1200)
            viewModel.resetAfterSuccess()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(id = R.string.create_note_title)) }) },
        bottomBar = { MainBottomNavBar(navController = navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = { Text(stringResource(id = R.string.note_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Replaced content input with CodeEditor
            Text(text = stringResource(id = R.string.note_content_label), style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
            ) {
                CodeEditor(
                    value = content,
                    onValueChange = viewModel::onContentChange,
                    language = CodeLangMapper.fromString(syntaxLanguage),
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Visibility dropdown
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = visibilityLabel(visibility),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.create_note_visibility_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    PasteVisibility.entries.forEach { v ->
                        DropdownMenuItem(
                            text = { Text(visibilityLabel(v)) },
                            onClick = {
                                viewModel.onVisibilityChange(v)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.create() },
                enabled = createState !is PasteResult.Loading
            ) {
                if (createState is PasteResult.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize))
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(id = R.string.note_create_action))
                }
            }

            Spacer(Modifier.height(12.dp))

            when (createState) {
                is PasteResult.Error -> Text(
                    text = stringResource(id = R.string.create_note_error, createState.message),
                    color = MaterialTheme.colorScheme.error
                )
                is PasteResult.Success -> Text(
                    text = stringResource(id = R.string.create_note_success),
                    color = MaterialTheme.colorScheme.primary
                )
                else -> {}
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
