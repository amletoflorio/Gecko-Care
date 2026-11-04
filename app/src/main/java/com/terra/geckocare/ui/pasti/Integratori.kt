package com.terra.geckocare.ui.pasti

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terra.geckocare.domain.IntegratoriPrevisti
import com.terra.geckocare.ui.common.TerraChip
import androidx.compose.ui.res.stringResource
import com.terra.geckocare.R
import com.terra.geckocare.ui.common.ChipVariant

/** "Calcio + D3 + Multivit.", oppure null se nessun integratore. Condiviso da registro pasti e schema del piano. */
@Composable
fun integratoriLabel(conCalcio: Boolean, conD3: Boolean, conMultivitaminico: Boolean): String? = buildList {
    if (conCalcio) add(stringResource(R.string.integratore_calcio))
    if (conD3) add(stringResource(R.string.integratore_d3))
    if (conMultivitaminico) add(stringResource(R.string.integratore_multivit))
}.takeIf { it.isNotEmpty() }?.joinToString(" + ")

fun integratoriVariant(conCalcio: Boolean, conD3: Boolean, conMultivitaminico: Boolean): ChipVariant = when {
    conCalcio && conD3 -> ChipVariant.SAGE
    conCalcio || conD3 || conMultivitaminico -> ChipVariant.CLAY
    else -> ChipVariant.NEUTRAL
}

/** Cosa dare al pasto previsto dal piano: un chip per ogni tipo di insetto e uno per gli integratori. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PastoPrevistoChips(p: IntegratoriPrevisti, modifier: Modifier = Modifier) {
    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        p.insetti.forEach { TerraChip(text = "🦗 $it", variant = ChipVariant.TEAL) }
        TerraChip(
            text = integratoriLabel(p.conCalcio, p.conD3, p.conMultivitaminico)?.let { "● $it" }
                ?: stringResource(R.string.pasti_nessun_integratore),
            variant = integratoriVariant(p.conCalcio, p.conD3, p.conMultivitaminico),
        )
    }
}
