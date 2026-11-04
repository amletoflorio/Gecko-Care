package com.terra.geckocare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.terra.geckocare.data.local.entity.FeedingPlanStep
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FeedingPlanStepDao {

    @Query("SELECT * FROM feeding_plan_step ORDER BY ruleId, numeroPasto ASC")
    abstract fun allSteps(): Flow<List<FeedingPlanStep>>

    @Query("SELECT * FROM feeding_plan_step WHERE ruleId = :ruleId ORDER BY numeroPasto ASC")
    abstract suspend fun stepsForRuleOnce(ruleId: Long): List<FeedingPlanStep>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertAll(steps: List<FeedingPlanStep>)

    @Query("DELETE FROM feeding_plan_step WHERE ruleId = :ruleId")
    protected abstract suspend fun deleteForRule(ruleId: Long)

    /** Sostituisce l'intero schema di una fascia (le righe sono sempre editate in blocco dal form). */
    @Transaction
    open suspend fun replaceSteps(ruleId: Long, steps: List<FeedingPlanStep>) {
        deleteForRule(ruleId)
        insertAll(steps.map { it.copy(id = 0, ruleId = ruleId) })
    }
}
