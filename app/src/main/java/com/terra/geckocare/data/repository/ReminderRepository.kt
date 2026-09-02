package com.terra.geckocare.data.repository

import com.terra.geckocare.data.local.dao.ReminderDao
import com.terra.geckocare.data.local.entity.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val reminderDao: ReminderDao,
) {
    fun remindersForGecko(geckoId: Long): Flow<List<Reminder>> = reminderDao.remindersForGecko(geckoId)

    fun remindersGenerici(): Flow<List<Reminder>> = reminderDao.remindersGenerici()

    suspend fun activeOnce(): List<Reminder> = reminderDao.activeOnce()

    suspend fun getByIdOnce(id: Long): Reminder? = reminderDao.getByIdOnce(id)

    suspend fun delete(id: Long) {
        reminderDao.getByIdOnce(id)?.let { reminderDao.delete(it) }
    }

    suspend fun insert(reminder: Reminder): Long = reminderDao.insert(reminder)

    suspend fun update(reminder: Reminder) = reminderDao.update(reminder)

    suspend fun delete(reminder: Reminder) = reminderDao.delete(reminder)
}
