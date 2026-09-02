package com.terra.geckocare.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.terra.geckocare.data.local.dao.CareNoteDao
import com.terra.geckocare.data.local.dao.EnvironmentLogDao
import com.terra.geckocare.data.local.dao.ExpenseDao
import com.terra.geckocare.data.local.dao.FeedingDao
import com.terra.geckocare.data.local.dao.FeedingPlanRuleDao
import com.terra.geckocare.data.local.dao.GeckoDao
import com.terra.geckocare.data.local.dao.GeckoPhotoDao
import com.terra.geckocare.data.local.dao.GeckoTagDao
import com.terra.geckocare.data.local.dao.GrowthLogDao
import com.terra.geckocare.data.local.dao.ReminderDao
import com.terra.geckocare.data.local.dao.ShedLogDao
import com.terra.geckocare.data.local.entity.CareNote
import com.terra.geckocare.data.local.entity.EnvironmentLog
import com.terra.geckocare.data.local.entity.Expense
import com.terra.geckocare.data.local.entity.Feeding
import com.terra.geckocare.data.local.entity.FeedingPlanRule
import com.terra.geckocare.data.local.entity.Gecko
import com.terra.geckocare.data.local.entity.GeckoPhoto
import com.terra.geckocare.data.local.entity.GeckoTag
import com.terra.geckocare.data.local.entity.GrowthLog
import com.terra.geckocare.data.local.entity.Reminder
import com.terra.geckocare.data.local.entity.ShedLog

@Database(
    entities = [
        Gecko::class,
        GeckoTag::class,
        Feeding::class,
        GrowthLog::class,
        ShedLog::class,
        CareNote::class,
        Expense::class,
        FeedingPlanRule::class,
        Reminder::class,
        EnvironmentLog::class,
        GeckoPhoto::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class TerraDatabase : RoomDatabase() {
    abstract fun geckoDao(): GeckoDao
    abstract fun geckoPhotoDao(): GeckoPhotoDao
    abstract fun geckoTagDao(): GeckoTagDao
    abstract fun feedingDao(): FeedingDao
    abstract fun growthLogDao(): GrowthLogDao
    abstract fun shedLogDao(): ShedLogDao
    abstract fun careNoteDao(): CareNoteDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun feedingPlanRuleDao(): FeedingPlanRuleDao
    abstract fun reminderDao(): ReminderDao
    abstract fun environmentLogDao(): EnvironmentLogDao

    companion object {
        const val DATABASE_NAME = "terra.db"
    }
}
