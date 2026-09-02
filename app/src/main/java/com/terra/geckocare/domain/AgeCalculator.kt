package com.terra.geckocare.domain

import android.content.Context
import com.terra.geckocare.R
import com.terra.geckocare.data.local.entity.Gecko
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Età in giorni calcolata da [Gecko.dataNascita] se nota, altrimenti dalla
 * data di acquisizione ("gotcha day") come riferimento più affidabile
 * disponibile. Null se l'allevatore non ha registrato nessuna delle due.
 */
fun Gecko.etaInGiorni(oggi: LocalDate = LocalDate.now()): Long? {
    val riferimento = dataNascita ?: dataAcquisizione ?: return null
    return ChronoUnit.DAYS.between(riferimento, oggi).coerceAtLeast(0)
}

/** Etichetta leggibile ("62 giorni", "7 mesi", "2 anni 1 mese") nella lingua corrente. */
fun Gecko.etaLabel(context: Context, oggi: LocalDate = LocalDate.now()): String? =
    etaInGiorni(oggi)?.let { formatEtaGiorni(context, it) }

fun formatEtaGiorni(context: Context, giorni: Long): String {
    val res = context.resources
    return when {
        giorni < 60 -> res.getQuantityString(R.plurals.age_days, giorni.toInt(), giorni.toInt())
        giorni < 365 -> {
            val mesi = (giorni / 30).coerceAtLeast(1)
            res.getQuantityString(R.plurals.age_months, mesi.toInt(), mesi.toInt())
        }
        else -> {
            val anni = giorni / 365
            val mesiResto = (giorni % 365) / 30
            val etichettaAnni = res.getQuantityString(R.plurals.age_years, anni.toInt(), anni.toInt())
            if (mesiResto == 0L) {
                etichettaAnni
            } else {
                "$etichettaAnni ${res.getQuantityString(R.plurals.age_months, mesiResto.toInt(), mesiResto.toInt())}"
            }
        }
    }
}
