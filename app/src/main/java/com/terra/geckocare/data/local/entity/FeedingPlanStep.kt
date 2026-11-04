package com.terra.geckocare.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Riga dello schema integratori di una fascia del piano: al pasto n° [numeroPasto]
 * del ciclo, gli insetti vanno spolverati con gli integratori indicati.
 * [insetti] = tipi di preda previsti per quel pasto, separati da virgola (vuoto = non specificato).
 */
@Entity(
    tableName = "feeding_plan_step",
    foreignKeys = [
        ForeignKey(
            entity = FeedingPlanRule::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("ruleId")],
)
data class FeedingPlanStep(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleId: Long,
    val numeroPasto: Int,
    val conCalcio: Boolean = false,
    val conD3: Boolean = false,
    val conMultivitaminico: Boolean = false,
    @ColumnInfo(defaultValue = "")
    val insetti: String = "",
) {
    fun listaInsetti(): List<String> = insetti.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}
