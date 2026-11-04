package com.terra.geckocare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Una voce di spesa. [geckoId] nullo = spesa generica dell'allevamento (es. substrato in blocco).
 * Le voci con lo stesso [gruppo] appartengono allo stesso acquisto (stessa data e ambito) e
 * vengono mostrate/modificate insieme; [gruppo] nullo = spesa a voce singola (dati pre-v7).
 */
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
    indices = [Index("geckoId"), Index("gruppo")],
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val geckoId: Long? = null,
    val categoria: CategoriaSpesa,
    val importo: Double,
    val data: LocalDate = LocalDate.now(),
    val note: String = "",
    val gruppo: String? = null,
) {
    /** Chiave di raggruppamento: le voci senza gruppo fanno acquisto a sé. */
    val chiaveGruppo: String get() = gruppo ?: "id:$id"
}
