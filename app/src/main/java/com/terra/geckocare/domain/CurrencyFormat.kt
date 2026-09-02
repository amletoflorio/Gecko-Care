package com.terra.geckocare.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Codice ISO valuta scelto dall'utente in Impostazioni; null = automatico dal paese del dispositivo. */
val LocalCurrencyCode = staticCompositionLocalOf<String?> { null }

/** Formatta un importo con la valuta scelta dall'utente, o quella del dispositivo se non impostata (no conversione tassi). */
@Composable
fun formatImporto(value: Double): String {
    val isoCode = LocalCurrencyCode.current
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    if (isoCode != null) {
        runCatching { format.currency = Currency.getInstance(isoCode) }
    }
    return format.format(value)
}
