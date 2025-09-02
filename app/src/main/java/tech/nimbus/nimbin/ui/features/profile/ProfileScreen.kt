package tech.nimbus.nimbin.ui.features.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import tech.nimbus.nimbin.R
import tech.nimbus.nimbin.domain.repository.AuthResult
import tech.nimbus.nimbin.ui.components.MainBottomNavBar
import tech.nimbus.nimbin.ui.navigation.Graph
import tech.nimbus.nimbin.ui.navigation.MainAppScreen
import tech.nimbus.shared.utils.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val logoutState = viewModel.logoutState
    val profileState = viewModel.profileState
    var showEdit by remember { mutableStateOf(false) }

    LaunchedEffect(logoutState) {
        if (logoutState is AuthResult.Success) {
            navController.navigate(Graph.AUTHENTICATION) {
                popUpTo(Graph.ROOT) { inclusive = true }
                launchSingleTop = true
            }
            viewModel.resetLogout()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.profile_title)) }) },
        bottomBar = { MainBottomNavBar(navController = navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Основной контент профиля
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (profileState) {
                    is MyProfileUiState.Loading, MyProfileUiState.Idle, is MyProfileUiState.Updating -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is MyProfileUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(profileState.message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadProfile() }) { Text(stringResource(id = R.string.profile_retry)) }
                        }
                    }
                    is MyProfileUiState.Data -> {
                        val profile = profileState.profile
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start
                        ) {
                            val display = profile.user.displayName ?: profile.user.username
                            Text(display, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(4.dp))
                            Text("@" + profile.user.username, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(id = R.string.profile_public_total_counts, profile.publicPastesCount, profile.totalPastesCount ?: 0),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = { showEdit = true }) { Text(stringResource(id = R.string.profile_menu_edit_profile)) }
                                OutlinedButton(onClick = { navController.navigate(MainAppScreen.MyPastes.route) }) { Text(stringResource(id = R.string.profile_menu_my_pastes)) }
                            }
                        }
                    }
                }
            }

            // Кнопка Logout всегда внизу
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Divider()
                MenuItem(
                    label = stringResource(id = R.string.profile_logout),
                    onClick = { if (logoutState !is AuthResult.Loading) viewModel.logout() },
                    trailing = {
                        if (logoutState is AuthResult.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    },
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showEdit && profileState is MyProfileUiState.Data) {
        EditProfileDialog(
            initialUsername = profileState.profile.user.username,
            initialDisplayName = profileState.profile.user.displayName ?: "",
            onDismiss = { showEdit = false },
            onSave = { u, d ->
                viewModel.updateProfile(u, d)
                showEdit = false
            }
        )
    }
}

@Composable
private fun MenuItem(
    label: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = tint, style = MaterialTheme.typography.bodyLarge)
        trailing?.invoke()
    }
    Divider()
}

@Composable
private fun EditProfileDialog(
    initialUsername: String,
    initialDisplayName: String,
    onDismiss: () -> Unit,
    onSave: (String?, String?) -> Unit
) {
    var username by remember { mutableStateOf(initialUsername) }
    var displayName by remember { mutableStateOf(initialDisplayName) }
    var error by remember { mutableStateOf<String?>(null) }

    // Получаем строки в @Composable контексте
    val errorInvalidUsername = stringResource(id = R.string.profile_edit_error_invalid_username)
    val errorDisplayNameBlank = stringResource(id = R.string.profile_edit_error_display_name_blank)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // Валидация
                if (username.isNotBlank() && !ValidationUtils.isValidUsername(username)) {
                    error = errorInvalidUsername
                    return@TextButton
                }
                if (displayName.isBlank()) {
                    error = errorDisplayNameBlank
                    return@TextButton
                }
                onSave(username.ifBlank { null }, displayName.ifBlank { null })
            }) { Text(stringResource(id = R.string.profile_edit_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.profile_edit_cancel)) } },
        title = { Text(stringResource(id = R.string.profile_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; error = null },
                    label = { Text(stringResource(id = R.string.profile_edit_username_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it; error = null },
                    label = { Text(stringResource(id = R.string.profile_edit_display_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    )
}
