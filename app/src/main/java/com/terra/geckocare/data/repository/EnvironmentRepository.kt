package com.terra.geckocare.data.repository

import com.terra.geckocare.data.local.dao.EnvironmentLogDao
import com.terra.geckocare.data.local.entity.EnvironmentLog
import kotlinx.coroutines.flow.Flow

class EnvironmentRepository(
    private val environmentLogDao: EnvironmentLogDao,
) {
    fun logsForGecko(geckoId: Long): Flow<List<EnvironmentLog>> = environmentLogDao.logsForGecko(geckoId)

    fun latestForGecko(geckoId: Long): Flow<EnvironmentLog?> = environmentLogDao.latestForGecko(geckoId)

    suspend fun getById(id: Long): EnvironmentLog? = environmentLogDao.getById(id)

    suspend fun insert(log: EnvironmentLog): Long = environmentLogDao.insert(log)

    suspend fun update(log: EnvironmentLog) = environmentLogDao.update(log)

    suspend fun delete(log: EnvironmentLog) = environmentLogDao.delete(log)
}
