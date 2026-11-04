package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.terra.geckocare.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
abstract class ExpenseDao {

    @Query("SELECT * FROM expense WHERE geckoId = :geckoId ORDER BY data DESC")
    abstract fun expensesForGecko(geckoId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE id = :id")
    abstract suspend fun getById(id: Long): Expense?

    @Query("SELECT * FROM expense WHERE gruppo = :gruppo ORDER BY id ASC")
    abstract suspend fun getByGruppo(gruppo: String): List<Expense>

    @Query("SELECT * FROM expense ORDER BY data DESC")
    abstract fun expensesAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE data BETWEEN :start AND :end ORDER BY data DESC")
    abstract fun expensesInRange(start: LocalDate, end: LocalDate): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE geckoId = :geckoId AND data BETWEEN :start AND :end ORDER BY data DESC")
    abstract fun expensesForGeckoInRange(geckoId: Long, start: LocalDate, end: LocalDate): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(importo), 0.0) FROM expense WHERE data BETWEEN :start AND :end")
    abstract fun totaleAllevamentoInRange(start: LocalDate, end: LocalDate): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(expense: Expense): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertAll(expenses: List<Expense>)

    @Update
    abstract suspend fun update(expense: Expense)

    @Delete
    abstract suspend fun delete(expense: Expense)

    @Query("DELETE FROM expense WHERE id IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Long>)

    /** Sostituisce in blocco le voci di un acquisto: il form le modifica sempre tutte insieme. */
    @Transaction
    open suspend fun sostituisciVoci(idPrecedenti: List<Long>, nuove: List<Expense>) {
        if (idPrecedenti.isNotEmpty()) deleteByIds(idPrecedenti)
        insertAll(nuove.map { it.copy(id = 0) })
    }
}
