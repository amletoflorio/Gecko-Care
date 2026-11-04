@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.terra.geckocare.ui.pasti

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terra.geckocare.R
import com.terra.geckocare.di.terraViewModel
import com.terra.geckocare.ui.common.DataOraField
import com.terra.geckocare.ui.common.PrimaryButton
import com.terra.geckocare.ui.common.SectionTitle
import com.terra.geckocare.ui.common.TerraCard
import com.terra.geckocare.ui.navigation.GeckoSelectionViewModel
import com.terra.geckocare.ui.theme.LocalTerraColors

@Composable
fun FeciFormScreen(
    selectionViewModel: GeckoSelectionViewModel,
    onDone: () -> Unit,
    onClose: () -> Unit,
    logId: Long? = null,
    viewModel: FeciFormViewModel = terraViewModel(),
) {
    val colors = LocalTerraColors.current
    val state by viewModel.uiState.collectAsState()
    val geckoId by selectionViewModel.selectedGeckoId.collectAsState()
    val modalitaModifica = logId != null

    LaunchedEffect(logId) { logId?.let(viewModel::caricaPerModifica) }
    LaunchedEffect(state.salvataggioCompletato, state.eliminato) {
        if (state.salvataggioCompletato || state.eliminato) onDone()
    }

    Scaffold(
        containerColor = colors.night,
        topBar = {
            TopAppBar(
                title = { Text(if (modalitaModifica) stringResource(R.string.feci_title_modifica) else stringResource(R.string.feci_title_nuovo), color = colors.ink) },
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
            SectionTitle(stringResource(R.string.common_data_ora))
            DataOraField(dataOra = state.dataOra, onChange = viewModel::onDataOraChange)

            SectionTitle(stringResource(R.string.feci_aspetto))
            TerraCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.feci_normale_label), color = colors.ink, fontSize = 13.5.sp)
                        Text(stringResource(R.string.feci_normale_hint), color = colors.inkFaint, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    Switch(
                        checked = state.normale,
                        onCheckedChange = viewModel::onNormaleChange,
                        colors = SwitchDefaults.colors(checkedTrackColor = colors.teal, checkedThumbColor = colors.night),
                    )
                }
            }

            SectionTitle(stringResource(R.string.feci_note))
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text(stringResource(R.string.pasto_note_placeholder)) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.teal, unfocusedBorderColor = colors.line),
            )

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = if (modalitaModifica) stringResource(R.string.feci_salva_modifiche) else stringResource(R.string.feci_salva),
                enabled = geckoId != null,
                onClick = { geckoId?.let(viewModel::salva) },
            )

            if (modalitaModifica) {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = viewModel::elimina, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.feci_elimina), color = colors.danger)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
