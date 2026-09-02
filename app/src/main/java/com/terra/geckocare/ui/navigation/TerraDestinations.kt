package com.terra.geckocare.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import com.terra.geckocare.R

/** Route dei 5 tab principali (bottom bar sempre visibile). */
object TopLevelRoutes {
    const val GECHI = "gechi"
    const val HOME = "home"
    const val PASTI = "pasti"
    const val STATS = "stats"
    const val SPESE = "spese"
}

/** Route secondarie (form, dettagli): raggiungibili in push, bottom bar nascosta. */
object DetailRoutes {
    const val AGGIUNGI_GECKO = "gechi/aggiungi"
    const val MODIFICA_GECKO = "gechi/modifica/{geckoId}"
    const val AGGIUNGI_PASTO = "pasti/aggiungi"
    const val MODIFICA_PASTO = "pasti/modifica/{feedingId}"
    const val MODIFICA_FASCIA_ETA = "pasti/fascia/{ruleId}"
    const val NUOVA_FASCIA_ETA = "pasti/fascia/nuova"
    const val AGGIUNGI_SPESA = "spese/aggiungi"
    const val MODIFICA_SPESA = "spese/modifica/{expenseId}"
    const val AGGIUNGI_NOTA = "home/nota/aggiungi"
    const val AGGIUNGI_AMBIENTE = "home/ambiente/aggiungi"
    const val MODIFICA_AMBIENTE = "home/ambiente/{envLogId}"
    const val AGGIUNGI_MUTA = "home/muta/aggiungi"
    const val MODIFICA_MUTA = "home/muta/{shedLogId}"
    const val AGGIUNGI_CRESCITA = "home/crescita/aggiungi"
    const val MODIFICA_CRESCITA = "home/crescita/{growthLogId}"
    const val NUOVO_PROMEMORIA = "home/promemoria/nuovo"
    const val MODIFICA_PROMEMORIA = "home/promemoria/{reminderId}"
    const val IMPOSTAZIONI = "impostazioni"

    fun modificaGecko(geckoId: Long) = "gechi/modifica/$geckoId"
    fun modificaPasto(feedingId: Long) = "pasti/modifica/$feedingId"
    fun modificaSpesa(expenseId: Long) = "spese/modifica/$expenseId"
    fun modificaAmbiente(envLogId: Long) = "home/ambiente/$envLogId"
    fun modificaMuta(shedLogId: Long) = "home/muta/$shedLogId"
    fun modificaCrescita(growthLogId: Long) = "home/crescita/$growthLogId"
    fun modificaFasciaEta(ruleId: Long) = "pasti/fascia/$ruleId"
    fun modificaPromemoria(reminderId: Long) = "home/promemoria/$reminderId"
}

data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(TopLevelRoutes.GECHI, R.string.nav_gechi, GeckoIcon),
    BottomNavItem(TopLevelRoutes.HOME, R.string.nav_home, Icons.Outlined.Home),
    BottomNavItem(TopLevelRoutes.PASTI, R.string.nav_pasti, Icons.Outlined.Restaurant),
    BottomNavItem(TopLevelRoutes.STATS, R.string.nav_stats, Icons.Outlined.BarChart),
    BottomNavItem(TopLevelRoutes.SPESE, R.string.nav_spese, Icons.Outlined.Payments),
)
