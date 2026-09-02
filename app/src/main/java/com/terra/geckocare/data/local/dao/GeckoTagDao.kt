package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.terra.geckocare.data.local.entity.GeckoTag
import kotlinx.coroutines.flow.Flow

@Dao
interface GeckoTagDao {

    @Query("SELECT * FROM gecko_tag WHERE geckoId = :geckoId ORDER BY etichetta COLLATE NOCASE ASC")
    fun tagsForGecko(geckoId: Long): Flow<List<GeckoTag>>

    @Query("SELECT * FROM gecko_tag")
    fun tagsAll(): Flow<List<GeckoTag>>

    @Query("SELECT DISTINCT etichetta FROM gecko_tag ORDER BY etichetta COLLATE NOCASE ASC")
    fun etichetteDistinte(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: GeckoTag): Long

    @Delete
    suspend fun delete(tag: GeckoTag)

    @Query("DELETE FROM gecko_tag WHERE geckoId = :geckoId AND etichetta = :etichetta")
    suspend fun deleteByLabel(geckoId: Long, etichetta: String)
}
