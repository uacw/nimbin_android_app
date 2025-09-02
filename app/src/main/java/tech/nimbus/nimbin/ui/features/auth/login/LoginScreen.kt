package tech.nimbus.nimbin.ui.features.auth.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import tech.nimbus.nimbin.R // Убедитесь, что R импортирован
import tech.nimbus.nimbin.domain.repository.AuthResult
import tech.nimbus.nimbin.ui.navigation.AuthScreen
import tech.nimbus.nimbin.ui.navigation.Graph

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    // val loginState by viewModel.loginState.collectAsState() // Не используется напрямую в UI, а через LaunchedEffect

    LaunchedEffect(key1 = viewModel.loginState) {
        viewModel.loginState?.let { result ->
            when (result) {
                is AuthResult.Success -> {
                    Toast.makeText(context, R.string.login_successful, Toast.LENGTH_SHORT).show()
                    navController.navigate(Graph.MAIN_APP) {
                        popUpTo(Graph.AUTHENTICATION) { inclusive = true }
                    }
                }
                is AuthResult.Error -> {
                    val errorMessage = result.message ?: context.getString(R.string.login_error_occurred_default)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
                is AuthResult.Loading -> {
                    // Можно показать Toast или положиться на CircularProgressIndicator в UI
                    // Toast.makeText(context, R.string.login_in_progress, Toast.LENGTH_SHORT).show()
                }
            }
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
                text = stringResource(id = R.string.login_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text(stringResource(id = R.string.login_email_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text(stringResource(id = R.string.login_password_label)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.login() },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.loginState !is AuthResult.Loading,
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (viewModel.loginState is AuthResult.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.login_button_text),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { navController.navigate(AuthScreen.Register.route) },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(id = R.string.login_go_to_register_text),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    // Для превью нужно передать NavController и, возможно, фейковый ViewModel
    // или обернуть в тему Material
    MaterialTheme {
        LoginScreen(navController = rememberNavController())
    }
}
