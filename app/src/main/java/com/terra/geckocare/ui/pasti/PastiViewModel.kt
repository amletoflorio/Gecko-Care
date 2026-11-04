package com.terra.geckocare.ui.pasti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.EsitoPasto
import com.terra.geckocare.data.local.entity.Feeding
import com.terra.geckocare.data.local.entity.FeedingPlanRule
import com.terra.geckocare.data.local.entity.FeedingPlanStep
import com.terra.geckocare.data.repository.DefecationRepository
import com.terra.geckocare.data.repository.FeedingPlanRepository
import com.terra.geckocare.data.repository.FeedingRepository
import com.terra.geckocare.data.repository.GeckoRepository
import com.terra.geckocare.domain.etaInMesi
import com.terra.geckocare.domain.IntegratoriPrevisti
import com.terra.geckocare.domain.formatAttivitaTimestamp
import com.terra.geckocare.domain.formatIntervalloSettimana
import com.terra.geckocare.domain.integratoriPerPasto
import com.terra.geckocare.domain.ordinatePerEta
import com.terra.geckocare.domain.pastiNellaFascia
import com.terra.geckocare.domain.regolaAttuale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
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
    val conMultivitaminico: Boolean,
)

data class FeciUiModel(
    val id: Long,
    val dataLabel: String,
    val normale: Boolean,
    val note: String,
)

/** Una settimana (lun-dom) di un registro paginato, così la lista non cresce all'infinito. */
data class SettimanaUiModel<T>(
    /** 0 = settimana corrente, 1 = scorsa, ... */
    val offset: Int = 0,
    val intervalloLabel: String = "",
    val voci: List<T> = emptyList(),
    val puoAndareIndietro: Boolean = false,
)

data class FasciaUiModel(val rule: FeedingPlanRule, val attuale: Boolean, val schema: List<FeedingPlanStep>)

data class PastiUiState(
    val caricamento: Boolean = true,
    val nomeGecko: String = "",
    val pastiSettimana: Int = 0,
    /** Pasti della settimana con gli integratori previsti dal piano; null se la fascia attuale non ha uno schema. */
    val integratoriRispettati: Int? = null,
    val integratoriTotali: Int = 0,
    val prossimiIntegratori: IntegratoriPrevisti? = null,
    val filtro: FiltroRegistro = FiltroRegistro.TUTTI,
    val pastiPagina: SettimanaUiModel<PastoUiModel> = SettimanaUiModel(),
    val fasce: List<FasciaUiModel> = emptyList(),
    val feciUltimaLabel: String? = null,
    val feciSettimana: Int = 0,
    val feciPagina: SettimanaUiModel<FeciUiModel> = SettimanaUiModel(),
)

class PastiViewModel(
    private val context: android.content.Context,
    private val feedingRepository: FeedingRepository,
    private val feedingPlanRepository: FeedingPlanRepository,
    private val geckoRepository: GeckoRepository,
    private val defecationRepository: DefecationRepository,
) : ViewModel() {

    private val geckoId = MutableStateFlow<Long?>(null)
    private val filtro = MutableStateFlow(FiltroRegistro.TUTTI)
    private val settimanaPastiOffset = MutableStateFlow(0)
    private val settimanaFeciOffset = MutableStateFlow(0)

    fun selezionaGecko(id: Long) {
        if (geckoId.value != id) {
            settimanaPastiOffset.value = 0
            settimanaFeciOffset.value = 0
        }
        geckoId.value = id
    }

    fun settimanaPastiPrecedente() {
        settimanaPastiOffset.value += 1
    }

    fun settimanaPastiSuccessiva() {
        settimanaPastiOffset.value = (settimanaPastiOffset.value - 1).coerceAtLeast(0)
    }

    fun settimanaFeciPrecedente() {
        settimanaFeciOffset.value += 1
    }

    fun settimanaFeciSuccessiva() {
        settimanaFeciOffset.value = (settimanaFeciOffset.value - 1).coerceAtLeast(0)
    }

    fun impostaFiltro(f: FiltroRegistro) {
        filtro.value = f
    }

    private val feedingsFlow = geckoId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else feedingRepository.feedingsForGecko(id)
    }

    private val registroFlow = combine(feedingsFlow, filtro, settimanaPastiOffset) { feedings, f, offset -> Triple(feedings, f, offset) }

    private val rulesFlow = geckoId.flatMapLatest { id ->
        if (id == null) {
            flowOf(emptyList<FeedingPlanRule>() to emptyList())
        } else {
            combine(feedingPlanRepository.rulesForGeckoOrDefault(id), feedingPlanRepository.allSteps()) { r, s -> r to s }
        }
    }

    private val geckoFlow = geckoId.flatMapLatest { id ->
        if (id == null) flowOf(null) else geckoRepository.getById(id)
    }

    private val feciFlow = combine(
        geckoId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else defecationRepository.logsForGecko(id) },
        settimanaFeciOffset,
    ) { feci, offset -> feci to offset }

    val uiState: StateFlow<PastiUiState> = combine(registroFlow, rulesFlow, geckoFlow, feciFlow) { (feedings, filtroAttivo, offsetPasti), (rules, steps), gecko, (feci, offsetFeci) ->
        val inizioSettimana = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val zona = ZoneId.systemDefault()
        val pastiSettimana = feedings.count { it.dataOra.atZone(zona).toLocalDate() >= inizioSettimana }

        // La paginazione guarda tutti i pasti (le frecce non dipendono dal filtro), il filtro si applica dentro la settimana.
        val pastiPagina = paginaSettimana(feedings, offsetPasti, inizioSettimana, zona, { it.dataOra }) { lista ->
            lista.filter {
                when (filtroAttivo) {
                    FiltroRegistro.TUTTI -> true
                    FiltroRegistro.ACCETTATI -> it.esito == EsitoPasto.ACCETTATO
                    FiltroRegistro.RIFIUTATI -> it.esito == EsitoPasto.RIFIUTATO
                    FiltroRegistro.CON_D3 -> it.conD3
                }
            }.map { it.toUiModel() }
        }

        val etaMesi = gecko?.etaInMesi()
        val regolaCorrente = regolaAttuale(rules, etaMesi)
        val fasceOrdinate = rules.ordinatePerEta().map { FasciaUiModel(it, it.id == regolaCorrente?.id, steps.filter { s -> s.ruleId == it.id }) }

        val schemaCorrente = steps.filter { it.ruleId == regolaCorrente?.id }
        val pastiFascia = regolaCorrente?.let { pastiNellaFascia(feedings, gecko, it, zona) }.orEmpty()
        val verificaSettimana = pastiFascia.withIndex()
            .filter { (_, p) -> p.dataOra.atZone(zona).toLocalDate() >= inizioSettimana }
            .mapNotNull { (i, p) -> integratoriPerPasto(schemaCorrente, i)?.rispettatoDa(p) }
        val prossimi = if (regolaCorrente == null) null else integratoriPerPasto(schemaCorrente, pastiFascia.size)

        PastiUiState(
            caricamento = false,
            nomeGecko = gecko?.nome ?: "",
            pastiSettimana = pastiSettimana,
            integratoriRispettati = if (prossimi == null) null else verificaSettimana.count { it },
            integratoriTotali = verificaSettimana.size,
            prossimiIntegratori = prossimi,
            filtro = filtroAttivo,
            pastiPagina = pastiPagina,
            fasce = fasceOrdinate,
            feciUltimaLabel = feci.firstOrNull()?.let { formatAttivitaTimestamp(context, it.dataOra) },
            feciSettimana = feci.count { it.dataOra.atZone(zona).toLocalDate() >= inizioSettimana },
            feciPagina = paginaSettimana(feci, offsetFeci, inizioSettimana, zona, { it.dataOra }) { lista ->
                lista.map { FeciUiModel(it.id, formatAttivitaTimestamp(context, it.dataOra), it.normale, it.note) }
            },
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PastiUiState())

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

    /** [voci] deve essere ordinata per data DESC, come la restituiscono i DAO. */
    private fun <E, T> paginaSettimana(
        voci: List<E>,
        offset: Int,
        inizioSettimanaCorrente: LocalDate,
        zona: ZoneId,
        dataOra: (E) -> java.time.Instant,
        trasforma: (List<E>) -> List<T>,
    ): SettimanaUiModel<T> {
        val inizio = inizioSettimanaCorrente.minusWeeks(offset.toLong())
        val fine = inizio.plusWeeks(1)
        val piuVecchia = voci.lastOrNull()?.let { dataOra(it).atZone(zona).toLocalDate() }
        return SettimanaUiModel(
            offset = offset,
            intervalloLabel = formatIntervalloSettimana(context, inizio),
            voci = trasforma(voci.filter { dataOra(it).atZone(zona).toLocalDate().let { d -> d >= inizio && d < fine } }),
            puoAndareIndietro = piuVecchia != null && piuVecchia < inizio,
        )
    }

    private fun Feeding.toUiModel() = PastoUiModel(
        id = id,
        dataLabel = formatAttivitaTimestamp(context, dataOra),
        cibo = "$quantita $tipoPreda".trim(),
        esito = esito,
        conCalcio = conCalcio,
        conD3 = conD3,
        conMultivitaminico = conMultivitaminico,
    )
}
