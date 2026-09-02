@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.terra.geckocare.ui.pasti

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terra.geckocare.R
import com.terra.geckocare.di.terraViewModel
import com.terra.geckocare.data.local.entity.EsitoPasto
import com.terra.geckocare.ui.common.ChipVariant
import com.terra.geckocare.ui.common.GhostButton
import com.terra.geckocare.ui.common.PrimaryButton
import com.terra.geckocare.ui.common.SectionTitle
import com.terra.geckocare.ui.common.SegmentedControl
import com.terra.geckocare.ui.common.TerraCard
import com.terra.geckocare.ui.common.TerraChip
import com.terra.geckocare.ui.navigation.GeckoSelectionViewModel
import com.terra.geckocare.ui.theme.LocalTerraColors

@Composable
fun AggiungiPastoScreen(
    selectionViewModel: GeckoSelectionViewModel,
    onDone: () -> Unit,
    onClose: () -> Unit,
    feedingId: Long? = null,
    viewModel: AggiungiPastoViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val state by viewModel.uiState.collectAsState()
    val geckoId by selectionViewModel.selectedGeckoId.collectAsState()
    val modalitaModifica = feedingId != null

    LaunchedEffect(feedingId) { feedingId?.let(viewModel::caricaPerModifica) }
    LaunchedEffect(state.salvataggioCompletato, state.eliminato) {
        if (state.salvataggioCompletato || state.eliminato) onDone()
    }

    Scaffold(
        containerColor = colors.night,
        topBar = {
            TopAppBar(
                title = { Text(if (modalitaModifica) stringResource(R.string.pasto_title_modifica) else stringResource(R.string.pasto_title_nuovo), color = colors.ink) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            val prede = tipiPredaSuggeriti()
            state.prede.forEachIndexed { index, riga ->
                if (index > 0) Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    SectionTitle(if (state.prede.size > 1) stringResource(R.string.pasto_preda_n, index + 1) else stringResource(R.string.pasto_tipo_preda))
                    if (!modalitaModifica && state.prede.size > 1) {
                        IconButton(onClick = { viewModel.rimuoviPreda(index) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.pasto_rimuovi_preda_desc), tint = colors.inkFaint, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(prede) { tipo ->
                        TerraChip(
                            text = tipo,
                            variant = if (tipo.equals(riga.tipo, ignoreCase = true)) ChipVariant.TEAL else ChipVariant.NEUTRAL,
                            onClick = { viewModel.onTipoPredaChange(index, tipo) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = riga.tipo,
                    onValueChange = { viewModel.onTipoPredaChange(index, it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.pasto_preda_placeholder)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
                )

                SectionTitle(stringResource(R.string.pasto_quantita))
                TerraCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.onQuantitaChange(index, riga.quantita - 1) }) {
                            Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.pasto_diminuisci_desc), tint = colors.ink)
                        }
                        Text("${riga.quantita}", color = colors.ink, fontSize = 16.sp)
                        IconButton(onClick = { viewModel.onQuantitaChange(index, riga.quantita + 1) }) {
                            Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.pasto_aumenta_desc), tint = colors.ink)
                        }
                    }
                }
            }

            if (!modalitaModifica) {
                Spacer(Modifier.height(10.dp))
                GhostButton(text = stringResource(R.string.pasto_aggiungi_preda), onClick = viewModel::aggiungiPreda)
            }

            SectionTitle(stringResource(R.string.pasto_esito))
            SegmentedControl(
                options = listOf(stringResource(R.string.pasto_accettato), stringResource(R.string.pasto_rifiutato)),
                selectedIndex = if (state.esito == EsitoPasto.ACCETTATO) 0 else 1,
                onSelected = { viewModel.onEsitoChange(if (it == 0) EsitoPasto.ACCETTATO else EsitoPasto.RIFIUTATO) },
            )

            SectionTitle(stringResource(R.string.pasto_integratori))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TerraChip(
                    text = if (state.conCalcio) stringResource(R.string.pasto_calcio_on) else stringResource(R.string.pasto_calcio_off),
                    variant = if (state.conCalcio) ChipVariant.CLAY else ChipVariant.NEUTRAL,
                    onClick = { viewModel.onCalcioChange(!state.conCalcio) },
                )
                TerraChip(
                    text = if (state.conD3) stringResource(R.string.pasto_d3_on) else stringResource(R.string.pasto_d3_off),
                    variant = if (state.conD3) ChipVariant.SAGE else ChipVariant.NEUTRAL,
                    onClick = { viewModel.onD3Change(!state.conD3) },
                )
                TerraChip(
                    text = if (state.conMultivitaminico) stringResource(R.string.pasto_multivit_on) else stringResource(R.string.pasto_multivit_off),
                    variant = if (state.conMultivitaminico) ChipVariant.TEAL else ChipVariant.NEUTRAL,
                    onClick = { viewModel.onMultivitaminicoChange(!state.conMultivitaminico) },
                )
            }

            SectionTitle(stringResource(R.string.pasto_note))
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
                text = if (modalitaModifica) stringResource(R.string.pasto_salva_modifiche) else stringResource(R.string.pasto_salva),
                enabled = geckoId != null,
                onClick = { geckoId?.let(viewModel::salva) },
            )

            if (modalitaModifica) {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = viewModel::elimina, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.pasto_elimina), color = colors.danger)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun FasciaEtaFormScreen(
    ruleId: Long?,
    geckoId: Long?,
    onDone: () -> Unit,
    onClose: () -> Unit,
    viewModel: FasciaEtaFormViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(ruleId) { ruleId?.let { viewModel.caricaPerModifica(it, geckoId) } }
    LaunchedEffect(state.salvataggioCompletato, state.eliminato) {
        if (state.salvataggioCompletato || state.eliminato) onDone()
    }

    Scaffold(
        containerColor = colors.night,
        topBar = {
            TopAppBar(
                title = { Text(if (ruleId != null) stringResource(R.string.fascia_title_modifica) else stringResource(R.string.fascia_title_nuova), color = colors.ink) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            SectionTitle(stringResource(R.string.fascia_nome))
            OutlinedTextField(
                value = state.nomeFase,
                onValueChange = viewModel::onNomeFaseChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.fascia_nome_placeholder)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            SectionTitle(stringResource(R.string.fascia_eta))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.etaMinGiorni,
                    onValueChange = viewModel::onEtaMinChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.fascia_da)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
                )
                OutlinedTextField(
                    value = state.etaMaxGiorni,
                    onValueChange = viewModel::onEtaMaxChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.fascia_a)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
                )
            }

            SectionTitle(stringResource(R.string.fascia_frequenza))
            OutlinedTextField(
                value = state.frequenzaTesto,
                onValueChange = viewModel::onFrequenzaTestoChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.fascia_frequenza_placeholder)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            SectionTitle(stringResource(R.string.fascia_note_integratori))
            OutlinedTextField(
                value = state.noteIntegratori,
                onValueChange = viewModel::onNoteIntegratoriChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text(stringResource(R.string.fascia_note_integratori_placeholder)) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            if (geckoId != null) {
                SectionTitle(stringResource(R.string.fascia_ambito))
                TerraCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.fascia_solo_animale), color = colors.ink, fontSize = 13.5.sp)
                        androidx.compose.material3.Switch(
                            checked = state.soloQuestoAnimale,
                            onCheckedChange = viewModel::onSoloQuestoAnimaleChange,
                            colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = colors.teal, checkedThumbColor = colors.night),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton(text = stringResource(R.string.fascia_salva), onClick = { viewModel.salva(geckoId) })

            if (ruleId != null) {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = viewModel::elimina, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.fascia_elimina), color = colors.danger)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun tipiPredaSuggeriti(): List<String> = listOf(
    stringResource(R.string.preda_grilli),
    stringResource(R.string.preda_blatte),
    stringResource(R.string.preda_tarme),
    stringResource(R.string.preda_dubia),
    stringResource(R.string.preda_altro),
)
