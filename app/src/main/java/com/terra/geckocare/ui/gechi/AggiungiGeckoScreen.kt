@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.terra.geckocare.ui.gechi

import com.terra.geckocare.ui.common.TerraAsyncImage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terra.geckocare.R
import com.terra.geckocare.di.terraViewModel
import com.terra.geckocare.data.local.entity.Sesso
import com.terra.geckocare.ui.common.ChipVariant
import com.terra.geckocare.ui.common.ColorPickerDialog
import com.terra.geckocare.ui.common.PrimaryButton
import com.terra.geckocare.ui.common.SectionTitle
import com.terra.geckocare.ui.common.SegmentedControl
import com.terra.geckocare.ui.common.TerraChip
import com.terra.geckocare.ui.theme.LocalTerraColors
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AggiungiGeckoScreen(
    onDone: () -> Unit,
    onClose: () -> Unit,
    geckoIdDaModificare: Long? = null,
    viewModel: AggiungiGeckoViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val state by viewModel.uiState.collectAsState()
    val gechi by viewModel.gechi.collectAsState()
    val context = LocalContext.current
    val erroreFotoTesto = stringResource(R.string.foto_errore_salvataggio)

    LaunchedEffect(geckoIdDaModificare) {
        geckoIdDaModificare?.let { viewModel.caricaPerModifica(it) }
    }
    LaunchedEffect(state.salvataggioCompletato) {
        if (state.salvataggioCompletato) onDone()
    }
    LaunchedEffect(state.erroreFoto) {
        if (state.erroreFoto) android.widget.Toast.makeText(context, erroreFotoTesto, android.widget.Toast.LENGTH_LONG).show()
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) viewModel.onFotoScelta(uri)
    }

    var mostraPickerNascita by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var mostraPickerAcquisizione by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var nuovoTagInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var pickerGenitore by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Sesso?>(null) }

    Scaffold(
        containerColor = colors.night,
        topBar = {
            TopAppBar(
                title = { Text(if (geckoIdDaModificare != null) stringResource(R.string.gecko_form_title_modifica) else stringResource(R.string.gecko_form_title_nuovo), color = colors.ink) },
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
            // Foto profilo
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(state.coloreTag)).copy(alpha = 0.2f))
                        .clickable {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val fotoDaMostrare: Any? = state.fotoPendingUri ?: state.fotoSalvataPath?.let { java.io.File(it) }
                    if (fotoDaMostrare != null) {
                        TerraAsyncImage(
                            model = fotoDaMostrare,
                            contentDescription = stringResource(R.string.gecko_form_foto_hint),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                        )
                    } else {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = colors.inkFaint)
                    }
                }
            }
            Text(
                stringResource(R.string.gecko_form_foto_hint),
                color = colors.inkFaint,
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            SectionTitle(stringResource(R.string.gecko_form_nome))
            OutlinedTextField(
                value = state.nome,
                onValueChange = viewModel::onNomeChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.gecko_form_nome_placeholder)) },
                isError = state.nomeMancante,
                supportingText = if (state.nomeMancante) {
                    { Text(stringResource(R.string.gecko_form_nome_obbligatorio)) }
                } else null,
                colors = terraTextFieldColors(),
                singleLine = true,
            )

            SectionTitle(stringResource(R.string.gecko_form_morph))
            OutlinedTextField(
                value = state.morph,
                onValueChange = viewModel::onMorphChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.gecko_form_morph_placeholder)) },
                colors = terraTextFieldColors(),
                singleLine = true,
            )

            SectionTitle(stringResource(R.string.gecko_form_sesso))
            SegmentedControl(
                options = listOf(
                    stringResource(R.string.gecko_sesso_sconosciuto),
                    stringResource(R.string.gecko_sesso_maschio),
                    stringResource(R.string.gecko_sesso_femmina),
                ),
                selectedIndex = when (state.sesso) {
                    Sesso.SCONOSCIUTO -> 0
                    Sesso.MASCHIO -> 1
                    Sesso.FEMMINA -> 2
                },
                onSelected = { index ->
                    viewModel.onSessoChange(
                        when (index) {
                            1 -> Sesso.MASCHIO
                            2 -> Sesso.FEMMINA
                            else -> Sesso.SCONOSCIUTO
                        },
                    )
                },
            )

            SectionTitle(stringResource(R.string.gecko_form_date_title))
            Text(
                stringResource(R.string.gecko_form_date_hint),
                color = colors.inkFaint,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DataPickerField(
                    etichetta = stringResource(R.string.gecko_form_nascita),
                    data = state.dataNascita,
                    onClick = { mostraPickerNascita = true },
                    onClear = { viewModel.onDataNascitaChange(null) },
                    modifier = Modifier.weight(1f),
                )
                DataPickerField(
                    etichetta = stringResource(R.string.gecko_form_gotcha),
                    data = state.dataAcquisizione,
                    onClick = { mostraPickerAcquisizione = true },
                    onClear = { viewModel.onDataAcquisizioneChange(null) },
                    modifier = Modifier.weight(1f),
                )
            }

            SectionTitle(stringResource(R.string.gecko_form_genitori))
            Text(
                stringResource(R.string.gecko_form_genitori_hint),
                color = colors.inkFaint,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                GenitoreField(
                    etichetta = stringResource(R.string.gecko_padre),
                    valore = nomeGenitore(state.padre, gechi),
                    foto = fotoGenitore(state.padre, gechi),
                    onClick = { pickerGenitore = Sesso.MASCHIO },
                    onClear = { viewModel.onPadreChange(GenitoreScelto()) },
                    modifier = Modifier.weight(1f),
                )
                GenitoreField(
                    etichetta = stringResource(R.string.gecko_madre),
                    valore = nomeGenitore(state.madre, gechi),
                    foto = fotoGenitore(state.madre, gechi),
                    onClick = { pickerGenitore = Sesso.FEMMINA },
                    onClear = { viewModel.onMadreChange(GenitoreScelto()) },
                    modifier = Modifier.weight(1f),
                )
            }

            SectionTitle(stringResource(R.string.gecko_form_colore))
            var mostraColorPicker by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            val coloreCustomAttivo = state.coloreTag !in PaletteColoriGecko
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PaletteColoriGecko.forEach { hex ->
                    val selezionato = state.coloreTag == hex
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(hex)))
                            .clickable { viewModel.onColoreChange(hex) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selezionato) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = null,
                                tint = colors.onAccent,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (coloreCustomAttivo) Color(android.graphics.Color.parseColor(state.coloreTag)) else colors.surface2)
                        .border(
                            BorderStroke(1.dp, if (coloreCustomAttivo) Color.Transparent else colors.line),
                            CircleShape,
                        )
                        .clickable { mostraColorPicker = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Colorize,
                        contentDescription = stringResource(R.string.gecko_form_colore_custom_desc),
                        tint = if (coloreCustomAttivo) colors.onAccent else colors.inkFaint,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
            if (mostraColorPicker) {
                ColorPickerDialog(
                    coloreIniziale = state.coloreTag,
                    onDismiss = { mostraColorPicker = false },
                    onConfirm = { hex -> viewModel.onColoreChange(hex); mostraColorPicker = false },
                )
            }

            SectionTitle(stringResource(R.string.gecko_form_tag))
            val tagSuggeriti = tagSuggeriti()
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(tagSuggeriti) { tag ->
                    TerraChip(
                        text = tag,
                        variant = if (tag in state.tagSelezionati) ChipVariant.TEAL else ChipVariant.NEUTRAL,
                        onClick = { viewModel.toggleTag(tag) },
                    )
                }
                items(state.tagSelezionati.filter { it !in tagSuggeriti }.toList()) { tag ->
                    TerraChip(text = tag, variant = ChipVariant.CLAY, onClick = { viewModel.toggleTag(tag) })
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = nuovoTagInput,
                    onValueChange = { nuovoTagInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.gecko_form_tag_placeholder)) },
                    singleLine = true,
                    colors = terraTextFieldColors(),
                )
                IconButton(
                    onClick = {
                        val pulito = nuovoTagInput.trim()
                        if (pulito.isNotEmpty()) {
                            viewModel.toggleTag(pulito)
                            nuovoTagInput = ""
                        }
                    },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.gecko_form_tag_aggiungi_desc), tint = colors.teal)
                }
            }

            SectionTitle(stringResource(R.string.gecko_form_descrizione))
            OutlinedTextField(
                value = state.descrizione,
                onValueChange = viewModel::onDescrizioneChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text(stringResource(R.string.gecko_form_descrizione_placeholder)) },
                colors = terraTextFieldColors(),
            )

            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = if (state.salvataggioInCorso) stringResource(R.string.gecko_form_salvataggio) else stringResource(R.string.gecko_form_salva),
                onClick = viewModel::salva,
                enabled = !state.salvataggioInCorso,
            )
            Spacer(Modifier.height(12.dp))
        }
    }

    pickerGenitore?.let { sesso ->
        val padre = sesso == Sesso.MASCHIO
        GenitorePickerDialog(
            titolo = stringResource(if (padre) R.string.gecko_form_scegli_padre else R.string.gecko_form_scegli_madre),
            // Esclude se stesso e gli animali di sesso opposto (i sesso sconosciuto restano selezionabili).
            candidati = gechi.filter { it.id != state.geckoId && it.sesso != (if (padre) Sesso.FEMMINA else Sesso.MASCHIO) },
            attuale = if (padre) state.padre else state.madre,
            onDismiss = { pickerGenitore = null },
            onConfirm = { scelto ->
                if (padre) viewModel.onPadreChange(scelto) else viewModel.onMadreChange(scelto)
                pickerGenitore = null
            },
        )
    }

    if (mostraPickerNascita) {
        DatePickerModal(
            dataIniziale = state.dataNascita,
            onDismiss = { mostraPickerNascita = false },
            onConfirm = { viewModel.onDataNascitaChange(it); mostraPickerNascita = false },
        )
    }
    if (mostraPickerAcquisizione) {
        DatePickerModal(
            dataIniziale = state.dataAcquisizione,
            onDismiss = { mostraPickerAcquisizione = false },
            onConfirm = { viewModel.onDataAcquisizioneChange(it); mostraPickerAcquisizione = false },
        )
    }
}

private fun nomeGenitore(g: GenitoreScelto, gechi: List<com.terra.geckocare.data.local.entity.Gecko>): String? =
    g.geckoId?.let { id -> gechi.firstOrNull { it.id == id }?.nome } ?: g.nomeEsterno.takeIf { it.isNotBlank() }

/** Modello per TerraAsyncImage: foto profilo se il genitore è dell'allevamento, altrimenti la foto esterna. */
private fun fotoGenitore(g: GenitoreScelto, gechi: List<com.terra.geckocare.data.local.entity.Gecko>): Any? =
    if (g.geckoId != null) {
        gechi.firstOrNull { it.id == g.geckoId }?.fotoProfiloPath?.let { java.io.File(it) }
    } else {
        g.fotoPendingUri ?: g.fotoSalvataPath?.let { java.io.File(it) }
    }

@Composable
private fun MiniaturaGenitore(foto: Any?, nome: String?, size: androidx.compose.ui.unit.Dp = 32.dp) {
    val colors = LocalTerraColors.current
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.surface),
        contentAlignment = Alignment.Center,
    ) {
        if (foto != null) {
            TerraAsyncImage(
                model = foto,
                contentDescription = nome,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
        } else {
            Text(nome?.take(1)?.uppercase() ?: "?", color = colors.inkFaint, fontSize = (size.value * 0.4f).sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun GenitoreField(
    etichetta: String,
    valore: String?,
    foto: Any?,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTerraColors.current
    Column(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(colors.surface2)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Text(etichetta, color = colors.inkFaint, fontSize = 10.5.sp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            if (valore != null) MiniaturaGenitore(foto, valore, size = 28.dp)
            Text(
                valore ?: stringResource(R.string.gecko_genitore_non_impostato),
                color = if (valore != null) colors.ink else colors.inkFaint,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(top = 2.dp),
            )
            if (valore != null) {
                IconButton(onClick = onClear, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.gecko_form_rimuovi_genitore), tint = colors.inkFaint, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

/** Scelta di un genitore: un geco dell'allevamento oppure un nome libero per animali esterni. */
@Composable
private fun GenitorePickerDialog(
    titolo: String,
    candidati: List<com.terra.geckocare.data.local.entity.Gecko>,
    attuale: GenitoreScelto,
    onDismiss: () -> Unit,
    onConfirm: (GenitoreScelto) -> Unit,
) {
    val colors = LocalTerraColors.current
    val attualeEsterno = attuale.geckoId == null
    var esterno by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(if (attualeEsterno) attuale.nomeEsterno else "") }
    var fotoPending by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(if (attualeEsterno) attuale.fotoPendingUri else null) }
    var fotoSalvata by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(if (attualeEsterno) attuale.fotoSalvataPath else null) }
    val pickFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) fotoPending = uri
    }
    val fotoEsterna: Any? = fotoPending ?: fotoSalvata?.let { java.io.File(it) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text(titolo, color = colors.ink) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (candidati.isEmpty()) {
                    Text(stringResource(R.string.gecko_form_nessun_candidato), color = colors.inkFaint, fontSize = 12.5.sp)
                }
                candidati.forEach { g ->
                    val selezionato = g.id == attuale.geckoId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                            .background(if (selezionato) colors.tealDim else Color.Transparent)
                            .clickable { onConfirm(GenitoreScelto(geckoId = g.id)) }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MiniaturaGenitore(g.fotoProfiloPath?.let { java.io.File(it) }, g.nome)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(g.nome, color = colors.ink, fontSize = 14.sp)
                            if (g.morph.isNotBlank()) Text(g.morph, color = colors.inkFaint, fontSize = 11.sp)
                        }
                        if (selezionato) Icon(Icons.Outlined.Check, contentDescription = null, tint = colors.teal, modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    stringResource(R.string.gecko_form_genitore_esterno),
                    color = colors.inkDim,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
                )
                OutlinedTextField(
                    value = esterno,
                    onValueChange = { esterno = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.gecko_form_genitore_esterno_placeholder)) },
                    singleLine = true,
                    colors = terraTextFieldColors(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(modifier = Modifier.clickable { pickFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        if (fotoEsterna != null) {
                            MiniaturaGenitore(fotoEsterna, esterno, size = 48.dp)
                        } else {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.surface2),
                                contentAlignment = Alignment.Center,
                            ) { Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = colors.inkFaint) }
                        }
                    }
                    androidx.compose.material3.TextButton(onClick = { pickFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Text(stringResource(if (fotoEsterna != null) R.string.gecko_form_genitore_cambia_foto else R.string.gecko_form_genitore_aggiungi_foto), color = colors.teal)
                    }
                    if (fotoEsterna != null) {
                        IconButton(onClick = { fotoPending = null; fotoSalvata = null }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.gecko_form_genitore_rimuovi_foto), tint = colors.inkFaint, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onConfirm(GenitoreScelto(nomeEsterno = esterno.trim(), fotoSalvataPath = fotoSalvata, fotoPendingUri = fotoPending)) },
                enabled = esterno.isNotBlank(),
            ) { Text(stringResource(R.string.gecko_form_usa_nome_esterno)) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_annulla)) }
        },
    )
}

@Composable
private fun DataPickerField(
    etichetta: String,
    data: LocalDate?,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTerraColors.current
    Column(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(colors.surface2)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Text(etichetta, color = colors.inkFaint, fontSize = 10.5.sp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                data?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: stringResource(R.string.gecko_form_data_non_impostata),
                color = if (data != null) colors.ink else colors.inkFaint,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
            if (data != null) {
                IconButton(onClick = onClear, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.gecko_form_rimuovi_data), tint = colors.inkFaint, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(dataIniziale: LocalDate?, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val state = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = dataIniziale?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
    )
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    val data = java.time.Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    onConfirm(data)
                } ?: onDismiss()
            }) { Text(stringResource(R.string.common_conferma)) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_annulla)) }
        },
    ) {
        androidx.compose.material3.DatePicker(state = state)
    }
}

@Composable
private fun terraTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LocalTerraColors.current.teal,
    unfocusedBorderColor = LocalTerraColors.current.line,
    focusedTextColor = LocalTerraColors.current.ink,
    unfocusedTextColor = LocalTerraColors.current.ink,
    cursorColor = LocalTerraColors.current.teal,
    focusedPlaceholderColor = LocalTerraColors.current.inkFaint,
    unfocusedPlaceholderColor = LocalTerraColors.current.inkFaint,
)

@Composable
private fun tagSuggeriti(): List<String> = listOf(
    stringResource(R.string.tag_riproduttore),
    stringResource(R.string.tag_in_vendita),
    stringResource(R.string.tag_quarantena),
    stringResource(R.string.tag_cucciolo),
)
