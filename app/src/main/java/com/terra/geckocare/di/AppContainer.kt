package com.terra.geckocare.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.terra.geckocare.data.backup.BackupManager
import com.terra.geckocare.data.local.DefaultDataCallback
import com.terra.geckocare.data.local.TerraDatabase
import com.terra.geckocare.data.photo.PhotoStorageManager
import com.terra.geckocare.data.repository.CareNoteRepository
import com.terra.geckocare.data.repository.EnvironmentRepository
import com.terra.geckocare.data.repository.ExpenseRepository
import com.terra.geckocare.data.repository.FeedingPlanRepository
import com.terra.geckocare.data.repository.FeedingRepository
import com.terra.geckocare.data.repository.GeckoPhotoRepository
import com.terra.geckocare.data.repository.GeckoRepository
import com.terra.geckocare.data.repository.GrowthRepository
import com.terra.geckocare.data.repository.ReminderRepository
import com.terra.geckocare.data.repository.ShedRepository
import com.terra.geckocare.data.settings.SettingsRepository
import com.terra.geckocare.worker.BackupScheduler
import com.terra.geckocare.worker.ReminderScheduler

/**
 * Contenitore di dependency injection manuale: un'unica istanza per Application,
 * costruisce a mano il grafo delle dipendenze (niente Hilt/Dagger). Sostituisce
 * i moduli Hilt e viene esposto dalle Activity/Composable tramite [LocalAppContainer].
 */
class AppContainer(context: Context) {

    val appContext: Context = context.applicationContext

    val database: TerraDatabase = Room.databaseBuilder(appContext, TerraDatabase::class.java, TerraDatabase.DATABASE_NAME)
        .addCallback(DefaultDataCallback())
        .fallbackToDestructiveMigration()
        .build()

    val geckoRepository = GeckoRepository(database.geckoDao(), database.geckoTagDao())
    val feedingRepository = FeedingRepository(database.feedingDao())
    val growthRepository = GrowthRepository(database.growthLogDao())
    val shedRepository = ShedRepository(database.shedLogDao())
    val careNoteRepository = CareNoteRepository(database.careNoteDao())
    val expenseRepository = ExpenseRepository(database.expenseDao())
    val feedingPlanRepository = FeedingPlanRepository(database.feedingPlanRuleDao())
    val reminderRepository = ReminderRepository(database.reminderDao())
    val environmentRepository = EnvironmentRepository(database.environmentLogDao())

    val settingsRepository = SettingsRepository(appContext)
    val photoStorageManager = PhotoStorageManager(appContext)
    val geckoPhotoRepository = GeckoPhotoRepository(database.geckoPhotoDao(), photoStorageManager)

    val workManager: WorkManager by lazy { WorkManager.getInstance(appContext) }
    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(workManager) }

    val backupManager: BackupManager = BackupManager(database)
    val backupScheduler: BackupScheduler by lazy { BackupScheduler(workManager) }
}
