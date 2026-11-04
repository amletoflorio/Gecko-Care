@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.terra.geckocare.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terra.geckocare.R
import com.terra.geckocare.di.terraViewModel
import com.terra.geckocare.data.local.entity.TipoReminder
import com.terra.geckocare.ui.common.ChipVariant
import com.terra.geckocare.ui.common.DataOraField
import com.terra.geckocare.ui.common.PrimaryButton
import com.terra.geckocare.ui.common.SectionTitle
import com.terra.geckocare.ui.common.TerraCard
import com.terra.geckocare.ui.common.TerraChip
import com.terra.geckocare.ui.common.TimePickerDialogSimple
import com.terra.geckocare.ui.navigation.GeckoSelectionViewModel
import com.terra.geckocare.ui.theme.LocalTerraColors

@Composable
fun AggiungiNotaScreen(
    selectionViewModel: GeckoSelectionViewModel,
    onDone: () -> Unit,
    onClose: () -> Unit,
    viewModel: CareNoteFormViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val testo by viewModel.testo.collectAsState()
    val completato by viewModel.salvataggioCompletato.collectAsState()
    val geckoId by selectionViewModel.selectedGeckoId.collectAsState()

    LaunchedEffect(completato) { if (completato) onDone() }

    Scaffold(
        containerColor = colors.night,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nota_title), color = colors.ink) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.common_chiudi), tint = colors.ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.night),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            SectionTitle(stringResource(R.string.nota_section))
            OutlinedTextField(
                value = testo,
                onValueChange = viewModel::onTestoChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text(stringResource(R.string.nota_placeholder)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.teal,
                    unfocusedBorderColor = colors.line,
                ),
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = stringResource(R.string.nota_salva),
                enabled = testo.isNotBlank() && geckoId != null,
                onClick = { geckoId?.let(viewModel::salva) },
            )
        }
    }
}

@Composable
fun AmbienteFormScreen(
    selectionViewModel: GeckoSelectionViewModel,
    onDone: () -> Unit,
    onClose: () -> Unit,
    envLogId: Long? = null,
    viewModel: AmbienteFormViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val state by viewModel.uiState.collectAsState()
    val geckoId by selectionViewModel.selectedGeckoId.collectAsState()
    val modalitaModifica = envLogId != null

    LaunchedEffect(envLogId) { envLogId?.let(viewModel::caricaPerModifica) }
    LaunchedEffect(state.salvataggioCompletato, state.eliminato) {
        if (state.salvataggioCompletato || state.eliminato) onDone()
    }

    Scaffold(
        containerColor = colors.night,
        topBar = {
            TopAppBar(
                title = { Text(if (modalitaModifica) stringResource(R.string.ambiente_title_modifica) else stringResource(R.string.ambiente_title_nuovo), color = colors.ink) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.common_chiudi), tint = colors.ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.night),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(
                stringResource(R.string.ambiente_hint),
                color = colors.inkFaint,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            SectionTitle(stringResource(R.string.ambiente_hotspot))
            OutlinedTextField(
                value = state.hotSpotTesto,
                onValueChange = viewModel::onHotSpotChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("32") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            SectionTitle(stringResource(R.string.ambiente_coldspot))
            OutlinedTextField(
                value = state.coldSpotTesto,
                onValueChange = viewModel::onColdSpotChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("24") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            SectionTitle(stringResource(R.string.ambiente_umidita))
            OutlinedTextField(
                value = state.umiditaTesto,
                onValueChange = viewModel::onUmiditaChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("45") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            SectionTitle(stringResource(R.string.ambiente_notturna))
            OutlinedTextField(
                value = state.notturnaTesto,
                onValueChange = viewModel::onNotturnaChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("20") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = if (modalitaModifica) stringResource(R.string.ambiente_salva_modifiche) else stringResource(R.string.ambiente_salva),
                enabled = state.puoSalvare && geckoId != null,
                onClick = { geckoId?.let(viewModel::salva) },
            )

            if (modalitaModifica) {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = viewModel::elimina, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.ambiente_elimina), color = colors.danger)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun CrescitaFormScreen(
    selectionViewModel: GeckoSelectionViewModel,
    onDone: () -> Unit,
    onClose: () -> Unit,
    growthLogId: Long? = null,
    viewModel: CrescitaFormViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val state by viewModel.uiState.collectAsState()
    val geckoId by selectionViewModel.selectedGeckoId.collectAsState()
    val modalitaModifica = growthLogId != null

    LaunchedEffect(growthLogId) { growthLogId?.let(viewModel::caricaPerModifica) }
    LaunchedEffect(state.salvataggioCompletato, state.eliminato) {
        if (state.salvataggioCompletato || state.eliminato) onDone()
    }

    Scaffold(
        containerColor = colors.night,
        topBar = {
            TopAppBar(
                title = { Text(if (modalitaModifica) stringResource(R.string.crescita_title_modifica) else stringResource(R.string.crescita_title_nuovo), color = colors.ink) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.common_chiudi), tint = colors.ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.night),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            SectionTitle(stringResource(R.string.crescita_peso))
            OutlinedTextField(
                value = state.pesoTesto,
                onValueChange = viewModel::onPesoChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("45.5") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            SectionTitle(stringResource(R.string.crescita_lunghezza))
            OutlinedTextField(
                value = state.lunghezzaTesto,
                onValueChange = viewModel::onLunghezzaChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("21") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = if (modalitaModifica) stringResource(R.string.crescita_salva_modifiche) else stringResource(R.string.crescita_salva),
                enabled = state.puoSalvare && geckoId != null,
                onClick = { geckoId?.let(viewModel::salva) },
            )

            if (modalitaModifica) {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = viewModel::elimina, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.crescita_elimina), color = colors.danger)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun MutaFormScreen(
    selectionViewModel: GeckoSelectionViewModel,
    onDone: () -> Unit,
    onClose: () -> Unit,
    shedLogId: Long? = null,
    viewModel: MutaFormViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val state by viewModel.uiState.collectAsState()
    val geckoId by selectionViewModel.selectedGeckoId.collectAsState()
    val modalitaModifica = shedLogId != null

    LaunchedEffect(shedLogId) { shedLogId?.let(viewModel::caricaPerModifica) }
    LaunchedEffect(state.salvataggioCompletato, state.eliminato) {
        if (state.salvataggioCompletato || state.eliminato) onDone()
    }

    Scaffold(
        containerColor = colors.night,
        topBar = {
            TopAppBar(
                title = { Text(if (modalitaModifica) stringResource(R.string.muta_title_modifica) else stringResource(R.string.muta_title_nuovo), color = colors.ink) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.common_chiudi), tint = colors.ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.night),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            SectionTitle(stringResource(R.string.common_data_ora))
            DataOraField(dataOra = state.dataOra, onChange = viewModel::onDataOraChange)

            SectionTitle(stringResource(R.string.muta_esito))
            TerraCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.muta_completa_label), color = colors.ink, fontSize = 13.5.sp)
                        Text(stringResource(R.string.muta_completa_hint), color = colors.inkFaint, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    Switch(
                        checked = state.completa,
                        onCheckedChange = viewModel::onCompletaChange,
                        colors = SwitchDefaults.colors(checkedTrackColor = colors.teal, checkedThumbColor = colors.night),
                    )
                }
            }

            SectionTitle(stringResource(R.string.muta_note))
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text(stringResource(R.string.pasto_note_placeholder)) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = if (modalitaModifica) stringResource(R.string.muta_salva_modifiche) else stringResource(R.string.muta_salva),
                enabled = geckoId != null,
                onClick = { geckoId?.let(viewModel::salva) },
            )

            if (modalitaModifica) {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = viewModel::elimina, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.muta_elimina), color = colors.danger)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromemoriaFormScreen(
    reminderId: Long?,
    selectionViewModel: GeckoSelectionViewModel,
    onDone: () -> Unit,
    onClose: () -> Unit,
    viewModel: ReminderFormViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val state by viewModel.uiState.collectAsState()
    val geckoId by selectionViewModel.selectedGeckoId.collectAsState()

    LaunchedEffect(reminderId) { reminderId?.let(viewModel::caricaPerModifica) }
    LaunchedEffect(state.salvataggioCompletato, state.eliminato) {
        if (state.salvataggioCompletato || state.eliminato) onDone()
    }

    var mostraOrario by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.night,
        topBar = {
            TopAppBar(
                title = { Text(if (reminderId != null) stringResource(R.string.promemoria_title_modifica) else stringResource(R.string.promemoria_title_nuovo), color = colors.ink) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.common_chiudi), tint = colors.ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.night),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            SectionTitle(stringResource(R.string.promemoria_tipo))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(TipoReminder.entries.toList()) { tipo ->
                    TerraChip(
                        text = reminderTipoLabel(tipo),
                        variant = if (tipo == state.tipo) ChipVariant.TEAL else ChipVariant.NEUTRAL,
                        onClick = { viewModel.onTipoChange(tipo) },
                    )
                }
            }

            SectionTitle(stringResource(R.string.promemoria_frequenza_label))
            OutlinedTextField(
                value = state.frequenzaTesto,
                onValueChange = viewModel::onFrequenzaTestoChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.promemoria_frequenza_placeholder)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            SectionTitle(stringResource(R.string.promemoria_ripeti))
            TerraCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.onIntervalloChange(state.intervalloGiorni - 1) }) {
                        Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.pasto_diminuisci_desc), tint = colors.ink)
                    }
                    Text(stringResource(R.string.promemoria_giorni_suffix, state.intervalloGiorni), color = colors.ink, fontSize = 15.sp)
                    IconButton(onClick = { viewModel.onIntervalloChange(state.intervalloGiorni + 1) }) {
                        Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.pasto_aumenta_desc), tint = colors.ink)
                    }
                }
            }

            SectionTitle(stringResource(R.string.promemoria_orario))
            TerraCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostraOrario = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.promemoria_notifica_alle), color = colors.inkDim, fontSize = 13.sp)
                    Text(formatOrarioMinuti(state.orarioMinuti), color = colors.teal, fontSize = 15.sp)
                }
            }

            SectionTitle(stringResource(R.string.promemoria_attivo))
            TerraCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.promemoria_attivo_desc), color = colors.ink, fontSize = 13.5.sp)
                    Switch(
                        checked = state.attivo,
                        onCheckedChange = viewModel::onAttivoChange,
                        colors = SwitchDefaults.colors(checkedTrackColor = colors.teal, checkedThumbColor = colors.night),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton(text = stringResource(R.string.promemoria_salva), onClick = { viewModel.salva(geckoId) })

            if (reminderId != null) {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = viewModel::elimina, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.promemoria_elimina), color = colors.danger)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (mostraOrario) {
        TimePickerDialogSimple(
            orarioMinutiIniziale = state.orarioMinuti,
            onDismiss = { mostraOrario = false },
            onConfirm = { viewModel.onOrarioChange(it); mostraOrario = false },
        )
    }
}

private fun formatOrarioMinuti(minuti: Int): String {
    val h = (minuti / 60).toString().padStart(2, '0')
    val m = (minuti % 60).toString().padStart(2, '0')
    return "$h:$m"
}

@Composable
private fun reminderTipoLabel(tipo: TipoReminder): String = when (tipo) {
    TipoReminder.PASTO -> stringResource(R.string.promemoria_tipo_pasto)
    TipoReminder.PESO -> stringResource(R.string.promemoria_tipo_peso)
    TipoReminder.PULIZIA_TERRARIO -> stringResource(R.string.promemoria_tipo_pulizia)
    TipoReminder.INTEGRATORI -> stringResource(R.string.promemoria_tipo_integratori)
    TipoReminder.ALTRO -> stringResource(R.string.promemoria_tipo_altro)
}
