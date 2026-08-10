package com.mycodecalendar.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsPreferences(private val dataStore: DataStore<Preferences>) {
    companion object {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val isOnboardingComplete: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETE] ?: false
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETE] = complete
        }
    }
}
