package tech.nimbus.nimbin.ui.features.note

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import tech.nimbus.nimbin.R
import tech.nimbus.nimbin.ui.components.MainBottomNavBar
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.dto.PasteVisibility
import kotlinx.coroutines.delay

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
    val context = LocalContext.current

    var dropdownExpanded by remember { mutableStateOf(false) }

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
            OutlinedTextField(
                value = content,
                onValueChange = viewModel::onContentChange,
                label = { Text(stringResource(id = R.string.note_content_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
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
                    PasteVisibility.values().forEach { v ->
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
