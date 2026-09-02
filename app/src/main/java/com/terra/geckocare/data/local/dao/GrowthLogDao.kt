package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.terra.geckocare.data.local.entity.GrowthLog
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthLogDao {

    @Query("SELECT * FROM growth_log WHERE geckoId = :geckoId ORDER BY dataOra ASC")
    fun logsForGecko(geckoId: Long): Flow<List<GrowthLog>>

    @Query("SELECT * FROM growth_log WHERE id = :id")
    suspend fun getById(id: Long): GrowthLog?

    @Query("SELECT * FROM growth_log WHERE geckoId = :geckoId AND pesoGrammi IS NOT NULL ORDER BY dataOra DESC LIMIT 1")
    fun latestWeightForGecko(geckoId: Long): Flow<GrowthLog?>

    @Query("SELECT * FROM growth_log WHERE geckoId = :geckoId AND lunghezzaCm IS NOT NULL ORDER BY dataOra DESC LIMIT 1")
    fun latestLengthForGecko(geckoId: Long): Flow<GrowthLog?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(log: GrowthLog): Long

    @Update
    suspend fun update(log: GrowthLog)

    @Delete
    suspend fun delete(log: GrowthLog)
}
