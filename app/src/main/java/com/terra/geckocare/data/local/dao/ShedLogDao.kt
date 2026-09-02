package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.terra.geckocare.data.local.entity.ShedLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ShedLogDao {

    @Query("SELECT * FROM shed_log WHERE geckoId = :geckoId ORDER BY dataOra DESC")
    fun logsForGecko(geckoId: Long): Flow<List<ShedLog>>

    @Query("SELECT * FROM shed_log WHERE id = :id")
    suspend fun getById(id: Long): ShedLog?

    @Query("SELECT * FROM shed_log WHERE geckoId = :geckoId AND completa = 1 ORDER BY dataOra DESC LIMIT 1")
    fun latestCompleteForGecko(geckoId: Long): Flow<ShedLog?>

    @Query("SELECT * FROM shed_log WHERE geckoId = :geckoId AND completa = 1 ORDER BY dataOra ASC")
    suspend fun allCompleteOnceAsc(geckoId: Long): List<ShedLog>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(log: ShedLog): Long

    @Update
    suspend fun update(log: ShedLog)

    @Delete
    suspend fun delete(log: ShedLog)
}
