package com.terra.geckocare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Fascia d'età del piano alimentare. [geckoId] nullo = regola di default
 * applicabile a tutti i gechi finché non personalizzata; [geckoId] valorizzato
 * = override specifico per quell'animale (le regole geco-specifiche hanno
 * sempre priorità su quelle globali per la stessa fascia d'età).
 * Età in mesi compiuti, estremi inclusi; [etaMaxMesi] >= [ETA_MESI_SENZA_LIMITE] = fascia aperta.
 */
@Entity(
    tableName = "feeding_plan_rule",
    foreignKeys = [
        ForeignKey(
            entity = Gecko::class,
            parentColumns = ["id"],
            childColumns = ["geckoId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("geckoId")],
)
data class FeedingPlanRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val geckoId: Long? = null,
    val etaMinMesi: Int,
    val etaMaxMesi: Int,
    val nomeFase: String,
    val frequenzaTesto: String,
    val noteIntegratori: String,
)

const val ETA_MESI_SENZA_LIMITE = 999
