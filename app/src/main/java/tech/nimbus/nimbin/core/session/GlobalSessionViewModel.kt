package tech.nimbus.nimbin.core.session

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class GlobalSessionViewModel @Inject constructor(
    sessionManager: SessionManager
) : ViewModel() {
    val events: SharedFlow<SessionEvent> = sessionManager.events
}

