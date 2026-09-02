package com.terra.geckocare.data.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "terra_settings")

const val DATASTORE_FILE_NAME = "terra_settings.preferences_pb"

data class BackupSettings(
    val folderUri: String? = null,
    val enabled: Boolean = false,
    val frequencyDays: Int = 1,
    val lastRunTimestamp: Long? = null,
    val lastRunOk: Boolean? = null,
)

class SettingsRepository(
    private val context: Context,
) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val languageKey = stringPreferencesKey("app_language")
    private val currencyKey = stringPreferencesKey("app_currency")
    private val backupFolderUriKey = stringPreferencesKey("backup_folder_uri")
    private val backupEnabledKey = booleanPreferencesKey("backup_enabled")
    private val backupFrequencyDaysKey = intPreferencesKey("backup_frequency_days")
    private val backupLastRunTimestampKey = longPreferencesKey("backup_last_run_timestamp")
    private val backupLastRunOkKey = booleanPreferencesKey("backup_last_run_ok")

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        prefs[themeModeKey]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SISTEMA
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs -> prefs[themeModeKey] = mode.name }
    }

    val appLanguage: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        prefs[languageKey]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() } ?: AppLanguage.SISTEMA
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        val locales = if (language.tag != null) {
            LocaleListCompat.forLanguageTags(language.tag)
        } else {
            LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(locales)
        context.dataStore.edit { prefs -> prefs[languageKey] = language.name }
    }

    val appCurrency: Flow<AppCurrency> = context.dataStore.data.map { prefs ->
        prefs[currencyKey]?.let { runCatching { AppCurrency.valueOf(it) }.getOrNull() } ?: AppCurrency.AUTOMATICA
    }

    suspend fun setAppCurrency(currency: AppCurrency) {
        context.dataStore.edit { prefs -> prefs[currencyKey] = currency.name }
    }

    val backupSettings: Flow<BackupSettings> = context.dataStore.data.map { prefs ->
        BackupSettings(
            folderUri = prefs[backupFolderUriKey],
            enabled = prefs[backupEnabledKey] ?: false,
            frequencyDays = prefs[backupFrequencyDaysKey] ?: 1,
            lastRunTimestamp = prefs[backupLastRunTimestampKey],
            lastRunOk = prefs[backupLastRunOkKey],
        )
    }

    suspend fun setBackupFolderUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri == null) prefs.remove(backupFolderUriKey) else prefs[backupFolderUriKey] = uri
        }
    }

    suspend fun setBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[backupEnabledKey] = enabled }
    }

    suspend fun setBackupFrequencyDays(days: Int) {
        context.dataStore.edit { prefs -> prefs[backupFrequencyDaysKey] = days }
    }

    suspend fun recordBackupRun(ok: Boolean, timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            prefs[backupLastRunTimestampKey] = timestamp
            prefs[backupLastRunOkKey] = ok
        }
    }
}
