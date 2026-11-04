package com.terra.geckocare.ui.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.repository.DefecationRepository
import com.terra.geckocare.data.repository.ExpenseRepository
import com.terra.geckocare.data.repository.FeedingRepository
import com.terra.geckocare.data.repository.GeckoRepository
import com.terra.geckocare.data.repository.GrowthRepository
import com.terra.geckocare.data.repository.ShedRepository
import com.terra.geckocare.data.local.entity.EsitoPasto
import com.terra.geckocare.domain.buildCsvExport
import com.terra.geckocare.domain.intervalloMedioMuta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class StatsUiState(
    val caricamento: Boolean = true,
    val nomeGecko: String = "",
    val pesoPunti: List<Pair<LocalDate, Double>> = emptyList(),
    val pastiSettimanali: List<Int> = emptyList(),
    val intervalloMedioMutaLabel: String = "—",
    val crescitaUltimoMeseLabel: String = "—",
    val tassoRifiutoLabel: String = "—",
)

class StatsViewModel(
    private val context: Context,
    private val growthRepository: GrowthRepository,
    private val feedingRepository: FeedingRepository,
    private val shedRepository: ShedRepository,
    private val expenseRepository: ExpenseRepository,
    private val geckoRepository: GeckoRepository,
    private val defecationRepository: DefecationRepository,
) : ViewModel() {

    private val geckoId = MutableStateFlow<Long?>(null)

    fun selezionaGecko(id: Long) {
        geckoId.value = id
    }

    private val zona = ZoneId.systemDefault()

    val uiState: StateFlow<StatsUiState> = geckoId.flatMapLatest { id ->
        if (id == null) {
            flowOf(StatsUiState(caricamento = false))
        } else {
            combine(
                geckoRepository.getById(id),
                growthRepository.logsForGecko(id),
                feedingRepository.feedingsForGecko(id),
                shedRepository.logsForGecko(id),
            ) { gecko, growth, feedings, sheds ->
                val oggi = LocalDate.now()
                val pesoPunti = growth.mapNotNull { log -> log.pesoGrammi?.let { log.dataOra.atZone(zona).toLocalDate() to it } }
                    .sortedBy { it.first }

                val settimane = MutableList(5) { 0 }
                feedings.forEach { f ->
                    val giorniFa = ChronoUnit.DAYS.between(f.dataOra.atZone(zona).toLocalDate(), oggi)
                    if (giorniFa in 0..34) {
                        val idx = 4 - (giorniFa / 7).toInt()
                        if (idx in 0..4) settimane[idx] = settimane[idx] + 1
                    }
                }

                val mutaCompleta = sheds.filter { it.completa }
                val intervalloMuta = intervalloMedioMuta(mutaCompleta)

                val pesoUltimi30gg = pesoPunti.filter { ChronoUnit.DAYS.between(it.first, oggi) <= 30 }
                val crescita = if (pesoUltimi30gg.size >= 2) pesoUltimi30gg.last().second - pesoUltimi30gg.first().second else null

                val totalePasti = feedings.size
                val rifiutati = feedings.count { it.esito == EsitoPasto.RIFIUTATO }

                StatsUiState(
                    caricamento = false,
                    nomeGecko = gecko?.nome ?: "",
                    pesoPunti = pesoPunti,
                    pastiSettimanali = settimane,
                    intervalloMedioMutaLabel = intervalloMuta?.let { context.getString(com.terra.geckocare.R.string.stats_giorni_suffix, it) } ?: "—",
                    crescitaUltimoMeseLabel = crescita?.let { "%+.1f g".format(it) } ?: "—",
                    tassoRifiutoLabel = if (totalePasti > 0) context.getString(com.terra.geckocare.R.string.stats_rifiutati_su, rifiutati, totalePasti) else "—",
                )
            }
        }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    /** Esporta i dati del solo animale corrente in un CSV in cache, pronto per la condivisione. */
    suspend fun esportaCsvGeckoCorrente(): File? {
        val id = geckoId.value ?: return null
        val gecko = geckoRepository.getByIdOnce(id) ?: return null
        val feedings = feedingRepository.feedingsForGecko(id).first()
        val growth = growthRepository.logsForGecko(id).first()
        val sheds = shedRepository.logsForGecko(id).first()
        val expenses = expenseRepository.expensesForGecko(id).first()
        val feci = defecationRepository.logsForGecko(id).first()
        val csv = buildCsvExport(mapOf(id to gecko.nome), feedings, growth, sheds, expenses, feci)
        return scriviFileCsv(csv, "terra_${gecko.nome}.csv")
    }

    /** Esporta i dati di tutto l'allevamento in un unico CSV in cache. */
    suspend fun esportaCsvAllevamento(): File {
        val gechi = geckoRepository.getAll().first()
        val nomi = gechi.associate { it.id to it.nome }
        val feedings = mutableListOf<com.terra.geckocare.data.local.entity.Feeding>()
        val growth = mutableListOf<com.terra.geckocare.data.local.entity.GrowthLog>()
        val sheds = mutableListOf<com.terra.geckocare.data.local.entity.ShedLog>()
        val feci = mutableListOf<com.terra.geckocare.data.local.entity.DefecationLog>()
        gechi.forEach { g ->
            feedings += feedingRepository.feedingsForGecko(g.id).first()
            growth += growthRepository.logsForGecko(g.id).first()
            sheds += shedRepository.logsForGecko(g.id).first()
            feci += defecationRepository.logsForGecko(g.id).first()
        }
        val expenses = expenseRepository.expensesAll().first()
        val csv = buildCsvExport(nomi, feedings, growth, sheds, expenses, feci)
        return scriviFileCsv(csv, "terra_allevamento.csv")
    }

    private suspend fun scriviFileCsv(contenuto: String, nomeFile: String): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, nomeFile)
        file.writeText(contenuto)
        file
    }
}
