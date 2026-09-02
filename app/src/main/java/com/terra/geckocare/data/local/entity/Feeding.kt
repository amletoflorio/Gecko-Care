package com.terra.geckocare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "feeding",
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
data class Feeding(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val geckoId: Long,
    val dataOra: Instant = Instant.now(),
    val tipoPreda: String,
    val quantita: Int = 1,
    val esito: EsitoPasto = EsitoPasto.ACCETTATO,
    val conCalcio: Boolean = false,
    val conD3: Boolean = false,
    val conMultivitaminico: Boolean = false,
    val note: String = "",
)
