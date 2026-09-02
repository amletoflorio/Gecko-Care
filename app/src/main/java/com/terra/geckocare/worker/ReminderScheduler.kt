package com.terra.geckocare.worker

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.terra.geckocare.data.local.entity.Reminder
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Traduce ogni [Reminder] attivo in un PeriodicWorkRequest univoco (nome
 * "reminder_{id}"): niente stato "ultima esecuzione" da tracciare a mano,
 * WorkManager se ne occupa e sopravvive a riavvii/riavvii dell'app.
 */
class ReminderScheduler(
    private val workManager: WorkManager,
) {
    fun schedule(reminder: Reminder) {
        if (!reminder.attivo) {
            cancel(reminder.id)
            return
        }
        val ritardoIniziale = calcolaRitardoIniziale(reminder.orarioMinuti)
        val periodoGiorni = reminder.intervalloGiorni.coerceAtLeast(1).toLong()

        val request = PeriodicWorkRequestBuilder<ReminderNotificationWorker>(periodoGiorni, TimeUnit.DAYS)
            .setInitialDelay(ritardoIniziale)
            .setInputData(workDataOf(KEY_REMINDER_ID to reminder.id))
            .addTag(TAG_REMINDER)
            .build()

        workManager.enqueueUniquePeriodicWork(
            nomeUnivoco(reminder.id),
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(reminderId: Long) {
        workManager.cancelUniqueWork(nomeUnivoco(reminderId))
    }

    fun rischedulaTutti(reminders: List<Reminder>) {
        reminders.forEach(::schedule)
    }

    private fun nomeUnivoco(reminderId: Long) = "reminder_$reminderId"

    private fun calcolaRitardoIniziale(orarioMinuti: Int): Duration {
        val ora = LocalTime.of(orarioMinuti / 60, orarioMinuti % 60)
        val adesso = LocalDateTime.now()
        var prossimaEsecuzione = adesso.toLocalDate().atTime(ora)
        if (!prossimaEsecuzione.isAfter(adesso)) {
            prossimaEsecuzione = prossimaEsecuzione.plusDays(1)
        }
        return Duration.between(adesso, prossimaEsecuzione)
    }

    companion object {
        const val TAG_REMINDER = "terra_reminder"
    }
}
