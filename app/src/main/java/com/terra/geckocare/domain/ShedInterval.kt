package com.terra.geckocare.domain

import com.terra.geckocare.data.local.entity.ShedLog
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Intervallo medio (in giorni) tra mute complete consecutive. Null se non ci sono abbastanza dati. */
fun intervalloMedioMuta(muteComplete: List<ShedLog>): Int? {
    if (muteComplete.size < 2) return null
    val ordinate = muteComplete.sortedBy { it.dataOra }
    val intervalli = ordinate.zipWithNext { a, b ->
        ChronoUnit.DAYS.between(
            a.dataOra.atZone(ZoneId.systemDefault()).toLocalDate(),
            b.dataOra.atZone(ZoneId.systemDefault()).toLocalDate(),
        )
    }
    return intervalli.average().toInt()
}

/** Intervallo medio "tipico" di fallback quando non ci sono ancora dati sufficienti (media di specie). */
const val INTERVALLO_MUTA_DEFAULT_GIORNI = 18
