package com.terra.geckocare.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terra.geckocare.di.terraViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.terra.geckocare.R
import com.terra.geckocare.ui.gechi.AggiungiGeckoScreen
import com.terra.geckocare.ui.gechi.GechiScreen
import com.terra.geckocare.ui.home.AggiungiNotaScreen
import com.terra.geckocare.ui.home.AmbienteFormScreen
import com.terra.geckocare.ui.home.CrescitaFormScreen
import com.terra.geckocare.ui.home.HomeScreen
import com.terra.geckocare.ui.home.MutaFormScreen
import com.terra.geckocare.ui.home.PromemoriaFormScreen
import com.terra.geckocare.ui.navigation.DetailRoutes
import com.terra.geckocare.ui.navigation.GeckoSelectionViewModel
import com.terra.geckocare.ui.navigation.TopLevelRoutes
import com.terra.geckocare.ui.navigation.bottomNavItems
import com.terra.geckocare.ui.pasti.AggiungiPastoScreen
import com.terra.geckocare.ui.pasti.FasciaEtaFormScreen
import com.terra.geckocare.ui.pasti.PastiScreen
import com.terra.geckocare.ui.settings.ImpostazioniScreen
import com.terra.geckocare.ui.spese.AggiungiSpesaScreen
import com.terra.geckocare.ui.spese.SpeseScreen
import com.terra.geckocare.ui.stats.StatsScreen
import com.terra.geckocare.ui.theme.LocalTerraColors

@Composable
fun TerraApp() {
    val navController = rememberNavController()
    val selectionViewModel: GeckoSelectionViewModel = terraViewModel()
    val colors = LocalTerraColors.current
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concesso = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!concesso) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTopLevel = bottomNavItems.any { it.route == currentRoute }

    val gechiAttivi by selectionViewModel.gechiAttivi.collectAsState()
    val scanNonRiconosciuto = stringResource(R.string.gechi_scan_non_riconosciuto)
    val scanPrompt = stringResource(R.string.gechi_scan_prompt)
    var schedaImportataTesto by remember { mutableStateOf<String?>(null) }

    fun vaiAllaSchedaGecko(id: Long) {
        selectionViewModel.select(id)
        navController.navigate(TopLevelRoutes.HOME) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { risultato ->
        val contenuto = risultato.contents ?: return@rememberLauncherForActivityResult
        val righe = contenuto.lines()
        val id = Regex("GECKO-0*(\\d+)").find(contenuto)?.groupValues?.get(1)?.toLongOrNull()
        val trovato = id != null && gechiAttivi.any { it.id == id }
        when {
            trovato -> vaiAllaSchedaGecko(id!!)
            righe.size > 1 -> schedaImportataTesto = righe.drop(1).joinToString("\n")
            else -> android.widget.Toast.makeText(context, scanNonRiconosciuto, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    schedaImportataTesto?.let { testo ->
        AlertDialog(
            onDismissRequest = { schedaImportataTesto = null },
            title = { Text(stringResource(R.string.gechi_scan_scheda_importata_titolo)) },
            text = { Text(testo) },
            confirmButton = {
                TextButton(onClick = { schedaImportataTesto = null }) {
                    Text(stringResource(R.string.common_chiudi))
                }
            },
        )
    }

    Scaffold(
        containerColor = colors.night,
        bottomBar = {
            AnimatedVisibility(visible = isTopLevel) {
                NavigationBar(containerColor = colors.night.copy(alpha = 0.96f), tonalElevation = 0.dp) {
                    bottomNavItems.forEach { item ->
                        val selected = backStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                            label = { Text(stringResource(item.labelRes), fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.teal,
                                selectedTextColor = colors.teal,
                                unselectedIconColor = colors.inkFaint,
                                unselectedTextColor = colors.inkFaint,
                                indicatorColor = colors.tealDim,
                            ),
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (currentRoute) {
                TopLevelRoutes.PASTI -> FloatingActionButton(
                    onClick = { navController.navigate(DetailRoutes.AGGIUNGI_PASTO) },
                    containerColor = colors.teal,
                    contentColor = colors.onAccent,
                ) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.fab_add_pasto)) }

                TopLevelRoutes.SPESE -> FloatingActionButton(
                    onClick = { navController.navigate(DetailRoutes.AGGIUNGI_SPESA) },
                    containerColor = colors.teal,
                    contentColor = colors.onAccent,
                ) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.fab_add_spesa)) }

                TopLevelRoutes.GECHI -> FloatingActionButton(
                    onClick = { navController.navigate(DetailRoutes.AGGIUNGI_GECKO) },
                    containerColor = colors.teal,
                    contentColor = colors.onAccent,
                ) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.fab_add_gecko)) }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = TopLevelRoutes.GECHI,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(180)) },
                exitTransition = { fadeOut(animationSpec = tween(180)) },
                popEnterTransition = { fadeIn(animationSpec = tween(180)) },
                popExitTransition = { fadeOut(animationSpec = tween(180)) },
            ) {
            composable(TopLevelRoutes.GECHI) {
                GechiScreen(
                    onGeckoClick = ::vaiAllaSchedaGecko,
                    onAggiungiGecko = { navController.navigate(DetailRoutes.AGGIUNGI_GECKO) },
                )
            }
            composable(TopLevelRoutes.HOME) {
                HomeScreen(
                    selectionViewModel = selectionViewModel,
                    onModificaGecko = { id -> navController.navigate(DetailRoutes.modificaGecko(id)) },
                    onAggiungiNota = { navController.navigate(DetailRoutes.AGGIUNGI_NOTA) },
                    onAggiungiAmbiente = { navController.navigate(DetailRoutes.AGGIUNGI_AMBIENTE) },
                    onModificaAmbiente = { id -> navController.navigate(DetailRoutes.modificaAmbiente(id)) },
                    onAggiungiMuta = { navController.navigate(DetailRoutes.AGGIUNGI_MUTA) },
                    onModificaMuta = { id -> navController.navigate(DetailRoutes.modificaMuta(id)) },
                    onAggiungiCrescita = { navController.navigate(DetailRoutes.AGGIUNGI_CRESCITA) },
                    onModificaCrescita = { id -> navController.navigate(DetailRoutes.modificaCrescita(id)) },
                    onNuovoPromemoria = { navController.navigate(DetailRoutes.NUOVO_PROMEMORIA) },
                    onModificaPromemoria = { id -> navController.navigate(DetailRoutes.modificaPromemoria(id)) },
                )
            }
            composable(TopLevelRoutes.PASTI) {
                PastiScreen(
                    selectionViewModel = selectionViewModel,
                    onAggiungiPasto = { navController.navigate(DetailRoutes.AGGIUNGI_PASTO) },
                    onModificaPasto = { id -> navController.navigate(DetailRoutes.modificaPasto(id)) },
                    onModificaFasciaEta = { id -> navController.navigate(DetailRoutes.modificaFasciaEta(id)) },
                    onNuovaFasciaEta = { navController.navigate(DetailRoutes.NUOVA_FASCIA_ETA) },
                )
            }
            composable(TopLevelRoutes.STATS) {
                StatsScreen(selectionViewModel = selectionViewModel)
            }
            composable(TopLevelRoutes.SPESE) {
                SpeseScreen(
                    selectionViewModel = selectionViewModel,
                    onAggiungiSpesa = { navController.navigate(DetailRoutes.AGGIUNGI_SPESA) },
                    onModificaSpesa = { id -> navController.navigate(DetailRoutes.modificaSpesa(id)) },
                )
            }

            composable(DetailRoutes.AGGIUNGI_GECKO) {
                AggiungiGeckoScreen(onDone = { navController.popBackStack() }, onClose = { navController.popBackStack() })
            }
            composable(DetailRoutes.MODIFICA_GECKO) { entry ->
                val geckoId = entry.arguments?.let { navBackStackEntryLongArg(it, "geckoId") }
                AggiungiGeckoScreen(
                    onDone = { navController.popBackStack() },
                    onClose = { navController.popBackStack() },
                    geckoIdDaModificare = geckoId,
                )
            }
            composable(DetailRoutes.AGGIUNGI_PASTO) {
                AggiungiPastoScreen(selectionViewModel, onDone = { navController.popBackStack() }, onClose = { navController.popBackStack() })
            }
            composable(DetailRoutes.MODIFICA_PASTO) { entry ->
                val feedingId = entry.arguments?.let { navBackStackEntryLongArg(it, "feedingId") }
                AggiungiPastoScreen(
                    selectionViewModel,
                    onDone = { navController.popBackStack() },
                    onClose = { navController.popBackStack() },
                    feedingId = feedingId,
                )
            }
            composable(DetailRoutes.NUOVA_FASCIA_ETA) {
                FasciaEtaFormScreen(
                    ruleId = null,
                    geckoId = selectionViewModel.selectedGeckoId.value,
                    onDone = { navController.popBackStack() },
                    onClose = { navController.popBackStack() },
                )
            }
            composable(DetailRoutes.MODIFICA_FASCIA_ETA) { entry ->
                val ruleId = entry.arguments?.let { navBackStackEntryLongArg(it, "ruleId") }
                FasciaEtaFormScreen(
                    ruleId = ruleId,
                    geckoId = selectionViewModel.selectedGeckoId.value,
                    onDone = { navController.popBackStack() },
                    onClose = { navController.popBackStack() },
                )
            }
            composable(DetailRoutes.AGGIUNGI_SPESA) {
                AggiungiSpesaScreen(selectionViewModel, onDone = { navController.popBackStack() }, onClose = { navController.popBackStack() })
            }
            composable(DetailRoutes.MODIFICA_SPESA) { entry ->
                val expenseId = entry.arguments?.let { navBackStackEntryLongArg(it, "expenseId") }
                AggiungiSpesaScreen(
                    selectionViewModel,
                    onDone = { navController.popBackStack() },
                    onClose = { navController.popBackStack() },
                    expenseId = expenseId,
                )
            }
            composable(DetailRoutes.AGGIUNGI_NOTA) {
                AggiungiNotaScreen(selectionViewModel, onDone = { navController.popBackStack() }, onClose = { navController.popBackStack() })
            }
            composable(DetailRoutes.AGGIUNGI_AMBIENTE) {
                AmbienteFormScreen(selectionViewModel, onDone = { navController.popBackStack() }, onClose = { navController.popBackStack() })
            }
            composable(DetailRoutes.MODIFICA_AMBIENTE) { entry ->
                val envLogId = entry.arguments?.let { navBackStackEntryLongArg(it, "envLogId") }
                AmbienteFormScreen(
                    selectionViewModel,
                    onDone = { navController.popBackStack() },
                    onClose = { navController.popBackStack() },
                    envLogId = envLogId,
                )
            }
            composable(DetailRoutes.AGGIUNGI_MUTA) {
                MutaFormScreen(selectionViewModel, onDone = { navController.popBackStack() }, onClose = { navController.popBackStack() })
            }
            composable(DetailRoutes.MODIFICA_MUTA) { entry ->
                val shedLogId = entry.arguments?.let { navBackStackEntryLongArg(it, "shedLogId") }
                MutaFormScreen(
                    selectionViewModel,
                    onDone = { navController.popBackStack() },
                    onClose = { navController.popBackStack() },
                    shedLogId = shedLogId,
                )
            }
            composable(DetailRoutes.AGGIUNGI_CRESCITA) {
                CrescitaFormScreen(selectionViewModel, onDone = { navController.popBackStack() }, onClose = { navController.popBackStack() })
            }
            composable(DetailRoutes.MODIFICA_CRESCITA) { entry ->
                val growthLogId = entry.arguments?.let { navBackStackEntryLongArg(it, "growthLogId") }
                CrescitaFormScreen(
                    selectionViewModel,
                    onDone = { navController.popBackStack() },
                    onClose = { navController.popBackStack() },
                    growthLogId = growthLogId,
                )
            }
            composable(DetailRoutes.NUOVO_PROMEMORIA) {
                PromemoriaFormScreen(null, selectionViewModel, onDone = { navController.popBackStack() }, onClose = { navController.popBackStack() })
            }
            composable(DetailRoutes.MODIFICA_PROMEMORIA) { entry ->
                val reminderId = entry.arguments?.let { navBackStackEntryLongArg(it, "reminderId") }
                PromemoriaFormScreen(reminderId, selectionViewModel, onDone = { navController.popBackStack() }, onClose = { navController.popBackStack() })
            }
            composable(DetailRoutes.IMPOSTAZIONI) {
                ImpostazioniScreen(onClose = { navController.popBackStack() })
            }
            }

            AnimatedVisibility(visible = isTopLevel, modifier = Modifier.align(Alignment.TopEnd)) {
                Row(
                    modifier = Modifier.padding(top = 44.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    IconButton(
                        onClick = {
                            scanLauncher.launch(
                                ScanOptions()
                                    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                    .setPrompt(scanPrompt)
                                    .setBeepEnabled(false)
                                    .setOrientationLocked(true),
                            )
                        },
                    ) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = stringResource(R.string.gechi_scan_desc), tint = colors.inkFaint)
                    }
                    IconButton(onClick = { navController.navigate(DetailRoutes.IMPOSTAZIONI) }) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.gechi_impostazioni_desc), tint = colors.inkFaint)
                    }
                }
            }
        }
    }
}

private fun navBackStackEntryLongArg(bundle: android.os.Bundle, key: String): Long? =
    bundle.getString(key)?.toLongOrNull()
