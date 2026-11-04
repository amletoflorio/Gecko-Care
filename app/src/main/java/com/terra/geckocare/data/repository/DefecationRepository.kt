package com.terra.geckocare.data.repository

import com.terra.geckocare.data.local.dao.DefecationLogDao
import com.terra.geckocare.data.local.entity.DefecationLog
import kotlinx.coroutines.flow.Flow

class DefecationRepository(
    private val defecationLogDao: DefecationLogDao,
) {
    fun logsForGecko(geckoId: Long): Flow<List<DefecationLog>> = defecationLogDao.logsForGecko(geckoId)

    suspend fun getById(id: Long): DefecationLog? = defecationLogDao.getById(id)

    suspend fun insert(log: DefecationLog): Long = defecationLogDao.insert(log)

    suspend fun update(log: DefecationLog) = defecationLogDao.update(log)

    suspend fun delete(log: DefecationLog) = defecationLogDao.delete(log)
}
