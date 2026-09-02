package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.terra.geckocare.data.local.entity.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminder WHERE geckoId = :geckoId ORDER BY orarioMinuti ASC")
    fun remindersForGecko(geckoId: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminder WHERE geckoId IS NULL ORDER BY orarioMinuti ASC")
    fun remindersGenerici(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminder WHERE attivo = 1")
    suspend fun activeOnce(): List<Reminder>

    @Query("SELECT * FROM reminder WHERE id = :id")
    suspend fun getByIdOnce(id: Long): Reminder?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)
}
