package com.terra.geckocare.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.terra.geckocare.di.AppContainer

/** Sostituto manuale di HiltWorkerFactory: costruisce i Worker a mano da [AppContainer]. */
class TerraWorkerFactory(private val container: AppContainer) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        ReminderNotificationWorker::class.java.name -> ReminderNotificationWorker(
            appContext,
            workerParameters,
            container.reminderRepository,
            container.geckoRepository,
        )
        BackupWorker::class.java.name -> BackupWorker(
            appContext,
            workerParameters,
            container.backupManager,
            container.settingsRepository,
        )
        else -> null
    }
}
