package com.terra.geckocare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * [frequenzaTesto] è l'etichetta mostrata in UI (es. "ogni 14 giorni").
 * [intervalloGiorni] è la stessa frequenza in forma machine-readable, usata
 * da WorkManager per schedulare la notifica periodica.
 * [orarioMinuti] = minuti dalla mezzanotte (0-1439) a cui notificare.
 */
@Entity(
    tableName = "reminder",
    foreignKeys = [
        ForeignKey(
            entity = Gecko::class,
            parentColumns = ["id"],
            childColumns = ["geckoId"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
    indices = [Index("geckoId")],
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val geckoId: Long? = null,
    val tipo: TipoReminder,
    val frequenzaTesto: String,
    val intervalloGiorni: Int = 1,
    val orarioMinuti: Int = 19 * 60,
    val attivo: Boolean = true,
)
