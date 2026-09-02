package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.terra.geckocare.data.local.entity.Feeding
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface FeedingDao {

    @Query("SELECT * FROM feeding WHERE geckoId = :geckoId ORDER BY dataOra DESC")
    fun feedingsForGecko(geckoId: Long): Flow<List<Feeding>>

    @Query("SELECT * FROM feeding WHERE id = :id")
    suspend fun getById(id: Long): Feeding?

    @Query("SELECT * FROM feeding WHERE geckoId = :geckoId ORDER BY dataOra DESC LIMIT 1")
    fun latestForGecko(geckoId: Long): Flow<Feeding?>

    @Query("SELECT * FROM feeding WHERE geckoId = :geckoId ORDER BY dataOra DESC LIMIT 1")
    suspend fun latestForGeckoOnce(geckoId: Long): Feeding?

    @Query("SELECT * FROM feeding WHERE geckoId = :geckoId AND dataOra BETWEEN :start AND :end ORDER BY dataOra DESC")
    fun feedingsInRange(geckoId: Long, start: Instant, end: Instant): Flow<List<Feeding>>

    @Query("SELECT * FROM feeding WHERE geckoId = :geckoId AND dataOra BETWEEN :start AND :end ORDER BY dataOra DESC")
    suspend fun feedingsInRangeOnce(geckoId: Long, start: Instant, end: Instant): List<Feeding>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(feeding: Feeding): Long

    @Update
    suspend fun update(feeding: Feeding)

    @Delete
    suspend fun delete(feeding: Feeding)
}
