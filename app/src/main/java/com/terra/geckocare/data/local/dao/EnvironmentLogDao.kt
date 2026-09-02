package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.terra.geckocare.data.local.entity.EnvironmentLog
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvironmentLogDao {

    @Query("SELECT * FROM environment_log WHERE geckoId = :geckoId ORDER BY dataOra DESC")
    fun logsForGecko(geckoId: Long): Flow<List<EnvironmentLog>>

    @Query("SELECT * FROM environment_log WHERE geckoId = :geckoId ORDER BY dataOra DESC LIMIT 1")
    fun latestForGecko(geckoId: Long): Flow<EnvironmentLog?>

    @Query("SELECT * FROM environment_log WHERE id = :id")
    suspend fun getById(id: Long): EnvironmentLog?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(log: EnvironmentLog): Long

    @Update
    suspend fun update(log: EnvironmentLog)

    @Delete
    suspend fun delete(log: EnvironmentLog)
}
