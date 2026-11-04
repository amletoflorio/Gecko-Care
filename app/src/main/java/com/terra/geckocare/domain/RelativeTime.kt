package com.terra.geckocare.domain

import android.content.Context
import com.terra.geckocare.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val oraFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun localeCorrente(context: Context): Locale = context.resources.configuration.locales[0]

private fun giornoMeseAbbreviato(context: Context, data: LocalDate): String {
    val locale = localeCorrente(context)
    return DateTimeFormatter.ofPattern("d MMM", locale).format(data).uppercase(locale)
}

/** "20 OTT – 26 OTT" — intervallo lunedì-domenica della settimana che inizia in [inizio], nella lingua corrente. */
fun formatIntervalloSettimana(context: Context, inizio: LocalDate): String =
    "${giornoMeseAbbreviato(context, inizio)} – ${giornoMeseAbbreviato(context, inizio.plusDays(6))}"

/** "OGGI · 19:20", "IERI · 19:20", "21 AGO · 19:15" — per il feed attività (pasto/peso), nella lingua corrente. */
fun formatAttivitaTimestamp(context: Context, instant: Instant, oggi: LocalDate = LocalDate.now()): String {
    val dataOra: LocalDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
    val data = dataOra.toLocalDate()
    val ora = dataOra.format(oraFormatter)
    val giornoLabel = when {
        data == oggi -> context.getString(R.string.relative_oggi)
        data == oggi.minusDays(1) -> context.getString(R.string.relative_ieri)
        else -> giornoMeseAbbreviato(context, data)
    }
    return "$giornoLabel · $ora"
}

/** "oggi", "3 giorni fa", "21 ago" — per eventi in cui l'orario non è rilevante (mute, note), nella lingua corrente. */
fun formatGiorniFa(context: Context, instant: Instant, oggi: LocalDate = LocalDate.now()): String {
    val data = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    val giorni = java.time.temporal.ChronoUnit.DAYS.between(data, oggi)
    return when {
        giorni <= 0 -> context.getString(R.string.relative_oggi)
        giorni == 1L -> context.getString(R.string.relative_ieri)
        giorni < 7 -> context.resources.getQuantityString(R.plurals.giorni_fa, giorni.toInt(), giorni.toInt())
        else -> giornoMeseAbbreviato(context, data)
    }
}
