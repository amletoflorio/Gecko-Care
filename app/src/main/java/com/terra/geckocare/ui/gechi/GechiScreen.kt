package com.terra.geckocare.ui.gechi

import com.terra.geckocare.ui.common.TerraAsyncImage

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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.terra.geckocare.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terra.geckocare.di.terraViewModel
import com.terra.geckocare.ui.common.ChipVariant
import com.terra.geckocare.ui.common.EyebrowText
import com.terra.geckocare.ui.common.GhostButton
import com.terra.geckocare.ui.common.PageTitle
import com.terra.geckocare.ui.common.SectionTitle
import com.terra.geckocare.ui.common.TerraCard
import com.terra.geckocare.ui.common.TerraChip
import com.terra.geckocare.ui.theme.JetBrainsMono
import com.terra.geckocare.ui.theme.LocalTerraColors

@Composable
fun GechiScreen(
    onGeckoClick: (Long) -> Unit,
    onAggiungiGecko: () -> Unit,
    viewModel: GechiViewModel = terraViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalTerraColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 24.dp),
    ) {
        EyebrowText(stringResource(R.string.gechi_eyebrow))
        PageTitle(stringResource(R.string.gechi_title))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BreederSummaryItem("${state.totaleAnimali}", stringResource(R.string.gechi_stat_animali), Modifier.weight(1f))
            BreederSummaryItem("${state.pastoOggiCount}", stringResource(R.string.gechi_stat_pasto_oggi), Modifier.weight(1f))
            BreederSummaryItem(formatEuro(state.speseMeseCorrente), stringResource(R.string.gechi_stat_spese_mese), Modifier.weight(1f))
        }

        if (state.tagDisponibili.isNotEmpty()) {
            SectionTitle(stringResource(R.string.gechi_filtra_tag))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                item {
                    TerraChip(
                        text = stringResource(R.string.gechi_tag_tutti),
                        variant = if (state.tagSelezionato == null) ChipVariant.TEAL else ChipVariant.NEUTRAL,
                        onClick = { viewModel.selezionaTag(null) },
                    )
                }
                items(state.tagDisponibili) { tag ->
                    TerraChip(
                        text = tag,
                        variant = if (state.tagSelezionato == tag) ChipVariant.TEAL else ChipVariant.NEUTRAL,
                        onClick = { viewModel.selezionaTag(tag) },
                    )
                }
            }
        }

        SectionTitle(stringResource(R.string.gechi_tutti_animali))

        if (state.righeFiltrate.isEmpty() && !state.caricamento) {
            TerraCard {
                Text(
                    text = if (state.righe.isEmpty()) {
                        stringResource(R.string.gechi_empty_nessuno)
                    } else {
                        stringResource(R.string.gechi_empty_tag)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkDim,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.righeFiltrate.forEach { riga ->
                    GeckoRow(riga = riga, onClick = { onGeckoClick(riga.id) })
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        GhostButton(text = stringResource(R.string.gechi_aggiungi_button), onClick = onAggiungiGecko)
    }
}

@Composable
private fun BreederSummaryItem(numero: String, etichetta: String, modifier: Modifier = Modifier) {
    val colors = LocalTerraColors.current
    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(numero, color = colors.teal, fontFamily = JetBrainsMono, fontSize = 17.sp)
        Text(
            etichetta.uppercase(),
            color = colors.inkFaint,
            fontSize = 9.5.sp,
            letterSpacing = 0.4.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun GeckoRow(riga: GeckoRowUiModel, onClick: () -> Unit) {
    val colors = LocalTerraColors.current
    val tagColor = runCatching { Color(android.graphics.Color.parseColor(riga.coloreTag)) }.getOrDefault(colors.teal)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tagColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            if (riga.fotoProfiloPath != null) {
                TerraAsyncImage(
                    model = java.io.File(riga.fotoProfiloPath),
                    contentDescription = riga.nome,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                )
            } else {
                Text(
                    text = riga.nome.take(1).uppercase(),
                    color = tagColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(riga.nome, color = colors.ink, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
            if (riga.morph.isNotBlank()) {
                Text(riga.morph, color = colors.inkDim, fontSize = 11.5.sp, modifier = Modifier.padding(top = 1.dp))
            }
        }

        Column(horizontalAlignment = Alignment.End, modifier = Modifier.wrapContentWidth()) {
            Text(riga.pesoLabel, color = colors.ink, fontFamily = JetBrainsMono, fontSize = 13.sp)
            Text(riga.etaLabel, color = colors.inkFaint, fontSize = 10.5.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun formatEuro(value: Double): String = com.terra.geckocare.domain.formatImporto(value)
