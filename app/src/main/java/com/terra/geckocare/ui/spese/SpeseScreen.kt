@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.terra.geckocare.ui.spese

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import java.time.format.DateTimeFormatter

@Composable
fun SpeseScreen(
    selectionViewModel: GeckoSelectionViewModel,
    onAggiungiSpesa: () -> Unit,
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

        Spacer(Modifier.height(16.dp))

        if (panelIndex == 0) {
            TotaleCard(titolo = stringResource(R.string.spese_totale_mese), valore = state.totaleGeckoMese)
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
            }
            Spacer(Modifier.height(18.dp))
            GhostButton(text = stringResource(R.string.spese_aggiungi), onClick = onAggiungiSpesa)
        } else {
            TotaleCard(titolo = stringResource(R.string.spese_totale_allevamento_mese), valore = state.totaleAllevamentoMese)
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
            }
        }
    }
}

@Composable
private fun TotaleCard(titolo: String, valore: Double) {
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
    val colore = runCatching { Color(android.graphics.Color.parseColor(item.colore)) }.getOrDefault(colors.teal)
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
                Box(modifier = Modifier.size(14.dp).background(colore, CircleShape))
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.nome, color = colors.ink, fontSize = 14.sp)
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
    viewModel: AggiungiSpesaViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val state by viewModel.uiState.collectAsState()
    val geckoId by selectionViewModel.selectedGeckoId.collectAsState()
    val modalitaModifica = expenseId != null

    LaunchedEffect(expenseId) { expenseId?.let(viewModel::caricaPerModifica) }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            SectionTitle(stringResource(R.string.spesa_categoria))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoriaSpesa.entries.forEach { cat ->
                    TerraChip(
                        text = categoriaLabel(cat),
                        variant = if (cat == state.categoria) ChipVariant.TEAL else ChipVariant.NEUTRAL,
                        onClick = { viewModel.onCategoriaChange(cat) },
                    )
                }
            }

            SectionTitle(stringResource(R.string.spesa_importo))
            OutlinedTextField(
                value = state.importoTesto,
                onValueChange = viewModel::onImportoChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.spesa_importo_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            SectionTitle(stringResource(R.string.spesa_data))
            Text(
                state.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                color = colors.ink,
                fontFamily = JetBrainsMono,
                fontSize = 14.sp,
            )

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

            SectionTitle(stringResource(R.string.spesa_note))
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text(stringResource(R.string.spesa_note_placeholder)) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = if (modalitaModifica) stringResource(R.string.spesa_salva_modifiche) else stringResource(R.string.spesa_salva),
                enabled = state.importoTesto.isNotBlank(),
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
