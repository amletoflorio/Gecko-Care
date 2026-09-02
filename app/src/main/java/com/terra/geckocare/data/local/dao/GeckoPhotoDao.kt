package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.terra.geckocare.data.local.entity.GeckoPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface GeckoPhotoDao {

    @Query("SELECT * FROM gecko_photo WHERE geckoId = :geckoId ORDER BY dataOra DESC")
    fun photosForGecko(geckoId: Long): Flow<List<GeckoPhoto>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(photo: GeckoPhoto): Long

    @Delete
    suspend fun delete(photo: GeckoPhoto)
}
