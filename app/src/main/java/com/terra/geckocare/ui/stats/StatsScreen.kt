package com.terra.geckocare.ui.stats

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.terra.geckocare.R
import com.terra.geckocare.di.terraViewModel
import com.terra.geckocare.ui.common.EyebrowText
import com.terra.geckocare.ui.common.GhostButton
import com.terra.geckocare.ui.common.PageTitle
import com.terra.geckocare.ui.common.SectionTitle
import com.terra.geckocare.ui.common.TerraCard
import com.terra.geckocare.ui.navigation.GeckoSelectionViewModel
import com.terra.geckocare.ui.theme.JetBrainsMono
import com.terra.geckocare.ui.theme.LocalTerraColors
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun StatsScreen(
    selectionViewModel: GeckoSelectionViewModel,
    viewModel: StatsViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedId by selectionViewModel.selectedGeckoId.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val esportaChooserTitolo = stringResource(R.string.stats_esporta_chooser)

    LaunchedEffect(selectedId) { selectedId?.let(viewModel::selezionaGecko) }

    fun condividi(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, esportaChooserTitolo))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 24.dp),
    ) {
        EyebrowText(stringResource(R.string.stats_eyebrow, state.nomeGecko))
        PageTitle(stringResource(R.string.stats_title))

        SectionTitle(stringResource(R.string.stats_peso_tempo))
        TerraCard {
            if (state.pesoPunti.size < 2) {
                Text(stringResource(R.string.stats_peso_vuoto), color = colors.inkFaint, fontSize = 12.5.sp)
            } else {
                WeightLineChart(punti = state.pesoPunti.map { it.second }, modifier = Modifier.fillMaxWidth().height(110.dp))
            }
        }

        SectionTitle(stringResource(R.string.stats_pasti_settimana))
        TerraCard {
            if (state.pastiSettimanali.all { it == 0 }) {
                Text(stringResource(R.string.stats_pasti_vuoto), color = colors.inkFaint, fontSize = 12.5.sp)
            } else {
                MealsBarChart(valori = state.pastiSettimanali, modifier = Modifier.fillMaxWidth().height(120.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    (1..5).forEach { Text("S$it", color = colors.inkFaint, fontFamily = JetBrainsMono, fontSize = 10.sp) }
                }
            }
        }

        SectionTitle(stringResource(R.string.stats_riepilogo))
        TerraCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RiepilogoRow(stringResource(R.string.stats_intervallo_muta), state.intervalloMedioMutaLabel)
                RiepilogoRow(stringResource(R.string.stats_crescita_mese), state.crescitaUltimoMeseLabel)
                RiepilogoRow(stringResource(R.string.stats_pasti_rifiutati), state.tassoRifiutoLabel)
            }
        }

        Spacer(Modifier.height(18.dp))
        GhostButton(
            text = stringResource(R.string.stats_esporta_animale),
            onClick = {
                scope.launch {
                    viewModel.esportaCsvGeckoCorrente()?.let(::condividi)
                }
            },
        )
        Spacer(Modifier.height(10.dp))
        GhostButton(
            text = stringResource(R.string.stats_esporta_allevamento),
            onClick = {
                scope.launch {
                    condividi(viewModel.esportaCsvAllevamento())
                }
            },
        )
    }
}

@Composable
private fun RiepilogoRow(etichetta: String, valore: String) {
    val colors = LocalTerraColors.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etichetta, color = colors.inkDim, fontSize = 13.sp)
        Text(valore, color = colors.ink, fontFamily = JetBrainsMono, fontSize = 13.sp)
    }
}

@Composable
private fun WeightLineChart(punti: List<Double>, modifier: Modifier = Modifier) {
    val colors = LocalTerraColors.current
    val tealColor = colors.teal
    val clayColor = colors.clay
    Canvas(modifier = modifier) {
        val min = punti.min()
        val max = punti.max()
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val stepX = size.width / (punti.size - 1)
        val path = androidx.compose.ui.graphics.Path()
        punti.forEachIndexed { index, v ->
            val x = index * stepX
            val y = size.height - ((v - min) / range * size.height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = tealColor, style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        val lastX = (punti.size - 1) * stepX
        val lastY = size.height - ((punti.last() - min) / range * size.height).toFloat()
        drawCircle(color = clayColor, radius = 5f, center = Offset(lastX, lastY))
    }
}

@Composable
private fun MealsBarChart(valori: List<Int>, modifier: Modifier = Modifier) {
    val colors = LocalTerraColors.current
    val tealColor = colors.teal
    val clayColor = colors.clay
    val maxValore = (valori.maxOrNull() ?: 1).coerceAtLeast(1)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        valori.forEachIndexed { index, v ->
            val frazione = (v.toFloat() / maxValore.toFloat()).coerceAtLeast(0.02f)
            Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                if (frazione < 1f) {
                    Spacer(Modifier.weight(1f - frazione))
                }
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(frazione)
                        .background(if (index == valori.lastIndex) clayColor else tealColor, androidx.compose.foundation.shape.RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                )
            }
        }
    }
}
