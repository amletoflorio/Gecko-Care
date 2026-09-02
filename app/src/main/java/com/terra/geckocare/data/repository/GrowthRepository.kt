package com.terra.geckocare.data.repository

import com.terra.geckocare.data.local.dao.GrowthLogDao
import com.terra.geckocare.data.local.entity.GrowthLog
import kotlinx.coroutines.flow.Flow

class GrowthRepository(
    private val growthLogDao: GrowthLogDao,
) {
    fun logsForGecko(geckoId: Long): Flow<List<GrowthLog>> = growthLogDao.logsForGecko(geckoId)

    suspend fun getById(id: Long): GrowthLog? = growthLogDao.getById(id)

    fun latestWeightForGecko(geckoId: Long): Flow<GrowthLog?> = growthLogDao.latestWeightForGecko(geckoId)

    fun latestLengthForGecko(geckoId: Long): Flow<GrowthLog?> = growthLogDao.latestLengthForGecko(geckoId)

    suspend fun insert(log: GrowthLog): Long = growthLogDao.insert(log)

    suspend fun update(log: GrowthLog) = growthLogDao.update(log)

    suspend fun delete(log: GrowthLog) = growthLogDao.delete(log)
}
