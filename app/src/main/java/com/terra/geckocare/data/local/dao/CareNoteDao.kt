package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.terra.geckocare.data.local.entity.CareNote
import kotlinx.coroutines.flow.Flow

@Dao
interface CareNoteDao {

    @Query("SELECT * FROM care_note WHERE geckoId = :geckoId ORDER BY dataOra DESC")
    fun notesForGecko(geckoId: Long): Flow<List<CareNote>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(note: CareNote): Long

    @Update
    suspend fun update(note: CareNote)

    @Delete
    suspend fun delete(note: CareNote)
}
