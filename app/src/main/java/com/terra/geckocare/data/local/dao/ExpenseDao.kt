package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.terra.geckocare.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expense WHERE geckoId = :geckoId ORDER BY data DESC")
    fun expensesForGecko(geckoId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    @Query("SELECT * FROM expense ORDER BY data DESC")
    fun expensesAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE data BETWEEN :start AND :end ORDER BY data DESC")
    fun expensesInRange(start: LocalDate, end: LocalDate): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE geckoId = :geckoId AND data BETWEEN :start AND :end ORDER BY data DESC")
    fun expensesForGeckoInRange(geckoId: Long, start: LocalDate, end: LocalDate): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(importo), 0.0) FROM expense WHERE data BETWEEN :start AND :end")
    fun totaleAllevamentoInRange(start: LocalDate, end: LocalDate): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}
