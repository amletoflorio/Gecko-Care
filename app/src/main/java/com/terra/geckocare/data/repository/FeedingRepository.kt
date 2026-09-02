package com.terra.geckocare.data.repository

import com.terra.geckocare.data.local.dao.FeedingDao
import com.terra.geckocare.data.local.entity.Feeding
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class FeedingRepository(
    private val feedingDao: FeedingDao,
) {
    fun feedingsForGecko(geckoId: Long): Flow<List<Feeding>> = feedingDao.feedingsForGecko(geckoId)

    suspend fun getById(id: Long): Feeding? = feedingDao.getById(id)

    fun latestForGecko(geckoId: Long): Flow<Feeding?> = feedingDao.latestForGecko(geckoId)

    suspend fun latestForGeckoOnce(geckoId: Long): Feeding? = feedingDao.latestForGeckoOnce(geckoId)

    fun feedingsInRange(geckoId: Long, start: Instant, end: Instant): Flow<List<Feeding>> =
        feedingDao.feedingsInRange(geckoId, start, end)

    suspend fun feedingsInRangeOnce(geckoId: Long, start: Instant, end: Instant): List<Feeding> =
        feedingDao.feedingsInRangeOnce(geckoId, start, end)

    suspend fun insert(feeding: Feeding): Long = feedingDao.insert(feeding)

    suspend fun update(feeding: Feeding) = feedingDao.update(feeding)

    suspend fun delete(feeding: Feeding) = feedingDao.delete(feeding)
}
