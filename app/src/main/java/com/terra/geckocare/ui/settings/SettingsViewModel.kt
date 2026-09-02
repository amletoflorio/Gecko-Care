package com.terra.geckocare.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.backup.BackupManager
import com.terra.geckocare.data.settings.AppCurrency
import com.terra.geckocare.data.settings.AppLanguage
import com.terra.geckocare.data.settings.BackupSettings
import com.terra.geckocare.data.settings.SettingsRepository
import com.terra.geckocare.data.settings.ThemeMode
import com.terra.geckocare.worker.BackupScheduler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class BackupEvent {
    data object BackupOk : BackupEvent()
    data object BackupErrore : BackupEvent()
    data object RestorePronto : BackupEvent()
    data object RestoreErrore : BackupEvent()
}

class SettingsViewModel(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
    private val backupScheduler: BackupScheduler,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SISTEMA)

    val appLanguage: StateFlow<AppLanguage> = settingsRepository.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLanguage.SISTEMA)

    val appCurrency: StateFlow<AppCurrency> = settingsRepository.appCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppCurrency.AUTOMATICA)

    val backupSettings: StateFlow<BackupSettings> = settingsRepository.backupSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BackupSettings())

    private val _eventi = MutableSharedFlow<BackupEvent>()
    val eventi: SharedFlow<BackupEvent> = _eventi

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsRepository.setAppLanguage(language) }
    }

    fun setAppCurrency(currency: AppCurrency) {
        viewModelScope.launch { settingsRepository.setAppCurrency(currency) }
    }

    fun onCartellaBackupScelta(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
        viewModelScope.launch { settingsRepository.setBackupFolderUri(uri.toString()) }
    }

    fun setBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBackupEnabled(enabled)
            if (enabled) {
                backupScheduler.schedule(backupSettings.value.frequencyDays)
            } else {
                backupScheduler.cancel()
            }
        }
    }

    fun setBackupFrequencyDays(giorni: Int) {
        viewModelScope.launch {
            settingsRepository.setBackupFrequencyDays(giorni)
            if (backupSettings.value.enabled) backupScheduler.schedule(giorni)
        }
    }

    fun backupOra() {
        viewModelScope.launch {
            val uriTesto = backupSettings.value.folderUri ?: return@launch
            val esito = backupManager.backup(context, Uri.parse(uriTesto))
            settingsRepository.recordBackupRun(esito.isSuccess)
            _eventi.emit(if (esito.isSuccess) BackupEvent.BackupOk else BackupEvent.BackupErrore)
        }
    }

    fun ripristinaDaFile(uri: Uri) {
        viewModelScope.launch {
            val esito = BackupManager.stageRestore(context, uri)
            _eventi.emit(if (esito.isSuccess) BackupEvent.RestorePronto else BackupEvent.RestoreErrore)
        }
    }
}
