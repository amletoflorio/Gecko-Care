package com.terra.geckocare.ui.home

import com.terra.geckocare.ui.common.TerraAsyncImage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.terra.geckocare.R
import com.terra.geckocare.di.terraViewModel
import com.terra.geckocare.data.local.entity.GeckoPhoto
import com.terra.geckocare.data.local.entity.Reminder
import com.terra.geckocare.data.local.entity.TipoReminder
import com.terra.geckocare.domain.ActivityKind
import com.terra.geckocare.ui.common.ChipVariant
import com.terra.geckocare.ui.common.EyebrowText
import com.terra.geckocare.ui.common.GhostButton
import com.terra.geckocare.ui.common.PageTitle
import com.terra.geckocare.ui.common.SectionTitle
import com.terra.geckocare.ui.common.SegmentedControl
import com.terra.geckocare.ui.common.TerraCard
import com.terra.geckocare.ui.common.TerraChip
import com.terra.geckocare.ui.common.rememberQrCodeBitmap
import com.terra.geckocare.ui.navigation.GeckoSelectionViewModel
import com.terra.geckocare.ui.theme.JetBrainsMono
import com.terra.geckocare.ui.theme.LocalTerraColors

@Composable
fun HomeScreen(
    selectionViewModel: GeckoSelectionViewModel,
    onModificaGecko: (Long) -> Unit,
    onAggiungiNota: () -> Unit,
    onAggiungiAmbiente: () -> Unit,
    onModificaAmbiente: (Long) -> Unit,
    onAggiungiMuta: () -> Unit,
    onModificaMuta: (Long) -> Unit,
    onAggiungiCrescita: () -> Unit,
    onModificaCrescita: (Long) -> Unit,
    onNuovoPromemoria: () -> Unit,
    onModificaPromemoria: (Long) -> Unit,
    homeViewModel: HomeViewModel = terraViewModel(),
    galleryViewModel: GalleryViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val gechi by selectionViewModel.gechiAttivi.collectAsState()
    val selectedId by selectionViewModel.selectedGeckoId.collectAsState()
    val state by homeViewModel.uiState.collectAsState()
    val foto by galleryViewModel.foto.collectAsState()
    val context = LocalContext.current
    val erroreFotoTesto = stringResource(R.string.foto_errore_salvataggio)

    LaunchedEffect(selectedId) {
        selectedId?.let(homeViewModel::selezionaGecko)
        selectedId?.let(galleryViewModel::selezionaGecko)
    }
    LaunchedEffect(Unit) {
        galleryViewModel.erroreFoto.collect {
            android.widget.Toast.makeText(context, erroreFotoTesto, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    if (gechi.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(R.string.home_empty_title), color = colors.ink, style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.home_empty_subtitle),
                color = colors.inkFaint,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        return
    }

    var panelIndex by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 24.dp),
    ) {
        EyebrowText(stringResource(R.string.home_eyebrow))
        PageTitle(state.gecko?.nome ?: "")

        LazyRow(
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(gechi) { g ->
                val selezionato = g.id == selectedId
                val tagColor = runCatching { Color(android.graphics.Color.parseColor(g.coloreTag)) }.getOrDefault(colors.teal)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100))
                        .background(if (selezionato) colors.tealDim else colors.surface)
                        .clickable { selectionViewModel.select(g.id) }
                        .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(tagColor.copy(alpha = 0.35f)),
                    )
                    Text(g.nome, color = colors.ink, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        SegmentedControl(
            options = listOf(stringResource(R.string.home_tab_panoramica), stringResource(R.string.home_tab_cura)),
            selectedIndex = panelIndex,
            onSelected = { panelIndex = it },
            modifier = Modifier.padding(top = 4.dp),
        )

        if (state.gecko == null) return@Column

        Spacer(Modifier.height(16.dp))
        if (panelIndex == 0) {
            PanoramicaContent(
                state,
                onAggiungiCrescita = onAggiungiCrescita,
                onModificaCrescita = onModificaCrescita,
                foto = foto,
                onAggiungiFoto = { uri -> selectedId?.let { galleryViewModel.aggiungiFoto(it, uri) } },
                onEliminaFoto = galleryViewModel::eliminaFoto,
                onModificaGecko = onModificaGecko,
            )
        } else {
            CuraAmbienteContent(
                state = state,
                onAggiungiNota = onAggiungiNota,
                onAggiungiAmbiente = onAggiungiAmbiente,
                onModificaAmbiente = onModificaAmbiente,
                onAggiungiMuta = onAggiungiMuta,
                onModificaMuta = onModificaMuta,
                onNuovoPromemoria = onNuovoPromemoria,
                onModificaPromemoria = onModificaPromemoria,
                onToggleReminder = homeViewModel::toggleReminder,
            )
        }
    }
}

@Composable
private fun PanoramicaContent(
    state: HomeUiState,
    onAggiungiCrescita: () -> Unit,
    onModificaCrescita: (Long) -> Unit,
    foto: List<GeckoPhoto>,
    onAggiungiFoto: (Uri) -> Unit,
    onEliminaFoto: (GeckoPhoto) -> Unit,
    onModificaGecko: (Long) -> Unit,
) {
    val colors = LocalTerraColors.current
    val context = LocalContext.current
    val gecko = state.gecko ?: return

    // Hero
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surface)
            .padding(20.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text(gecko.nome, color = colors.ink, style = MaterialTheme.typography.headlineMedium)
                Text(state.etaLabel, color = colors.inkDim, fontFamily = JetBrainsMono, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(onClick = { onModificaGecko(gecko.id) }) {
                    Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.home_modifica_geco_desc), tint = colors.inkFaint)
                }
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(runCatching { Color(android.graphics.Color.parseColor(gecko.coloreTag)) }.getOrDefault(colors.teal).copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center,
                ) {
                if (gecko.fotoProfiloPath != null) {
                    TerraAsyncImage(
                        model = java.io.File(gecko.fotoProfiloPath),
                        contentDescription = gecko.nome,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                } else {
                    Text(gecko.nome.take(1).uppercase(), color = colors.teal, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HStat(state.prossimoPastoLabel, stringResource(R.string.home_stat_prossimo_pasto), Modifier.weight(1f))
            HStat(state.pesoAttualeLabel, stringResource(R.string.home_stat_peso_attuale), Modifier.weight(1f))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HStat(state.giorniMutaLabel, stringResource(R.string.home_stat_dall_ultima_muta), Modifier.weight(1f))
            HStat(state.hotSpotOraLabel, stringResource(R.string.home_stat_hotspot_ora), Modifier.weight(1f))
        }
    }

    SectionTitle(stringResource(R.string.home_crescita))
    TerraCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            GrowthValue(stringResource(R.string.home_peso), state.pesoValore, state.pesoTrendSu, Modifier.weight(1f))
            GrowthValue(stringResource(R.string.home_lunghezza), state.lunghezzaValore, state.lunghezzaTrendSu, Modifier.weight(1f))
        }
    }
    if (state.crescitaStorica.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        TerraCard {
            Column {
                state.crescitaStorica.forEachIndexed { index, log ->
                    if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                    CrescitaRow(log = log, onClick = { onModificaCrescita(log.id) })
                }
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    GhostButton(text = stringResource(R.string.home_registra_crescita), onClick = onAggiungiCrescita)

    SectionTitle(stringResource(R.string.home_galleria))
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let(onAggiungiFoto)
    }
    var fotoInVisualizzazione by remember { mutableStateOf<GeckoPhoto?>(null) }
    if (foto.isEmpty()) {
        TerraCard { Text(stringResource(R.string.home_galleria_vuota), color = colors.inkFaint, fontSize = 12.5.sp) }
        Spacer(Modifier.height(10.dp))
        GhostButton(
            text = stringResource(R.string.home_galleria_aggiungi_desc),
            onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        )
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface2)
                        .clickable { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.home_galleria_aggiungi_desc), tint = colors.inkFaint)
                }
            }
            items(foto) { f ->
                TerraAsyncImage(
                    model = java.io.File(f.path),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { fotoInVisualizzazione = f },
                )
            }
        }
    }

    fotoInVisualizzazione?.let { f ->
        Dialog(onDismissRequest = { fotoInVisualizzazione = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface),
            ) {
                TerraAsyncImage(
                    model = java.io.File(f.path),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        com.terra.geckocare.domain.formatGiorniFa(context, f.dataOra),
                        color = colors.inkFaint,
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 10.dp),
                    )
                    Row {
                        IconButton(onClick = { onEliminaFoto(f); fotoInVisualizzazione = null }) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.home_galleria_elimina), tint = colors.danger)
                        }
                        IconButton(onClick = { fotoInVisualizzazione = null }) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.common_chiudi), tint = colors.ink)
                        }
                    }
                }
            }
        }
    }

    SectionTitle(stringResource(R.string.home_condividi_scheda))
    var qrEspanso by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    TerraCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.clickable { qrEspanso = true },
        ) {
            val qr = rememberQrCodeBitmap(state.qrContent)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(6.dp),
            ) {
                Image(bitmap = qr, contentDescription = stringResource(R.string.home_qr_desc), modifier = Modifier.fillMaxSize())
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.home_qr_title), color = colors.ink, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                Text(
                    stringResource(R.string.home_qr_subtitle, gecko.nome, state.shareCode),
                    color = colors.inkDim,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    stringResource(R.string.home_qr_tap_hint),
                    color = colors.teal,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }

    if (qrEspanso) {
        Dialog(onDismissRequest = { qrEspanso = false }) {
            val qrGrande = rememberQrCodeBitmap(state.qrContent, sizePx = 720)
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .clickable { qrEspanso = false }
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    bitmap = qrGrande,
                    contentDescription = stringResource(R.string.home_qr_desc),
                    modifier = Modifier.size(280.dp),
                )
                Text(
                    gecko.nome,
                    color = Color(0xFF29251F),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }

    SectionTitle(stringResource(R.string.home_piano_alimentare))
    TerraCard(backgroundColor = if (state.pianoNomeFase != null) colors.tealDim else colors.surface) {
        Column {
            Text(state.pianoNomeFase ?: stringResource(R.string.home_piano_nessuna_fascia), color = colors.ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(state.pianoDettaglio, color = colors.inkDim, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }

    SectionTitle(stringResource(R.string.home_attivita_recente))
    if (state.attivitaRecente.isEmpty()) {
        TerraCard {
            Text(stringResource(R.string.home_attivita_vuota), color = colors.inkFaint, fontSize = 12.5.sp)
        }
    } else {
        TerraCard {
            Column {
                state.attivitaRecente.forEachIndexed { index, item ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.line),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val dotColor = when (item.kind) {
                            ActivityKind.PASTO -> colors.sage
                            ActivityKind.PESO -> colors.teal
                            ActivityKind.LUNGHEZZA -> colors.teal
                            ActivityKind.MUTA -> colors.clay
                            ActivityKind.NOTA -> colors.slate
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(dotColor),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.label, color = colors.ink, fontSize = 13.5.sp)
                            Text(item.timestampLabel(context), color = colors.inkFaint, fontFamily = JetBrainsMono, fontSize = 10.5.sp, modifier = Modifier.padding(top = 1.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CuraAmbienteContent(
    state: HomeUiState,
    onAggiungiNota: () -> Unit,
    onAggiungiAmbiente: () -> Unit,
    onModificaAmbiente: (Long) -> Unit,
    onAggiungiMuta: () -> Unit,
    onModificaMuta: (Long) -> Unit,
    onNuovoPromemoria: () -> Unit,
    onModificaPromemoria: (Long) -> Unit,
    onToggleReminder: (Reminder) -> Unit,
) {
    val colors = LocalTerraColors.current
    val context = LocalContext.current

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        EnvCard(stringResource(R.string.home_env_hotspot), state.hotSpotC, Modifier.weight(1f))
        EnvCard(stringResource(R.string.home_env_coldspot), state.coldSpotC, Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        EnvCard(stringResource(R.string.home_env_umidita), state.umidita, Modifier.weight(1f))
        EnvCard(stringResource(R.string.home_env_notturna), state.notturnaC, Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    GhostButton(text = stringResource(R.string.home_registra_ambiente), onClick = onAggiungiAmbiente)

    SectionTitle(stringResource(R.string.home_storico_letture))
    if (state.letture.isEmpty()) {
        TerraCard { Text(stringResource(R.string.home_letture_vuoto), color = colors.inkFaint, fontSize = 12.5.sp) }
    } else {
        TerraCard {
            Column {
                state.letture.forEachIndexed { index, log ->
                    if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                    LetturaAmbienteRow(log = log, onClick = { onModificaAmbiente(log.id) })
                }
            }
        }
    }

    SectionTitle(stringResource(R.string.home_prossima_muta))
    TerraCard {
        val trascorsi = state.mutaGiorniTrascorsi ?: 0
        val tipico = state.mutaIntervalloTipico.coerceAtLeast(1)
        val inLinea = trascorsi <= tipico + 3
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (state.mutaGiorniTrascorsi == null) stringResource(R.string.home_muta_nessuna) else stringResource(R.string.home_muta_progresso, trascorsi, tipico),
                color = colors.inkDim,
                fontSize = 13.sp,
            )
            if (state.mutaGiorniTrascorsi != null) {
                TerraChip(
                    text = if (inLinea) stringResource(R.string.home_muta_in_linea) else stringResource(R.string.home_muta_in_ritardo),
                    variant = if (inLinea) ChipVariant.SAGE else ChipVariant.DANGER,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.line),
        ) {
            val frazione = (trascorsi.toFloat() / tipico.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(frazione)
                    .height(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.sage),
            )
        }
    }
    if (state.muteStoriche.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        TerraCard {
            Column {
                state.muteStoriche.forEachIndexed { index, log ->
                    if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                    MutaRow(log = log, onClick = { onModificaMuta(log.id) })
                }
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    GhostButton(text = stringResource(R.string.home_registra_muta), onClick = onAggiungiMuta)

    SectionTitle(stringResource(R.string.home_note_comportamentali))
    if (state.noteComportamentali.isEmpty()) {
        TerraCard { Text(stringResource(R.string.home_note_vuoto), color = colors.inkFaint, fontSize = 12.5.sp) }
    } else {
        TerraCard {
            Column {
                state.noteComportamentali.take(6).forEachIndexed { index, nota ->
                    if (index > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.sage))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nota.testo, color = colors.ink, fontSize = 13.5.sp)
                            Text(
                                com.terra.geckocare.domain.formatGiorniFa(context, nota.dataOra),
                                color = colors.inkFaint,
                                fontFamily = JetBrainsMono,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(top = 1.dp),
                            )
                        }
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(18.dp))
    GhostButton(text = stringResource(R.string.home_aggiungi_nota), onClick = onAggiungiNota)

    SectionTitle(stringResource(R.string.home_promemoria_personalizzati))
    if (state.reminders.isEmpty()) {
        TerraCard { Text(stringResource(R.string.home_promemoria_vuoto), color = colors.inkFaint, fontSize = 12.5.sp) }
    } else {
        TerraCard {
            Column {
                state.reminders.forEachIndexed { index, reminder ->
                    if (index > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                    }
                    ReminderRow(
                        reminder = reminder,
                        onClick = { onModificaPromemoria(reminder.id) },
                        onToggle = { onToggleReminder(reminder) },
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    GhostButton(text = stringResource(R.string.home_nuovo_promemoria), onClick = onNuovoPromemoria)
}

@Composable
private fun ReminderRow(reminder: Reminder, onClick: () -> Unit, onToggle: () -> Unit) {
    val colors = LocalTerraColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(reminderTipoLabel(reminder.tipo), color = colors.ink, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
            Text(reminder.frequenzaTesto, color = colors.inkFaint, fontFamily = JetBrainsMono, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Box(
            modifier = Modifier
                .width(38.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(100))
                .background(if (reminder.attivo) colors.teal else colors.surface2)
                .clickable(onClick = onToggle),
            contentAlignment = if (reminder.attivo) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (reminder.attivo) colors.night else colors.ink),
            )
        }
    }
}

@Composable
private fun reminderTipoLabel(tipo: TipoReminder): String = when (tipo) {
    TipoReminder.PASTO -> stringResource(R.string.promemoria_tipo_pasto)
    TipoReminder.PESO -> stringResource(R.string.promemoria_tipo_peso)
    TipoReminder.PULIZIA_TERRARIO -> stringResource(R.string.promemoria_tipo_pulizia)
    TipoReminder.INTEGRATORI -> stringResource(R.string.promemoria_tipo_integratori_controllo)
    TipoReminder.ALTRO -> stringResource(R.string.promemoria_tipo_altro_generico)
}

@Composable
private fun HStat(valore: String, etichetta: String, modifier: Modifier = Modifier) {
    val colors = LocalTerraColors.current
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 14.dp),
    ) {
        Text(valore, color = colors.teal, fontFamily = JetBrainsMono, fontSize = 19.sp)
        Text(etichetta, color = colors.inkDim, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun GrowthValue(etichetta: String, valore: String, trendSu: Boolean?, modifier: Modifier = Modifier) {
    val colors = LocalTerraColors.current
    Column(modifier = modifier) {
        Text(etichetta, color = colors.inkDim, fontSize = 11.sp)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 3.dp)) {
            Text(valore, color = colors.teal, fontFamily = JetBrainsMono, fontSize = 18.sp)
            if (trendSu != null) {
                Text(
                    if (trendSu) " ↑" else " ↓",
                    color = if (trendSu) colors.sage else colors.danger,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun EnvCard(etichetta: String, valore: String, modifier: Modifier = Modifier) {
    val colors = LocalTerraColors.current
    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(16.dp))
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(etichetta, color = colors.inkDim, fontSize = 11.sp)
        Text(valore, color = colors.teal, fontFamily = JetBrainsMono, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun CrescitaRow(log: com.terra.geckocare.data.local.entity.GrowthLog, onClick: () -> Unit) {
    val colors = LocalTerraColors.current
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            com.terra.geckocare.domain.formatGiorniFa(context, log.dataOra),
            color = colors.inkFaint,
            fontFamily = JetBrainsMono,
            fontSize = 10.5.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            log.pesoGrammi?.let { Text("%.1f g".format(it), color = colors.teal, fontFamily = JetBrainsMono, fontSize = 12.sp) }
            log.lunghezzaCm?.let { Text("%.1f cm".format(it), color = colors.teal, fontFamily = JetBrainsMono, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun MutaRow(log: com.terra.geckocare.data.local.entity.ShedLog, onClick: () -> Unit) {
    val colors = LocalTerraColors.current
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            com.terra.geckocare.domain.formatGiorniFa(context, log.dataOra),
            color = colors.inkFaint,
            fontFamily = JetBrainsMono,
            fontSize = 10.5.sp,
        )
        TerraChip(
            text = if (log.completa) stringResource(R.string.home_muta_completa) else stringResource(R.string.home_muta_parziale),
            variant = if (log.completa) ChipVariant.SAGE else ChipVariant.CLAY,
        )
    }
}

@Composable
private fun LetturaAmbienteRow(log: com.terra.geckocare.data.local.entity.EnvironmentLog, onClick: () -> Unit) {
    val colors = LocalTerraColors.current
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            com.terra.geckocare.domain.formatGiorniFa(context, log.dataOra),
            color = colors.inkFaint,
            fontFamily = JetBrainsMono,
            fontSize = 10.5.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            log.hotSpotC?.let { Text("🔥 %.0f°".format(it), color = colors.teal, fontFamily = JetBrainsMono, fontSize = 12.sp) }
            log.coldSpotC?.let { Text("❄️ %.0f°".format(it), color = colors.clay, fontFamily = JetBrainsMono, fontSize = 12.sp) }
            log.umiditaPercento?.let { Text("💧 %.0f%%".format(it), color = colors.slate, fontFamily = JetBrainsMono, fontSize = 12.sp) }
        }
    }
}
