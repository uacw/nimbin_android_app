package tech.nimbus.nimbin.ui.features.auth.register

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import tech.nimbus.nimbin.R
import tech.nimbus.nimbin.domain.repository.AuthResult

/**
 * Composable function for the Register Screen.
 * Handles user input for email, password, and confirm password, interacts with [RegisterViewModel],
 * and displays UI feedback based on registration state.
 * @param navController The [NavController] for navigating back to login.
 * @param viewModel The [RegisterViewModel] instance provided by Hilt.
 */
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.registerState

    LaunchedEffect(state) {
        when (state) {
            is AuthResult.Success -> {
                Toast.makeText(context, context.getString(R.string.register_successful_toast), Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is AuthResult.Error -> {
                Toast.makeText(context, context.getString(R.string.register_failed_toast_with_reason, state.message), Toast.LENGTH_LONG).show()
            }
            is AuthResult.Loading, null -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.register_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = viewModel.username,
                onValueChange = viewModel::onUsernameChange,
                label = { Text(stringResource(id = R.string.register_username_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text(stringResource(id = R.string.register_email_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(stringResource(id = R.string.register_password_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text(stringResource(id = R.string.register_confirm_password_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = viewModel::register,
                enabled = state !is AuthResult.Loading,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (state is AuthResult.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.register_button_text),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = { navController.popBackStack() },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(id = R.string.register_go_to_login_text),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}