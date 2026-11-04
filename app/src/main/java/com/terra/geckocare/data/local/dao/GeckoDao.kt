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

    @Query("SELECT * FROM gecko WHERE padreId = :id OR madreId = :id ORDER BY nome COLLATE NOCASE ASC")
    fun figli(id: Long): Flow<List<Gecko>>

    /** Scollega i figli da un genitore che sta per essere eliminato, conservandone nome e foto come genitore esterno. */
    @Query("UPDATE gecko SET padreId = NULL, padreEsterno = :nome, padreEsternoFotoPath = :foto WHERE padreId = :id")
    suspend fun scollegaPadre(id: Long, nome: String, foto: String?)

    @Query("UPDATE gecko SET madreId = NULL, madreEsterno = :nome, madreEsternoFotoPath = :foto WHERE madreId = :id")
    suspend fun scollegaMadre(id: Long, nome: String, foto: String?)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(gecko: Gecko): Long

    @Update
    suspend fun update(gecko: Gecko)

    @Delete
    suspend fun delete(gecko: Gecko)
}
