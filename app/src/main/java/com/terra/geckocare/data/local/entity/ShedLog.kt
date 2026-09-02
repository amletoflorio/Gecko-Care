package com.terra.geckocare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "shed_log",
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
data class ShedLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val geckoId: Long,
    val dataOra: Instant = Instant.now(),
    val completa: Boolean = true,
    val note: String = "",
)
