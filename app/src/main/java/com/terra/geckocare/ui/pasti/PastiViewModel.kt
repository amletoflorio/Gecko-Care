package com.terra.geckocare.ui.pasti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.EsitoPasto
import com.terra.geckocare.data.local.entity.Feeding
import com.terra.geckocare.data.local.entity.FeedingPlanRule
import com.terra.geckocare.data.repository.FeedingPlanRepository
import com.terra.geckocare.data.repository.FeedingRepository
import com.terra.geckocare.data.repository.GeckoRepository
import com.terra.geckocare.domain.etaInGiorni
import com.terra.geckocare.domain.formatAttivitaTimestamp
import com.terra.geckocare.domain.ordinatePerEta
import com.terra.geckocare.domain.regolaAttuale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class FiltroRegistro { TUTTI, ACCETTATI, RIFIUTATI, CON_D3 }

data class PastoUiModel(
    val id: Long,
    val dataLabel: String,
    val cibo: String,
    val esito: EsitoPasto,
    val conCalcio: Boolean,
    val conD3: Boolean,
)

data class FasciaUiModel(val rule: FeedingPlanRule, val attuale: Boolean)

data class PastiUiState(
    val caricamento: Boolean = true,
    val nomeGecko: String = "",
    val pastiSettimana: Int = 0,
    val calcioD3Rispettati: Int = 0,
    val calcioD3Totali: Int = 0,
    val filtro: FiltroRegistro = FiltroRegistro.TUTTI,
    val pastiFiltrati: List<PastoUiModel> = emptyList(),
    val fasce: List<FasciaUiModel> = emptyList(),
)

class PastiViewModel(
    private val context: android.content.Context,
    private val feedingRepository: FeedingRepository,
    private val feedingPlanRepository: FeedingPlanRepository,
    private val geckoRepository: GeckoRepository,
) : ViewModel() {

    private val geckoId = MutableStateFlow<Long?>(null)
    private val filtro = MutableStateFlow(FiltroRegistro.TUTTI)

    fun selezionaGecko(id: Long) {
        geckoId.value = id
    }

    fun impostaFiltro(f: FiltroRegistro) {
        filtro.value = f
    }

    private val feedingsFlow = geckoId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else feedingRepository.feedingsForGecko(id)
    }

    private val rulesFlow = geckoId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else feedingPlanRepository.rulesForGeckoOrDefault(id)
    }

    private val geckoFlow = geckoId.flatMapLatest { id ->
        if (id == null) flowOf(null) else geckoRepository.getById(id)
    }

    val uiState: StateFlow<PastiUiState> = combine(feedingsFlow, rulesFlow, geckoFlow, filtro) { feedings, rules, gecko, filtroAttivo ->
        val inizioSettimana = java.time.LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val zona = ZoneId.systemDefault()
        val pastiSettimana = feedings.count { it.dataOra.atZone(zona).toLocalDate() >= inizioSettimana }
        val settimanaFeedings = feedings.filter { it.dataOra.atZone(zona).toLocalDate() >= inizioSettimana }
        val calcioD3Rispettati = settimanaFeedings.count { it.conCalcio && it.conD3 }

        val filtrati = feedings.filter {
            when (filtroAttivo) {
                FiltroRegistro.TUTTI -> true
                FiltroRegistro.ACCETTATI -> it.esito == EsitoPasto.ACCETTATO
                FiltroRegistro.RIFIUTATI -> it.esito == EsitoPasto.RIFIUTATO
                FiltroRegistro.CON_D3 -> it.conD3
            }
        }.map { it.toUiModel() }

        val etaGiorni = gecko?.etaInGiorni()
        val regolaCorrente = regolaAttuale(rules, etaGiorni)
        val fasceOrdinate = rules.ordinatePerEta().map { FasciaUiModel(it, it.id == regolaCorrente?.id) }

        PastiUiState(
            caricamento = false,
            nomeGecko = gecko?.nome ?: "",
            pastiSettimana = pastiSettimana,
            calcioD3Rispettati = calcioD3Rispettati,
            calcioD3Totali = settimanaFeedings.size,
            filtro = filtroAttivo,
            pastiFiltrati = filtrati,
            fasce = fasceOrdinate,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PastiUiState())

    fun toggleCalcioD3Pasto(pastoId: Long) {
        viewModelScope.launch {
            val id = geckoId.value ?: return@launch
            val lista = feedingRepository.feedingsForGecko(id).first()
            val pasto = lista.find { it.id == pastoId } ?: return@launch
            val nuovo = if (pasto.conCalcio && pasto.conD3) {
                pasto.copy(conCalcio = false, conD3 = false)
            } else {
                pasto.copy(conCalcio = true, conD3 = true)
            }
            feedingRepository.update(nuovo)
        }
    }

    private fun Feeding.toUiModel() = PastoUiModel(
        id = id,
        dataLabel = formatAttivitaTimestamp(context, dataOra),
        cibo = "$quantita $tipoPreda".trim(),
        esito = esito,
        conCalcio = conCalcio,
        conD3 = conD3,
    )
}
