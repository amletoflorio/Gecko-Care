package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.terra.geckocare.data.local.entity.DefecationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DefecationLogDao {

    @Query("SELECT * FROM defecation_log WHERE geckoId = :geckoId ORDER BY dataOra DESC")
    fun logsForGecko(geckoId: Long): Flow<List<DefecationLog>>

    @Query("SELECT * FROM defecation_log WHERE id = :id")
    suspend fun getById(id: Long): DefecationLog?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(log: DefecationLog): Long

    @Update
    suspend fun update(log: DefecationLog)

    @Delete
    suspend fun delete(log: DefecationLog)
}
