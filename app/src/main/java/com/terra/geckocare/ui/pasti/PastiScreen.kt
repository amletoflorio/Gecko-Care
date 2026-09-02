package com.terra.geckocare.ui.pasti

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terra.geckocare.R
import com.terra.geckocare.di.terraViewModel
import com.terra.geckocare.data.local.entity.EsitoPasto
import com.terra.geckocare.ui.common.ChipVariant
import com.terra.geckocare.ui.common.EyebrowText
import com.terra.geckocare.ui.common.GhostButton
import com.terra.geckocare.ui.common.PageTitle
import com.terra.geckocare.ui.common.PrimaryButton
import com.terra.geckocare.ui.common.SectionTitle
import com.terra.geckocare.ui.common.SegmentedControl
import com.terra.geckocare.ui.common.TerraCard
import com.terra.geckocare.ui.common.TerraChip
import com.terra.geckocare.ui.navigation.GeckoSelectionViewModel
import com.terra.geckocare.ui.theme.JetBrainsMono
import com.terra.geckocare.ui.theme.LocalTerraColors

@Composable
fun PastiScreen(
    selectionViewModel: GeckoSelectionViewModel,
    onAggiungiPasto: () -> Unit,
    onModificaPasto: (Long) -> Unit,
    onModificaFasciaEta: (Long) -> Unit,
    onNuovaFasciaEta: () -> Unit,
    viewModel: PastiViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val selectedId by selectionViewModel.selectedGeckoId.collectAsState()
    val state by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(selectedId) { selectedId?.let(viewModel::selezionaGecko) }

    var panelIndex by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 24.dp),
    ) {
        EyebrowText(stringResource(R.string.pasti_eyebrow, state.nomeGecko))
        PageTitle(stringResource(R.string.pasti_title))

        SegmentedControl(
            options = listOf(stringResource(R.string.pasti_tab_registro), stringResource(R.string.pasti_tab_piano)),
            selectedIndex = panelIndex,
            onSelected = { panelIndex = it },
            modifier = Modifier.padding(top = 4.dp),
        )

        Spacer(Modifier.height(16.dp))

        if (panelIndex == 0) {
            RegistroContent(state = state, viewModel = viewModel, onAggiungiPasto = onAggiungiPasto, onModificaPasto = onModificaPasto)
        } else {
            PianoContent(state = state, onModificaFasciaEta = onModificaFasciaEta, onNuovaFasciaEta = onNuovaFasciaEta)
        }
    }
}

@Composable
private fun RegistroContent(state: PastiUiState, viewModel: PastiViewModel, onAggiungiPasto: () -> Unit, onModificaPasto: (Long) -> Unit) {
    val colors = LocalTerraColors.current

    TerraCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(stringResource(R.string.pasti_settimana), color = colors.inkDim, fontSize = 13.sp)
                Text(stringResource(R.string.pasti_count, state.pastiSettimana), color = colors.teal, fontFamily = JetBrainsMono, fontSize = 22.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.pasti_calcio_d3), color = colors.inkDim, fontSize = 13.sp)
                Text(
                    "${state.calcioD3Rispettati}/${state.calcioD3Totali}",
                    color = colors.sage,
                    fontFamily = JetBrainsMono,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }

    SectionTitle(stringResource(R.string.pasti_cronologia))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        items(FiltroRegistro.entries.toList()) { f ->
            TerraChip(
                text = filtroLabel(f),
                variant = if (state.filtro == f) ChipVariant.TEAL else ChipVariant.NEUTRAL,
                onClick = { viewModel.impostaFiltro(f) },
            )
        }
    }

    Spacer(Modifier.height(10.dp))

    if (state.pastiFiltrati.isEmpty()) {
        TerraCard { Text(stringResource(R.string.pasti_registro_vuoto), color = colors.inkFaint, fontSize = 12.5.sp) }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.pastiFiltrati.forEach { pasto ->
                TerraCard(modifier = Modifier.clickable { onModificaPasto(pasto.id) }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column {
                            Text(pasto.dataLabel, color = colors.inkFaint, fontFamily = JetBrainsMono, fontSize = 11.sp)
                            Text(pasto.cibo, color = colors.ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                        TerraChip(
                            text = if (pasto.esito == EsitoPasto.ACCETTATO) stringResource(R.string.pasti_accettato) else stringResource(R.string.pasti_rifiutato),
                            variant = if (pasto.esito == EsitoPasto.ACCETTATO) ChipVariant.NEUTRAL else ChipVariant.DANGER,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    TerraChip(
                        text = when {
                            pasto.conCalcio && pasto.conD3 -> stringResource(R.string.pasti_calcio_d3_chip)
                            pasto.conCalcio -> stringResource(R.string.pasti_solo_calcio)
                            else -> stringResource(R.string.pasti_nessun_integratore)
                        },
                        variant = if (pasto.conCalcio && pasto.conD3) ChipVariant.SAGE else if (pasto.conCalcio) ChipVariant.CLAY else ChipVariant.NEUTRAL,
                        onClick = { viewModel.toggleCalcioD3Pasto(pasto.id) },
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(18.dp))
    PrimaryButton(text = stringResource(R.string.pasti_aggiungi), onClick = onAggiungiPasto)
}

@Composable
private fun PianoContent(state: PastiUiState, onModificaFasciaEta: (Long) -> Unit, onNuovaFasciaEta: () -> Unit) {
    val colors = LocalTerraColors.current

    TerraCard(backgroundColor = colors.tealDim) {
        Text(
            stringResource(R.string.pasti_piano_hint),
            color = colors.inkDim,
            fontSize = 12.5.sp,
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    SectionTitle(stringResource(R.string.pasti_fasce_eta))
    if (state.fasce.isEmpty()) {
        TerraCard { Text(stringResource(R.string.pasti_fasce_vuoto), color = colors.inkFaint, fontSize = 12.5.sp) }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.fasce.forEach { fascia ->
                TerraCard(backgroundColor = if (fascia.attuale) colors.tealDim else colors.surface, borderColor = if (fascia.attuale) androidx.compose.ui.graphics.Color.Transparent else colors.line) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column {
                            Text(
                                stringResource(R.string.pasti_range_eta, fascia.rule.etaMinGiorni, fascia.rule.etaMaxGiorni),
                                color = colors.inkFaint,
                                fontFamily = JetBrainsMono,
                                fontSize = 10.5.sp,
                            )
                            Text(fascia.rule.nomeFase, color = colors.ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.padding(top = 3.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (fascia.attuale) {
                                TerraChip(text = stringResource(R.string.pasti_attuale), variant = ChipVariant.TEAL)
                            }
                            androidx.compose.material3.IconButton(onClick = { onModificaFasciaEta(fascia.rule.id) }) {
                                androidx.compose.material3.Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = stringResource(R.string.pasti_modifica_fascia_desc),
                                    tint = colors.inkFaint,
                                )
                            }
                        }
                    }
                    Text(
                        "${fascia.rule.frequenzaTesto} · ${fascia.rule.noteIntegratori}",
                        color = colors.inkDim,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(18.dp))
    GhostButton(text = stringResource(R.string.pasti_nuova_fascia), onClick = onNuovaFasciaEta)
}

@Composable
private fun filtroLabel(f: FiltroRegistro): String = when (f) {
    FiltroRegistro.TUTTI -> stringResource(R.string.pasti_filtro_tutti)
    FiltroRegistro.ACCETTATI -> stringResource(R.string.pasti_filtro_accettati)
    FiltroRegistro.RIFIUTATI -> stringResource(R.string.pasti_filtro_rifiutati)
    FiltroRegistro.CON_D3 -> stringResource(R.string.pasti_filtro_con_d3)
}
