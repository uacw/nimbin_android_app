package tech.nimbus.nimbin.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Объявляем DataStore на уровне модуля (top-level)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Repository for managing user-specific preferences using Jetpack DataStore.
 * This class handles operations like saving and retrieving an authentication token.
 * @param context The application context, used to get the DataStore instance.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferenceKeys {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    /**
     * Flow emitting the authentication token. Emits null if no token is stored.
     */
    val authToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferenceKeys.AUTH_TOKEN]
        }

    /**
     * Saves the authentication token to DataStore.
     * @param token The authentication token to save.
     */
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.AUTH_TOKEN] = token
        }
    }

    /**
     * Clears the authentication token from DataStore.
     */
    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferenceKeys.AUTH_TOKEN)
        }
    }
}
