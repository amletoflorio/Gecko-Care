@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.terra.geckocare.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.terra.geckocare.R
import com.terra.geckocare.ui.theme.JetBrainsMono
import com.terra.geckocare.ui.theme.LocalTerraColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Card cliccabile che mostra data e ora di un evento (pasto, defecazione...) e le fa
 * modificare con un DatePicker seguito da un TimePicker. Le date future non sono selezionabili.
 */
@Composable
fun DataOraField(dataOra: Instant, onChange: (Instant) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalTerraColors.current
    val zona = ZoneId.systemDefault()
    val locale = LocalConfiguration.current.locales[0]
    val dataOraLocale = dataOra.atZone(zona).toLocalDateTime()

    var mostraData by remember { mutableStateOf(false) }
    var dataScelta by remember { mutableStateOf<LocalDate?>(null) }

    TerraCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { mostraData = true },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(dataOraLocale),
                color = colors.ink,
                fontSize = 14.sp,
            )
            Text(dataOraLocale.format(DateTimeFormatter.ofPattern("HH:mm")), color = colors.teal, fontFamily = JetBrainsMono, fontSize = 15.sp)
        }
    }

    if (mostraData) {
        val oggiUtcMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = dataOraLocale.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= oggiUtcMillis
                override fun isSelectableYear(year: Int): Boolean = year <= LocalDate.now().year
            },
        )
        DatePickerDialog(
            onDismissRequest = { mostraData = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        dataScelta = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    mostraData = false
                }) { Text(stringResource(R.string.common_conferma)) }
            },
            dismissButton = {
                TextButton(onClick = { mostraData = false }) { Text(stringResource(R.string.common_annulla)) }
            },
        ) {
            DatePicker(state = state)
        }
    }

    dataScelta?.let { data ->
        TimePickerDialogSimple(
            orarioMinutiIniziale = dataOraLocale.hour * 60 + dataOraLocale.minute,
            onDismiss = {
                // Data confermata ma orario annullato: si mantiene l'orario precedente.
                onChange(data.atTime(dataOraLocale.toLocalTime()).atZone(zona).toInstant())
                dataScelta = null
            },
            onConfirm = { minuti ->
                onChange(data.atTime(LocalTime.of(minuti / 60, minuti % 60)).atZone(zona).toInstant())
                dataScelta = null
            },
        )
    }
}

@Composable
fun TimePickerDialogSimple(orarioMinutiIniziale: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val state = rememberTimePickerState(
        initialHour = orarioMinutiIniziale / 60,
        initialMinute = orarioMinutiIniziale % 60,
        is24Hour = true,
    )
    Dialog(onDismissRequest = onDismiss) {
        TerraCard {
            TimePicker(state = state)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_annulla)) }
                TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) { Text(stringResource(R.string.common_conferma)) }
            }
        }
    }
}

/** DatePicker per una sola data (senza orario), es. la data di una spesa. Le date future non sono selezionabili. */
@Composable
fun DataPickerDialog(dataIniziale: LocalDate, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val oggiUtcMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = dataIniziale.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= oggiUtcMillis
            override fun isSelectableYear(year: Int): Boolean = year <= LocalDate.now().year
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onConfirm(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) } ?: onDismiss()
            }) { Text(stringResource(R.string.common_conferma)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_annulla)) }
        },
    ) {
        DatePicker(state = state)
    }
}
