package com.terra.geckocare

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import com.terra.geckocare.data.backup.BackupManager
import com.terra.geckocare.di.AppContainer
import com.terra.geckocare.worker.REMINDER_NOTIFICATION_CHANNEL_ID
import com.terra.geckocare.worker.TerraWorkerFactory

class TerraApplication : Application(), Configuration.Provider {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        BackupManager.applyStagedRestoreIfPresent(this)
        container = AppContainer(this)
        creaCanaleNotifiche()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(TerraWorkerFactory(container))
            .build()

    private fun creaCanaleNotifiche() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_NOTIFICATION_CHANNEL_ID,
                getString(R.string.notif_canale_nome),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = getString(R.string.notif_canale_descrizione)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
