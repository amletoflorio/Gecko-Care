package com.terra.geckocare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/** Foto del diario di crescita: più scatti nel tempo per animale, distinte dalla foto profilo singola in [Gecko.fotoProfiloPath]. */
@Entity(
    tableName = "gecko_photo",
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
data class GeckoPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val geckoId: Long,
    val path: String,
    val dataOra: Instant = Instant.now(),
)
