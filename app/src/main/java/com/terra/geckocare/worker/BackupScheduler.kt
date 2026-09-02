package com.terra.geckocare.worker

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val NOME_UNIVOCO = "auto_backup"

/** Pianifica/cancella il backup automatico periodico: unico worker "auto_backup", sopravvive a riavvii tramite WorkManager. */
class BackupScheduler(private val workManager: WorkManager) {

    fun schedule(frequenzaGiorni: Int) {
        val request = PeriodicWorkRequestBuilder<BackupWorker>(frequenzaGiorni.toLong().coerceAtLeast(1), TimeUnit.DAYS).build()
        workManager.enqueueUniquePeriodicWork(NOME_UNIVOCO, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel() {
        workManager.cancelUniqueWork(NOME_UNIVOCO)
    }
}
