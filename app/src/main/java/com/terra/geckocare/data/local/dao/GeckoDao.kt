package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.terra.geckocare.data.local.entity.Gecko
import kotlinx.coroutines.flow.Flow

@Dao
interface GeckoDao {

    @Query("SELECT * FROM gecko ORDER BY nome COLLATE NOCASE ASC")
    fun getAll(): Flow<List<Gecko>>

    @Query("SELECT * FROM gecko WHERE stato = 'ATTIVO' ORDER BY nome COLLATE NOCASE ASC")
    fun getAllAttivi(): Flow<List<Gecko>>

    @Query("SELECT * FROM gecko WHERE id = :id")
    fun getById(id: Long): Flow<Gecko?>

    @Query("SELECT * FROM gecko WHERE id = :id")
    suspend fun getByIdOnce(id: Long): Gecko?

    @Query("SELECT COUNT(*) FROM gecko WHERE stato = 'ATTIVO'")
    fun countAttivi(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(gecko: Gecko): Long

    @Update
    suspend fun update(gecko: Gecko)

    @Delete
    suspend fun delete(gecko: Gecko)
}
