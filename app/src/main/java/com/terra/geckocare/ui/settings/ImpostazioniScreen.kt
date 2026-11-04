@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.terra.geckocare.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.terra.geckocare.R
import com.terra.geckocare.di.terraViewModel
import com.terra.geckocare.data.settings.AppCurrency
import com.terra.geckocare.data.settings.AppLanguage
import com.terra.geckocare.data.settings.ThemeMode
import com.terra.geckocare.ui.common.GhostButton
import com.terra.geckocare.ui.common.SectionTitle
import com.terra.geckocare.ui.common.SegmentedControl
import com.terra.geckocare.ui.common.TerraCard
import com.terra.geckocare.ui.theme.LocalTerraColors
import java.text.DateFormat
import java.util.Date

@Composable
fun ImpostazioniScreen(
    onClose: () -> Unit,
    viewModel: SettingsViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val appCurrency by viewModel.appCurrency.collectAsState()
    val backupSettings by viewModel.backupSettings.collectAsState()

    val messaggioBackupOk = stringResource(R.string.settings_backup_evento_ok)
    val messaggioBackupErrore = stringResource(R.string.settings_backup_evento_errore)
    val messaggioRestoreOk = stringResource(R.string.settings_restore_evento_ok)
    val messaggioRestoreErrore = stringResource(R.string.settings_restore_evento_errore)

    var mostraConfermaRestore by remember { mutableStateOf(false) }
    var uriRestoreScelto by remember { mutableStateOf<android.net.Uri?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventi.collect { evento ->
            when (evento) {
                is BackupEvent.BackupOk -> android.widget.Toast.makeText(context, messaggioBackupOk, android.widget.Toast.LENGTH_SHORT).show()
                is BackupEvent.BackupErrore -> android.widget.Toast.makeText(context, messaggioBackupErrore, android.widget.Toast.LENGTH_LONG).show()
                is BackupEvent.RestorePronto -> {
                    android.widget.Toast.makeText(context, messaggioRestoreOk, android.widget.Toast.LENGTH_SHORT).show()
                    riavviaApp(context)
                }
                is BackupEvent.RestoreErrore -> android.widget.Toast.makeText(context, messaggioRestoreErrore, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    val sceltaCartella = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let(viewModel::onCartellaBackupScelta)
    }
    val sceltaFileRestore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            uriRestoreScelto = uri
            mostraConfermaRestore = true
        }
    }

    if (mostraConfermaRestore) {
        AlertDialog(
            onDismissRequest = { mostraConfermaRestore = false },
            title = { Text(stringResource(R.string.settings_restore_conferma_titolo)) },
            text = { Text(stringResource(R.string.settings_restore_conferma_testo)) },
            confirmButton = {
                TextButton(onClick = {
                    mostraConfermaRestore = false
                    uriRestoreScelto?.let(viewModel::ripristinaDaFile)
                }) {
                    Text(stringResource(R.string.settings_restore_conferma_conferma), color = colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostraConfermaRestore = false }) {
                    Text(stringResource(R.string.settings_restore_conferma_annulla))
                }
            },
        )
    }

    Scaffold(
        containerColor = colors.night,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), color = colors.ink) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.common_indietro), tint = colors.ink)
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
                .padding(horizontal = 20.dp),
        ) {
            SectionTitle(stringResource(R.string.settings_aspetto))
            TerraCard(padding = PaddingValues(6.dp)) {
                ThemeMode.entries.forEach { mode ->
                    SettingsOptionRow(
                        label = when (mode) {
                            ThemeMode.SISTEMA -> stringResource(R.string.settings_tema_sistema)
                            ThemeMode.CHIARO -> stringResource(R.string.settings_tema_chiaro)
                            ThemeMode.SCURO -> stringResource(R.string.settings_tema_scuro)
                        },
                        selected = themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                    )
                }
            }

            SectionTitle(stringResource(R.string.settings_lingua))
            TerraCard(padding = PaddingValues(6.dp)) {
                AppLanguage.entries.forEach { lang ->
                    SettingsOptionRow(
                        label = when (lang) {
                            AppLanguage.SISTEMA -> stringResource(R.string.settings_lingua_sistema)
                            AppLanguage.ITALIANO -> stringResource(R.string.settings_lingua_it)
                            AppLanguage.INGLESE -> stringResource(R.string.settings_lingua_en)
                        },
                        selected = appLanguage == lang,
                        onClick = {
                            viewModel.setAppLanguage(lang)
                            (context as? android.app.Activity)?.recreate()
                        },
                    )
                }
            }

            SectionTitle(stringResource(R.string.settings_valuta))
            TerraCard(padding = PaddingValues(6.dp)) {
                AppCurrency.entries.forEach { currency ->
                    SettingsOptionRow(
                        label = currency.isoCode ?: stringResource(R.string.settings_valuta_automatica),
                        selected = appCurrency == currency,
                        onClick = { viewModel.setAppCurrency(currency) },
                    )
                }
            }

            SectionTitle(stringResource(R.string.settings_backup))
            TerraCard {
                val nomeCartella = remember(backupSettings.folderUri) {
                    backupSettings.folderUri?.let { runCatching { DocumentFile.fromTreeUri(context, android.net.Uri.parse(it))?.name }.getOrNull() }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sceltaCartella.launch(null) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_backup_cartella_titolo), color = colors.ink, fontSize = 13.5.sp)
                        Text(
                            nomeCartella ?: stringResource(R.string.settings_backup_cartella_non_impostata),
                            color = colors.inkDim,
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(stringResource(R.string.settings_backup_scegli_cartella), color = colors.teal, fontSize = 12.5.sp)
                }

                if (backupSettings.folderUri != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_backup_attivo), color = colors.ink, fontSize = 13.5.sp)
                            Text(stringResource(R.string.settings_backup_attivo_hint), color = colors.inkFaint, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                        Switch(
                            checked = backupSettings.enabled,
                            onCheckedChange = viewModel::setBackupEnabled,
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.teal, checkedThumbColor = colors.night),
                        )
                    }

                    if (backupSettings.enabled) {
                        SegmentedControl(
                            options = listOf(stringResource(R.string.settings_backup_frequenza_giornaliero), stringResource(R.string.settings_backup_frequenza_settimanale)),
                            selectedIndex = if (backupSettings.frequencyDays >= 7) 1 else 0,
                            onSelected = { index -> viewModel.setBackupFrequencyDays(if (index == 1) 7 else 1) },
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }

                    val statoTesto = when {
                        backupSettings.lastRunTimestamp == null -> stringResource(R.string.settings_backup_mai)
                        backupSettings.lastRunOk == true -> stringResource(R.string.settings_backup_ultimo_ok, formattaData(backupSettings.lastRunTimestamp!!))
                        else -> stringResource(R.string.settings_backup_ultimo_errore, formattaData(backupSettings.lastRunTimestamp!!))
                    }
                    Text(statoTesto, color = colors.inkFaint, fontSize = 11.sp, modifier = Modifier.padding(top = 14.dp))

                    GhostButton(
                        text = stringResource(R.string.settings_backup_ora),
                        onClick = viewModel::backupOra,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }

            SectionTitle(stringResource(R.string.settings_restore))
            TerraCard {
                Text(stringResource(R.string.settings_restore_hint), color = colors.inkDim, style = MaterialTheme.typography.bodySmall)
                GhostButton(
                    text = stringResource(R.string.settings_restore_bottone),
                    onClick = { sceltaFileRestore.launch(arrayOf("application/zip")) },
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            SectionTitle(stringResource(R.string.settings_info))
            TerraCard {
                Text(
                    stringResource(R.string.settings_info_testo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkDim,
                )
            }
        }
    }
}

@Composable
private fun SettingsOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalTerraColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = colors.ink, style = MaterialTheme.typography.bodyLarge)
        if (selected) {
            Row(
                modifier = Modifier
                    .size(22.dp)
                    .background(colors.teal, CircleShape),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier
                        .padding(3.dp)
                        .size(16.dp),
                )
            }
        }
    }
}

private fun formattaData(timestampMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timestampMillis))

private fun riavviaApp(context: android.content.Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
    } ?: return
    context.startActivity(intent)
    Runtime.getRuntime().exit(0)
}
