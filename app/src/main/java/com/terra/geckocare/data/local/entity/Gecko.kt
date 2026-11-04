package com.terra.geckocare.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/**
 * [dataNascita] e [dataAcquisizione] sono entrambe opzionali e indipendenti:
 * non tutti gli allevatori conoscono la data di nascita esatta, in quel caso
 * si usa la data di acquisizione ("gotcha day") come riferimento per l'età.
 * Se entrambe sono nulle l'età non viene mostrata.
 *
 * Genitori: [padreId]/[madreId] puntano a gechi dell'allevamento; per genitori
 * esterni (es. riproduttori dell'allevatore d'origine) si usa il nome libero
 * [padreEsterno]/[madreEsterno], con foto facoltativa in [padreEsternoFotoPath]/[madreEsternoFotoPath]
 * (i genitori dell'allevamento usano la propria foto profilo). Niente foreign key: alla cancellazione di un
 * genitore il repository azzera il riferimento conservandone il nome.
 */
@Entity(tableName = "gecko")
data class Gecko(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val morph: String = "",
    val sesso: Sesso = Sesso.SCONOSCIUTO,
    val dataNascita: LocalDate? = null,
    val dataAcquisizione: LocalDate? = null,
    val fotoProfiloPath: String? = null,
    val descrizione: String = "",
    val coloreTag: String = "#5B9C93",
    val stato: StatoGecko = StatoGecko.ATTIVO,
    val dataCreazione: Instant = Instant.now(),
    val padreId: Long? = null,
    val madreId: Long? = null,
    @ColumnInfo(defaultValue = "")
    val padreEsterno: String = "",
    @ColumnInfo(defaultValue = "")
    val madreEsterno: String = "",
    val padreEsternoFotoPath: String? = null,
    val madreEsternoFotoPath: String? = null,
)
