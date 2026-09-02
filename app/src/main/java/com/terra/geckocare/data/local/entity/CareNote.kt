package com.terra.geckocare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "care_note",
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
data class CareNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val geckoId: Long,
    val dataOra: Instant = Instant.now(),
    val testo: String,
)
