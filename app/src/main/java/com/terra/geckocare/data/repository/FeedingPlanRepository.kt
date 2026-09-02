package com.terra.geckocare.data.repository

import com.terra.geckocare.data.local.dao.FeedingPlanRuleDao
import com.terra.geckocare.data.local.entity.FeedingPlanRule
import kotlinx.coroutines.flow.Flow

class FeedingPlanRepository(
    private val feedingPlanRuleDao: FeedingPlanRuleDao,
) {
    fun defaultRules(): Flow<List<FeedingPlanRule>> = feedingPlanRuleDao.defaultRules()

    fun rulesForGecko(geckoId: Long): Flow<List<FeedingPlanRule>> = feedingPlanRuleDao.rulesForGecko(geckoId)

    fun rulesForGeckoOrDefault(geckoId: Long): Flow<List<FeedingPlanRule>> =
        feedingPlanRuleDao.rulesForGeckoOrDefault(geckoId)

    suspend fun insert(rule: FeedingPlanRule): Long = feedingPlanRuleDao.insert(rule)

    suspend fun update(rule: FeedingPlanRule) = feedingPlanRuleDao.update(rule)

    suspend fun delete(rule: FeedingPlanRule) = feedingPlanRuleDao.delete(rule)

    suspend fun deleteById(id: Long) = feedingPlanRuleDao.deleteById(id)
}
