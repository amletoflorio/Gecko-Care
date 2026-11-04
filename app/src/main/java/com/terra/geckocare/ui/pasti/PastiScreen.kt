package com.terra.geckocare.ui.pasti

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.terra.geckocare.data.local.entity.ETA_MESI_SENZA_LIMITE
import com.terra.geckocare.data.local.entity.EsitoPasto
import com.terra.geckocare.data.local.entity.FeedingPlanRule
import com.terra.geckocare.data.local.entity.FeedingPlanStep
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
    onAggiungiFeci: () -> Unit,
    onModificaFeci: (Long) -> Unit,
    viewModel: PastiViewModel = terraViewModel(),
) {
    val selectedId by selectionViewModel.selectedGeckoId.collectAsState()
    val state by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(selectedId) { selectedId?.let(viewModel::selezionaGecko) }

    var panelIndex by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Spazio in fondo per non finire sotto il FAB.
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 56.dp, bottom = 96.dp),
    ) {
        item(key = "header", contentType = "header") {
            Column {
                EyebrowText(stringResource(R.string.pasti_eyebrow, state.nomeGecko))
                PageTitle(stringResource(R.string.pasti_title))

                SegmentedControl(
                    options = listOf(stringResource(R.string.pasti_tab_registro), stringResource(R.string.pasti_tab_feci), stringResource(R.string.pasti_tab_piano)),
                    selectedIndex = panelIndex,
                    onSelected = { panelIndex = it },
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(Modifier.height(16.dp))
            }
        }

        when (panelIndex) {
            0 -> registroContent(state = state, viewModel = viewModel, onAggiungiPasto = onAggiungiPasto, onModificaPasto = onModificaPasto)
            1 -> feciContent(state = state, viewModel = viewModel, onAggiungiFeci = onAggiungiFeci, onModificaFeci = onModificaFeci)
            else -> item(key = "piano", contentType = "piano") {
                Column { PianoContent(state = state, onModificaFasciaEta = onModificaFasciaEta, onNuovaFasciaEta = onNuovaFasciaEta) }
            }
        }
    }
}

private fun LazyListScope.registroContent(state: PastiUiState, viewModel: PastiViewModel, onAggiungiPasto: () -> Unit, onModificaPasto: (Long) -> Unit) {
    val pagina = state.pastiPagina

    item(key = "registro_header", contentType = "registro_header") {
        val colors = LocalTerraColors.current
        Column {
            TerraCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(stringResource(R.string.pasti_settimana), color = colors.inkDim, fontSize = 13.sp)
                        Text(stringResource(R.string.pasti_count, state.pastiSettimana), color = colors.teal, fontFamily = JetBrainsMono, fontSize = 22.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.pasti_calcio_d3), color = colors.inkDim, fontSize = 13.sp)
                        Text(
                            state.integratoriRispettati?.let { "$it/${state.integratoriTotali}" } ?: "—",
                            color = colors.sage,
                            fontFamily = JetBrainsMono,
                            fontSize = 22.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }

            // Bottone in alto: resta raggiungibile anche con molti pasti registrati.
            PrimaryButton(text = stringResource(R.string.pasti_aggiungi), onClick = onAggiungiPasto, modifier = Modifier.padding(top = 14.dp))

            SectionTitle(stringResource(R.string.pasti_prossimo_pasto))
            ProssimoPastoCard(state.prossimiIntegratori)

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
            SettimanaNavigator(
                pagina = pagina,
                conteggioLabel = stringResource(R.string.pasti_count, pagina.voci.size),
                onPrecedente = viewModel::settimanaPastiPrecedente,
                onSuccessiva = viewModel::settimanaPastiSuccessiva,
            )
            Spacer(Modifier.height(10.dp))
        }
    }

    if (pagina.voci.isEmpty()) {
        item(key = "registro_vuoto", contentType = "vuoto") {
            val testo = if (state.filtro == FiltroRegistro.TUTTI) R.string.pasti_settimana_vuota else R.string.pasti_registro_vuoto
            TerraCard { Text(stringResource(testo), color = LocalTerraColors.current.inkFaint, fontSize = 12.5.sp) }
        }
    } else {
        items(pagina.voci, key = { "pasto_${it.id}" }, contentType = { "pasto" }) { pasto ->
            val colors = LocalTerraColors.current
            TerraCard(modifier = Modifier.padding(bottom = 10.dp).clickable { onModificaPasto(pasto.id) }) {
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
                    text = integratoriLabel(pasto.conCalcio, pasto.conD3, pasto.conMultivitaminico)?.let { "● $it" }
                        ?: stringResource(R.string.pasti_nessun_integratore),
                    variant = integratoriVariant(pasto.conCalcio, pasto.conD3, pasto.conMultivitaminico),
                    onClick = { viewModel.toggleCalcioD3Pasto(pasto.id) },
                )
            }
        }
    }
}

@Composable
private fun ProssimoPastoCard(prossimi: com.terra.geckocare.domain.IntegratoriPrevisti?) {
    val colors = LocalTerraColors.current
    if (prossimi == null) {
        TerraCard { Text(stringResource(R.string.pasti_prossimo_senza_schema), color = colors.inkFaint, fontSize = 12.5.sp) }
        return
    }
    TerraCard(backgroundColor = colors.tealDim) {
        Text(
            stringResource(R.string.pasti_prossimo_numero, prossimi.numeroPasto, prossimi.lunghezzaCiclo),
            color = colors.inkFaint,
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
        )
        Spacer(Modifier.height(8.dp))
        PastoPrevistoChips(prossimi)
        if (prossimi.insetti.isEmpty()) {
            Text(stringResource(R.string.pasti_prossimo_insetti_vuoto), color = colors.inkFaint, fontSize = 11.5.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Text(stringResource(R.string.pasti_prossimo_hint), color = colors.inkDim, fontSize = 11.5.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

private fun LazyListScope.feciContent(state: PastiUiState, viewModel: PastiViewModel, onAggiungiFeci: () -> Unit, onModificaFeci: (Long) -> Unit) {
    val pagina = state.feciPagina

    item(key = "feci_header", contentType = "feci_header") {
        val colors = LocalTerraColors.current
        Column {
            TerraCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(stringResource(R.string.pasti_settimana), color = colors.inkDim, fontSize = 13.sp)
                        Text(stringResource(R.string.feci_count, state.feciSettimana), color = colors.teal, fontFamily = JetBrainsMono, fontSize = 22.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.feci_ultima), color = colors.inkDim, fontSize = 13.sp)
                        Text(state.feciUltimaLabel ?: "—", color = colors.sage, fontFamily = JetBrainsMono, fontSize = 15.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }

            // Bottone in alto: resta raggiungibile anche con molte registrazioni.
            PrimaryButton(text = stringResource(R.string.feci_aggiungi), onClick = onAggiungiFeci, modifier = Modifier.padding(top = 14.dp))

            SectionTitle(stringResource(R.string.pasti_cronologia))
            SettimanaNavigator(
                pagina = pagina,
                conteggioLabel = stringResource(R.string.feci_count, pagina.voci.size),
                onPrecedente = viewModel::settimanaFeciPrecedente,
                onSuccessiva = viewModel::settimanaFeciSuccessiva,
            )
            Spacer(Modifier.height(10.dp))
        }
    }

    if (pagina.voci.isEmpty()) {
        item(key = "feci_vuoto", contentType = "vuoto") {
            val testo = if (state.feciUltimaLabel == null) R.string.feci_registro_vuoto else R.string.feci_settimana_vuota
            TerraCard { Text(stringResource(testo), color = LocalTerraColors.current.inkFaint, fontSize = 12.5.sp) }
        }
    } else {
        items(pagina.voci, key = { "feci_${it.id}" }, contentType = { "feci" }) { voce ->
            val colors = LocalTerraColors.current
            TerraCard(modifier = Modifier.padding(bottom = 10.dp).clickable { onModificaFeci(voce.id) }) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(voce.dataLabel, color = colors.inkFaint, fontFamily = JetBrainsMono, fontSize = 11.sp)
                        if (voce.note.isNotBlank()) {
                            Text(voce.note, color = colors.ink, fontSize = 13.5.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                    TerraChip(
                        text = if (voce.normale) stringResource(R.string.feci_normale) else stringResource(R.string.feci_anomala),
                        variant = if (voce.normale) ChipVariant.SAGE else ChipVariant.DANGER,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettimanaNavigator(pagina: SettimanaUiModel<*>, conteggioLabel: String, onPrecedente: () -> Unit, onSuccessiva: () -> Unit) {
    val colors = LocalTerraColors.current
    TerraCard(padding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrecedente, enabled = pagina.puoAndareIndietro) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.settimana_precedente_desc),
                    tint = if (pagina.puoAndareIndietro) colors.ink else colors.line,
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    when (pagina.offset) {
                        0 -> stringResource(R.string.pasti_settimana)
                        1 -> stringResource(R.string.settimana_scorsa)
                        else -> stringResource(R.string.settimane_fa, pagina.offset)
                    },
                    color = colors.inkDim,
                    fontSize = 12.sp,
                )
                Text(
                    "${pagina.intervalloLabel} · $conteggioLabel",
                    color = colors.ink,
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            IconButton(onClick = onSuccessiva, enabled = pagina.offset > 0) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.settimana_successiva_desc),
                    tint = if (pagina.offset > 0) colors.ink else colors.line,
                )
            }
        }
    }
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
                                rangeEtaLabel(fascia.rule),
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
                        listOf(fascia.rule.frequenzaTesto, fascia.rule.noteIntegratori).filter { it.isNotBlank() }.joinToString(" · "),
                        color = colors.inkDim,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    if (fascia.schema.isNotEmpty()) SchemaIntegratoriTabella(fascia.schema)
                }
            }
        }
    }

    Spacer(Modifier.height(18.dp))
    GhostButton(text = stringResource(R.string.pasti_nuova_fascia), onClick = onNuovaFasciaEta)
}

@Composable
private fun SchemaIntegratoriTabella(schema: List<FeedingPlanStep>) {
    val colors = LocalTerraColors.current
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            Text(stringResource(R.string.piano_col_pasto), color = colors.inkFaint, fontSize = 11.sp, modifier = Modifier.width(64.dp))
            Text(stringResource(R.string.piano_col_cosa_dare), color = colors.inkFaint, fontSize = 11.sp)
        }
        schema.forEach { step ->
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
                Text("${step.numeroPasto}", color = colors.teal, fontFamily = JetBrainsMono, fontSize = 13.sp, modifier = Modifier.width(64.dp))
                Column {
                    if (step.listaInsetti().isNotEmpty()) {
                        Text(step.listaInsetti().joinToString(", "), color = colors.ink, fontSize = 13.sp)
                    }
                    Text(
                        integratoriLabel(step.conCalcio, step.conD3, step.conMultivitaminico) ?: stringResource(R.string.piano_nessuno),
                        color = if (step.listaInsetti().isNotEmpty()) colors.inkDim else colors.ink,
                        fontSize = if (step.listaInsetti().isNotEmpty()) 12.sp else 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun rangeEtaLabel(rule: FeedingPlanRule): String =
    if (rule.etaMaxMesi >= ETA_MESI_SENZA_LIMITE) {
        stringResource(R.string.pasti_range_eta_aperto, rule.etaMinMesi)
    } else {
        stringResource(R.string.pasti_range_eta, rule.etaMinMesi, rule.etaMaxMesi)
    }

@Composable
private fun filtroLabel(f: FiltroRegistro): String = when (f) {
    FiltroRegistro.TUTTI -> stringResource(R.string.pasti_filtro_tutti)
    FiltroRegistro.ACCETTATI -> stringResource(R.string.pasti_filtro_accettati)
    FiltroRegistro.RIFIUTATI -> stringResource(R.string.pasti_filtro_rifiutati)
    FiltroRegistro.CON_D3 -> stringResource(R.string.pasti_filtro_con_d3)
}
