package com.terra.geckocare.data.repository

import com.terra.geckocare.data.local.dao.ShedLogDao
import com.terra.geckocare.data.local.entity.ShedLog
import kotlinx.coroutines.flow.Flow

class ShedRepository(
    private val shedLogDao: ShedLogDao,
) {
    fun logsForGecko(geckoId: Long): Flow<List<ShedLog>> = shedLogDao.logsForGecko(geckoId)

    suspend fun getById(id: Long): ShedLog? = shedLogDao.getById(id)

    fun latestCompleteForGecko(geckoId: Long): Flow<ShedLog?> = shedLogDao.latestCompleteForGecko(geckoId)

    suspend fun allCompleteOnceAsc(geckoId: Long): List<ShedLog> = shedLogDao.allCompleteOnceAsc(geckoId)

    suspend fun insert(log: ShedLog): Long = shedLogDao.insert(log)

    suspend fun update(log: ShedLog) = shedLogDao.update(log)

    suspend fun delete(log: ShedLog) = shedLogDao.delete(log)
}
