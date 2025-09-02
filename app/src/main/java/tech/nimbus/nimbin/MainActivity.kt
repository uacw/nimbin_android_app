package tech.nimbus.nimbin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Required for by viewModels()
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment // Required for Alignment.Center
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Required for SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import tech.nimbus.nimbin.ui.navigation.RootNavGraph
import tech.nimbus.nimbin.ui.theme.NimBinTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.uiState.isLoading
        }

        setContent {
            NimBinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!viewModel.uiState.isLoading) {
                        RootNavGraph(startDestination = viewModel.uiState.startDestination)
                    } else {
                        // Show a loading indicator while ViewModel determines the route
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
