package com.terra.geckocare.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.terra.geckocare.data.local.entity.TipoReminder
import com.terra.geckocare.data.repository.GeckoRepository
import com.terra.geckocare.data.repository.ReminderRepository

const val REMINDER_NOTIFICATION_CHANNEL_ID = "terra_reminders"
const val KEY_REMINDER_ID = "reminder_id"

class ReminderNotificationWorker(
    context: Context,
    params: WorkerParameters,
    private val reminderRepository: ReminderRepository,
    private val geckoRepository: GeckoRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getLong(KEY_REMINDER_ID, -1L)
        if (reminderId == -1L) return Result.failure()

        val reminder = reminderRepository.getByIdOnce(reminderId) ?: return Result.success()
        if (!reminder.attivo) return Result.success()

        val nomeGecko = reminder.geckoId?.let { geckoRepository.getByIdOnce(it)?.nome }
        val titolo = tipoTitolo(reminder.tipo)
        val corpo = if (nomeGecko != null) "$titolo · $nomeGecko" else "$titolo · ${applicationContext.getString(com.terra.geckocare.R.string.notif_tutto_allevamento)}"

        postNotification(applicationContext, reminderId, titolo, corpo)
        return Result.success()
    }

    private fun postNotification(context: Context, reminderId: Long, titolo: String, corpo: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concesso = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!concesso) return
        }

        val notification = NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(titolo)
            .setContentText(corpo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(reminderId.toInt(), notification)
    }

    private fun tipoTitolo(tipo: TipoReminder): String = applicationContext.getString(
        when (tipo) {
            TipoReminder.PASTO -> com.terra.geckocare.R.string.notif_promemoria_pasto
            TipoReminder.PESO -> com.terra.geckocare.R.string.notif_promemoria_peso
            TipoReminder.PULIZIA_TERRARIO -> com.terra.geckocare.R.string.notif_promemoria_pulizia
            TipoReminder.INTEGRATORI -> com.terra.geckocare.R.string.notif_promemoria_integratori
            TipoReminder.ALTRO -> com.terra.geckocare.R.string.notif_promemoria_altro
        },
    )
}
