@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.terra.geckocare.ui.spese

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.outlined.Close
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terra.geckocare.R
import com.terra.geckocare.di.terraViewModel
import com.terra.geckocare.data.local.entity.CategoriaSpesa
import com.terra.geckocare.ui.common.ChipVariant
import com.terra.geckocare.ui.common.DataPickerDialog
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun SpeseScreen(
    selectionViewModel: GeckoSelectionViewModel,
    onAggiungiSpesa: () -> Unit,
    onAggiungiSpesaGenerica: () -> Unit,
    onModificaSpesa: (Long) -> Unit,
    viewModel: SpeseViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val selectedId by selectionViewModel.selectedGeckoId.collectAsState()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(selectedId) { selectedId?.let(viewModel::selezionaGecko) }

    var panelIndex by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 24.dp),
    ) {
        EyebrowText(stringResource(R.string.spese_budget))
        PageTitle(stringResource(R.string.spese_title))

        SegmentedControl(
            options = listOf(state.nomeGecko.ifBlank { stringResource(R.string.spese_tab_animale_default) }, stringResource(R.string.spese_tab_allevamento)),
            selectedIndex = panelIndex,
            onSelected = { panelIndex = it },
            modifier = Modifier.padding(top = 4.dp),
        )

        SelettoreMese(
            mese = state.mese,
            onPrecedente = viewModel::mesePrecedente,
            onSuccessivo = viewModel::meseSuccessivo,
            successivoAbilitato = state.mese < YearMonth.now(),
        )

        val meseLabel = meseLabel(state.mese)
        if (panelIndex == 0) {
            TotaleCard(titolo = stringResource(R.string.spese_totale_del_mese, meseLabel), valore = state.totaleGeckoMese)
            SectionTitle(stringResource(R.string.spese_per_categoria))
            if (state.breakdownCategoria.isEmpty()) {
                TerraCard { Text(stringResource(R.string.spese_vuoto_mese), color = colors.inkFaint, fontSize = 12.5.sp) }
            } else {
                TerraCard {
                    Column {
                        state.breakdownCategoria.forEachIndexed { index, item ->
                            if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                            CategoriaRow(item, onModificaSpesa = onModificaSpesa)
                        }
                    }
                }
                SectionTitle(stringResource(R.string.spese_acquisti, state.acquistiGecko.size))
                ElencoAcquisti(state.acquistiGecko, mostraAnimale = false, onModificaSpesa = onModificaSpesa)
            }
            Spacer(Modifier.height(18.dp))
            GhostButton(text = stringResource(R.string.spese_aggiungi), onClick = onAggiungiSpesa)
        } else {
            TotaleCard(
                titolo = stringResource(R.string.spese_totale_allevamento_del_mese, meseLabel),
                valore = state.totaleAllevamentoMese,
                sottotitolo = state.totaleGenericheMese.takeIf { it > 0 }?.let {
                    stringResource(R.string.spese_di_cui_generiche, com.terra.geckocare.domain.formatImporto(it))
                },
            )
            SectionTitle(stringResource(R.string.spese_per_animale))
            if (state.breakdownAnimale.isEmpty()) {
                TerraCard { Text(stringResource(R.string.spese_vuoto_mese), color = colors.inkFaint, fontSize = 12.5.sp) }
            } else {
                TerraCard {
                    Column {
                        state.breakdownAnimale.forEachIndexed { index, item ->
                            if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                            AnimaleRow(item, onModificaSpesa = onModificaSpesa)
                        }
                    }
                }
                SectionTitle(stringResource(R.string.spese_acquisti, state.acquistiAllevamento.size))
                ElencoAcquisti(state.acquistiAllevamento, mostraAnimale = true, onModificaSpesa = onModificaSpesa)
            }
            Spacer(Modifier.height(18.dp))
            GhostButton(text = stringResource(R.string.spese_aggiungi_generica), onClick = onAggiungiSpesaGenerica)
        }
    }
}

@Composable
private fun meseLabel(mese: YearMonth): String {
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    return mese.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)).replaceFirstChar { it.titlecase(locale) }
}

@Composable
private fun SelettoreMese(mese: YearMonth, onPrecedente: () -> Unit, onSuccessivo: () -> Unit, successivoAbilitato: Boolean) {
    val colors = LocalTerraColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrecedente) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = stringResource(R.string.spese_mese_precedente), tint = colors.ink)
        }
        Text(meseLabel(mese), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        IconButton(onClick = onSuccessivo, enabled = successivoAbilitato) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = stringResource(R.string.spese_mese_successivo),
                tint = if (successivoAbilitato) colors.ink else colors.line,
            )
        }
    }
}

/** Elenco delle spese del mese, dalla più recente: una riga per acquisto con le sue voci puntate. */
@Composable
private fun ElencoAcquisti(acquisti: List<AcquistoUi>, mostraAnimale: Boolean, onModificaSpesa: (Long) -> Unit) {
    val colors = LocalTerraColors.current
    val generica = stringResource(R.string.spese_generica)
    TerraCard {
        Column {
            acquisti.forEachIndexed { index, acquisto ->
                if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                val puntino = acquisto.coloreGecko?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() } ?: colors.inkFaint
                val singola = acquisto.voci.singleOrNull()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onModificaSpesa(acquisto.primoId) }
                        .padding(vertical = 11.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(7.dp).background(if (mostraAnimale) puntino else colors.teal, CircleShape))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (singola != null) {
                                    "${categoriaEmoji(singola.categoria)} " + singola.note.ifBlank { categoriaLabel(singola.categoria) }
                                } else {
                                    stringResource(R.string.spese_acquisto_voci, acquisto.voci.size)
                                },
                                color = colors.ink,
                                fontSize = 13.5.sp,
                                fontWeight = if (singola == null) FontWeight.Medium else FontWeight.Normal,
                            )
                            val dettagli = buildList {
                                add(acquisto.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                                if (singola != null && singola.note.isNotBlank()) add(categoriaLabel(singola.categoria))
                                if (mostraAnimale) add(acquisto.nomeGecko ?: generica)
                            }
                            Text(dettagli.joinToString(" · "), color = colors.inkFaint, fontFamily = JetBrainsMono, fontSize = 10.5.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                        Text(com.terra.geckocare.domain.formatImporto(acquisto.totale), color = colors.ink, fontFamily = JetBrainsMono, fontSize = 13.sp)
                    }
                    if (singola == null) {
                        Column(
                            modifier = Modifier.padding(start = 17.dp, top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            acquisto.voci.forEach { voce ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        "• ${categoriaEmoji(voce.categoria)} " + voce.note.ifBlank { categoriaLabel(voce.categoria) },
                                        color = colors.inkDim,
                                        fontSize = 12.5.sp,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(com.terra.geckocare.domain.formatImporto(voce.importo), color = colors.inkDim, fontFamily = JetBrainsMono, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TotaleCard(titolo: String, valore: Double, sottotitolo: String? = null) {
    val colors = LocalTerraColors.current
    TerraCard {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titolo, color = colors.inkDim, fontSize = 12.sp)
            Text(
                com.terra.geckocare.domain.formatImporto(valore),
                color = colors.teal,
                fontFamily = JetBrainsMono,
                fontSize = 30.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
            sottotitolo?.let { Text(it, color = colors.inkFaint, fontSize = 11.5.sp, modifier = Modifier.padding(top = 4.dp)) }
        }
    }
}

@Composable
private fun CategoriaRow(item: CategoriaBreakdown, onModificaSpesa: (Long) -> Unit) {
    val colors = LocalTerraColors.current
    var espanso by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { espanso = !espanso }
            .padding(vertical = 12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(colors.surface2, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(categoriaEmoji(item.categoria), fontSize = 16.sp)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(categoriaLabel(item.categoria), color = colors.ink, fontSize = 14.sp)
                    Text(com.terra.geckocare.domain.formatImporto(item.importo), color = colors.ink, fontFamily = JetBrainsMono, fontSize = 13.sp)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(4.dp)
                        .background(colors.line, RoundedCornerShape(4.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(item.frazione.coerceIn(0f, 1f))
                            .height(4.dp)
                            .background(colors.teal, RoundedCornerShape(4.dp)),
                    )
                }
            }
        }
        if (espanso) {
            SpeseDelGruppo(item.spese, onModificaSpesa)
        }
    }
}

@Composable
private fun AnimaleRow(item: AnimaleBreakdown, onModificaSpesa: (Long) -> Unit) {
    val colors = LocalTerraColors.current
    val generica = item.geckoId == null
    val colore = if (generica) colors.inkDim else runCatching { Color(android.graphics.Color.parseColor(item.colore)) }.getOrDefault(colors.teal)
    var espanso by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { espanso = !espanso }
            .padding(vertical = 12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(colore.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (generica) Text("🏠", fontSize = 16.sp) else Box(modifier = Modifier.size(14.dp).background(colore, CircleShape))
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (generica) stringResource(R.string.spese_generiche) else item.nome, color = colors.ink, fontSize = 14.sp)
                    Text(com.terra.geckocare.domain.formatImporto(item.importo), color = colors.ink, fontFamily = JetBrainsMono, fontSize = 13.sp)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(4.dp)
                        .background(colors.line, RoundedCornerShape(4.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(item.frazione.coerceIn(0f, 1f))
                            .height(4.dp)
                            .background(colore, RoundedCornerShape(4.dp)),
                    )
                }
            }
        }
        if (espanso) {
            SpeseDelGruppo(item.spese, onModificaSpesa)
        }
    }
}

@Composable
private fun SpeseDelGruppo(spese: List<com.terra.geckocare.data.local.entity.Expense>, onModificaSpesa: (Long) -> Unit) {
    val colors = LocalTerraColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 48.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        spese.forEach { spesa ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onModificaSpesa(spesa.id) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(spesa.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), color = colors.inkFaint, fontFamily = JetBrainsMono, fontSize = 10.5.sp)
                    if (spesa.note.isNotBlank()) {
                        Text(spesa.note, color = colors.inkDim, fontSize = 11.5.sp, modifier = Modifier.padding(top = 1.dp))
                    }
                }
                Text(com.terra.geckocare.domain.formatImporto(spesa.importo), color = colors.inkDim, fontFamily = JetBrainsMono, fontSize = 12.sp)
            }
        }
    }
}

private fun categoriaEmoji(c: CategoriaSpesa): String = when (c) {
    CategoriaSpesa.INSETTI -> "🦗"
    CategoriaSpesa.INTEGRATORI -> "💊"
    CategoriaSpesa.SUBSTRATO_TERRARIO -> "🌱"
    CategoriaSpesa.VETERINARIO -> "🩺"
    CategoriaSpesa.ALTRO -> "📦"
}

@Composable
private fun categoriaLabel(c: CategoriaSpesa): String = when (c) {
    CategoriaSpesa.INSETTI -> stringResource(R.string.spese_cat_insetti)
    CategoriaSpesa.INTEGRATORI -> stringResource(R.string.spese_cat_integratori)
    CategoriaSpesa.SUBSTRATO_TERRARIO -> stringResource(R.string.spese_cat_substrato)
    CategoriaSpesa.VETERINARIO -> stringResource(R.string.spese_cat_veterinario)
    CategoriaSpesa.ALTRO -> stringResource(R.string.spese_cat_altro)
}

@Composable
fun AggiungiSpesaScreen(
    selectionViewModel: GeckoSelectionViewModel,
    onDone: () -> Unit,
    onClose: () -> Unit,
    expenseId: Long? = null,
    generica: Boolean = false,
    viewModel: AggiungiSpesaViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val state by viewModel.uiState.collectAsState()
    val geckoId by selectionViewModel.selectedGeckoId.collectAsState()
    val modalitaModifica = expenseId != null

    LaunchedEffect(expenseId) {
        if (expenseId != null) viewModel.caricaPerModifica(expenseId) else if (generica) viewModel.onGenericaChange(true)
    }
    var mostraPickerData by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    LaunchedEffect(state.salvataggioCompletato, state.eliminato) {
        if (state.salvataggioCompletato || state.eliminato) onDone()
    }

    Scaffold(
        containerColor = colors.night,
        topBar = {
            TopAppBar(
                title = { Text(if (modalitaModifica) stringResource(R.string.spesa_title_modifica) else stringResource(R.string.spesa_title_nuovo), color = colors.ink) },
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
            SectionTitle(stringResource(R.string.spesa_data))
            TerraCard(modifier = Modifier.clickable { mostraPickerData = true }) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        state.data.format(DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM).withLocale(androidx.compose.ui.platform.LocalConfiguration.current.locales[0])),
                        color = colors.ink,
                        fontSize = 14.sp,
                    )
                    Text(stringResource(R.string.spesa_data_cambia), color = colors.teal, fontSize = 12.sp)
                }
            }
            if (mostraPickerData) {
                DataPickerDialog(
                    dataIniziale = state.data,
                    onDismiss = { mostraPickerData = false },
                    onConfirm = { viewModel.onDataChange(it); mostraPickerData = false },
                )
            }

            SectionTitle(stringResource(R.string.spesa_ambito))
            TerraCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.spesa_generica_label), color = colors.ink, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.spesa_generica_hint), color = colors.inkFaint, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    androidx.compose.material3.Switch(
                        checked = state.genericaAllevamento,
                        onCheckedChange = viewModel::onGenericaChange,
                        colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = colors.teal, checkedThumbColor = colors.night),
                    )
                }
            }

            SectionTitle(stringResource(R.string.spesa_voci))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.voci.forEachIndexed { index, voce ->
                    VoceSpesaCard(
                        numero = index + 1,
                        voce = voce,
                        rimovibile = state.voci.size > 1,
                        onDescrizioneChange = { viewModel.onDescrizioneChange(index, it) },
                        onImportoChange = { viewModel.onImportoChange(index, it) },
                        onCategoriaChange = { viewModel.onCategoriaChange(index, it) },
                        onRimuovi = { viewModel.rimuoviVoce(index) },
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            GhostButton(text = stringResource(R.string.spesa_aggiungi_voce), onClick = viewModel::aggiungiVoce)
            if (state.voci.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.spesa_totale_voci, state.voci.count { it.importo != null }), color = colors.inkDim, fontSize = 13.sp)
                    Text(com.terra.geckocare.domain.formatImporto(state.totale), color = colors.teal, fontFamily = JetBrainsMono, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = if (modalitaModifica) stringResource(R.string.spesa_salva_modifiche) else stringResource(R.string.spesa_salva),
                enabled = state.valida,
                onClick = { viewModel.salva(geckoId) },
            )

            if (modalitaModifica) {
                Spacer(Modifier.height(10.dp))
                androidx.compose.material3.TextButton(onClick = viewModel::elimina, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.spesa_elimina), color = colors.danger)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** Riga del ripetitore: descrizione, importo e categoria di un singolo articolo. */
@Composable
private fun VoceSpesaCard(
    numero: Int,
    voce: VoceSpesaForm,
    rimovibile: Boolean,
    onDescrizioneChange: (String) -> Unit,
    onImportoChange: (String) -> Unit,
    onCategoriaChange: (CategoriaSpesa) -> Unit,
    onRimuovi: () -> Unit,
) {
    val colors = LocalTerraColors.current
    val campoColori = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line)
    var menuCategoria by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    TerraCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.spesa_voce_n, numero), color = colors.teal, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Box {
                TerraChip(
                    text = "${categoriaEmoji(voce.categoria)} ${categoriaLabel(voce.categoria)} ▾",
                    variant = ChipVariant.TEAL,
                    onClick = { menuCategoria = true },
                )
                androidx.compose.material3.DropdownMenu(expanded = menuCategoria, onDismissRequest = { menuCategoria = false }) {
                    CategoriaSpesa.entries.forEach { cat ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("${categoriaEmoji(cat)} ${categoriaLabel(cat)}") },
                            onClick = { onCategoriaChange(cat); menuCategoria = false },
                        )
                    }
                }
            }
            if (rimovibile) {
                IconButton(onClick = onRimuovi, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.spesa_rimuovi_voce_desc), tint = colors.inkFaint, modifier = Modifier.size(16.dp))
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = voce.descrizione,
                onValueChange = onDescrizioneChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.spesa_voce_descrizione_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences, imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                colors = campoColori,
            )
            OutlinedTextField(
                value = voce.importoTesto,
                onValueChange = onImportoChange,
                modifier = Modifier.width(110.dp),
                placeholder = { Text(stringResource(R.string.spesa_importo_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                colors = campoColori,
            )
        }
    }
}
