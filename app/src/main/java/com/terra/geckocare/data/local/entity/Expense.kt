package com.terra.geckocare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/** [geckoId] nullo = spesa generica dell'allevamento (es. substrato in blocco). */
@Entity(
    tableName = "expense",
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
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val geckoId: Long? = null,
    val categoria: CategoriaSpesa,
    val importo: Double,
    val data: LocalDate = LocalDate.now(),
    val note: String = "",
)
