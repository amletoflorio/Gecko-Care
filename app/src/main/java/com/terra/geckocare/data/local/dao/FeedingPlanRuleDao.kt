package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.terra.geckocare.data.local.entity.FeedingPlanRule
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedingPlanRuleDao {

    /** Regole di default valide per tutti i gechi (geckoId nullo). */
    @Query("SELECT * FROM feeding_plan_rule WHERE geckoId IS NULL ORDER BY etaMinMesi ASC")
    fun defaultRules(): Flow<List<FeedingPlanRule>>

    /** Solo gli override specifici di un geco. */
    @Query("SELECT * FROM feeding_plan_rule WHERE geckoId = :geckoId ORDER BY etaMinMesi ASC")
    fun rulesForGecko(geckoId: Long): Flow<List<FeedingPlanRule>>

    /** Regole applicabili a un geco: default + eventuali override, entrambe le fonti. */
    @Query("SELECT * FROM feeding_plan_rule WHERE geckoId IS NULL OR geckoId = :geckoId ORDER BY etaMinMesi ASC")
    fun rulesForGeckoOrDefault(geckoId: Long): Flow<List<FeedingPlanRule>>

    @Query("SELECT COUNT(*) FROM feeding_plan_rule WHERE geckoId IS NULL")
    suspend fun countDefaultRules(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(rule: FeedingPlanRule): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(rules: List<FeedingPlanRule>)

    @Update
    suspend fun update(rule: FeedingPlanRule)

    @Delete
    suspend fun delete(rule: FeedingPlanRule)

    @Query("DELETE FROM feeding_plan_rule WHERE id = :id")
    suspend fun deleteById(id: Long)
}
