package com.terra.geckocare.worker

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.terra.geckocare.data.backup.BackupManager
import com.terra.geckocare.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first

class BackupWorker(
    context: Context,
    params: WorkerParameters,
    private val backupManager: BackupManager,
    private val settingsRepository: SettingsRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = settingsRepository.backupSettings.first()
        val folderUri = settings.folderUri ?: return Result.failure()

        val esito = backupManager.backup(applicationContext, Uri.parse(folderUri))
        settingsRepository.recordBackupRun(esito.isSuccess)
        return if (esito.isSuccess) Result.success() else Result.retry()
    }
}
