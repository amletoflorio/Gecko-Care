package com.terra.geckocare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/**
 * [dataNascita] e [dataAcquisizione] sono entrambe opzionali e indipendenti:
 * non tutti gli allevatori conoscono la data di nascita esatta, in quel caso
 * si usa la data di acquisizione ("gotcha day") come riferimento per l'età.
 * Se entrambe sono nulle l'età non viene mostrata.
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
)
