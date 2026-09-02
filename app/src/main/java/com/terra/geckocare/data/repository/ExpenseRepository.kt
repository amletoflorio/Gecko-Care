package com.terra.geckocare.data.repository

import com.terra.geckocare.data.local.dao.ExpenseDao
import com.terra.geckocare.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
) {
    fun expensesForGecko(geckoId: Long): Flow<List<Expense>> = expenseDao.expensesForGecko(geckoId)

    suspend fun getById(id: Long): Expense? = expenseDao.getById(id)

    fun expensesAll(): Flow<List<Expense>> = expenseDao.expensesAll()

    fun expensesInRange(start: LocalDate, end: LocalDate): Flow<List<Expense>> =
        expenseDao.expensesInRange(start, end)

    fun expensesForGeckoInRange(geckoId: Long, start: LocalDate, end: LocalDate): Flow<List<Expense>> =
        expenseDao.expensesForGeckoInRange(geckoId, start, end)

    fun totaleAllevamentoInRange(start: LocalDate, end: LocalDate): Flow<Double> =
        expenseDao.totaleAllevamentoInRange(start, end)

    suspend fun insert(expense: Expense): Long = expenseDao.insert(expense)

    suspend fun update(expense: Expense) = expenseDao.update(expense)

    suspend fun delete(expense: Expense) = expenseDao.delete(expense)
}
