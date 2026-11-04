package com.terra.geckocare.ui.pasti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.EsitoPasto
import com.terra.geckocare.data.local.entity.Feeding
import com.terra.geckocare.data.repository.FeedingPlanRepository
import com.terra.geckocare.data.repository.FeedingRepository
import com.terra.geckocare.data.repository.GeckoRepository
import com.terra.geckocare.domain.IntegratoriPrevisti
import com.terra.geckocare.domain.etaInMesi
import com.terra.geckocare.domain.prossimiIntegratori
import com.terra.geckocare.domain.regolaAttuale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

/** Una riga preda del form: un pasto può essere composto da più specie (es. tarme + dubia). */
data class PredaRiga(val tipo: String = "", val quantita: Int = 5)

data class AggiungiPastoUiState(
    val feedingIdInModifica: Long? = null,
    val dataOra: Instant = Instant.now(),
    val prede: List<PredaRiga> = listOf(PredaRiga()),
    val esito: EsitoPasto = EsitoPasto.ACCETTATO,
    val conCalcio: Boolean = true,
    val conD3: Boolean = true,
    val conMultivitaminico: Boolean = false,
    val note: String = "",
    /** Integratori che il piano prevede per questo pasto (solo nuovi pasti), null se non c'è uno schema. */
    val previstoDalPiano: IntegratoriPrevisti? = null,
    val salvataggioCompletato: Boolean = false,
    val eliminato: Boolean = false,
)

class AggiungiPastoViewModel(
    private val context: android.content.Context,
    private val feedingRepository: FeedingRepository,
    private val feedingPlanRepository: FeedingPlanRepository,
    private val geckoRepository: GeckoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AggiungiPastoUiState())
    val uiState: StateFlow<AggiungiPastoUiState> = _uiState.asStateFlow()

    private var originale: Feeding? = null

    fun caricaPerModifica(feedingId: Long) {
        viewModelScope.launch {
            val f = feedingRepository.getById(feedingId) ?: return@launch
            originale = f
            update {
                it.copy(
                    feedingIdInModifica = f.id,
                    dataOra = f.dataOra,
                    prede = listOf(PredaRiga(f.tipoPreda, f.quantita)),
                    esito = f.esito,
                    conCalcio = f.conCalcio,
                    conD3 = f.conD3,
                    conMultivitaminico = f.conMultivitaminico,
                    note = f.note,
                )
            }
        }
    }

    /** Nuovo pasto: preseleziona insetti e integratori che lo schema della fascia attuale prevede per il prossimo pasto. */
    fun precompilaDaPiano(geckoId: Long) {
        viewModelScope.launch {
            val gecko = geckoRepository.getByIdOnce(geckoId) ?: return@launch
            val regola = regolaAttuale(feedingPlanRepository.rulesForGeckoOrDefault(geckoId).first(), gecko.etaInMesi())
            val schema = regola?.let { feedingPlanRepository.stepsForRuleOnce(it.id) }.orEmpty()
            val previsto = prossimiIntegratori(feedingRepository.feedingsForGecko(geckoId).first(), gecko, regola, schema)
                ?: return@launch
            if (_uiState.value.feedingIdInModifica != null) return@launch
            update {
                // Insetti del piano proposti solo se l'utente non ha ancora compilato le prede.
                val predeVuote = it.prede.all { r -> r.tipo.isBlank() }
                it.copy(
                    prede = if (predeVuote && previsto.insetti.isNotEmpty()) previsto.insetti.map { tipo -> PredaRiga(tipo) } else it.prede,
                    previstoDalPiano = previsto,
                    conCalcio = previsto.conCalcio,
                    conD3 = previsto.conD3,
                    conMultivitaminico = previsto.conMultivitaminico,
                )
            }
        }
    }

    fun aggiungiPreda() = update { it.copy(prede = it.prede + PredaRiga()) }

    fun rimuoviPreda(index: Int) = update { st ->
        st.copy(prede = st.prede.filterIndexed { i, _ -> i != index }.ifEmpty { listOf(PredaRiga()) })
    }

    fun onTipoPredaChange(index: Int, v: String) = update { st ->
        st.copy(prede = st.prede.mapIndexed { i, r -> if (i == index) r.copy(tipo = v) else r })
    }

    fun onQuantitaChange(index: Int, v: Int) = update { st ->
        st.copy(prede = st.prede.mapIndexed { i, r -> if (i == index) r.copy(quantita = v.coerceAtLeast(1)) else r })
    }

    fun onDataOraChange(v: Instant) = update { it.copy(dataOra = v) }
    fun onEsitoChange(v: EsitoPasto) = update { it.copy(esito = v) }
    fun onCalcioChange(v: Boolean) = update { it.copy(conCalcio = v) }
    fun onD3Change(v: Boolean) = update { it.copy(conD3 = v) }
    fun onMultivitaminicoChange(v: Boolean) = update { it.copy(conMultivitaminico = v) }
    fun onNoteChange(v: String) = update { it.copy(note = v) }

    fun salva(geckoId: Long) {
        val s = _uiState.value
        viewModelScope.launch {
            val modifica = originale
            if (s.feedingIdInModifica != null && modifica != null) {
                val riga = s.prede.first()
                feedingRepository.update(
                    modifica.copy(
                        dataOra = s.dataOra,
                        tipoPreda = riga.tipo.trim().ifBlank { context.getString(com.terra.geckocare.R.string.preda_altro) },
                        quantita = riga.quantita,
                        esito = s.esito,
                        conCalcio = s.conCalcio,
                        conD3 = s.conD3,
                        conMultivitaminico = s.conMultivitaminico,
                        note = s.note.trim(),
                    ),
                )
            } else {
                s.prede.forEach { riga ->
                    feedingRepository.insert(
                        Feeding(
                            geckoId = geckoId,
                            dataOra = s.dataOra,
                            tipoPreda = riga.tipo.trim().ifBlank { context.getString(com.terra.geckocare.R.string.preda_altro) },
                            quantita = riga.quantita,
                            esito = s.esito,
                            conCalcio = s.conCalcio,
                            conD3 = s.conD3,
                            conMultivitaminico = s.conMultivitaminico,
                            note = s.note.trim(),
                        ),
                    )
                }
            }
            update { it.copy(salvataggioCompletato = true) }
        }
    }

    fun elimina() {
        val f = originale ?: return
        viewModelScope.launch {
            feedingRepository.delete(f)
            update { it.copy(eliminato = true) }
        }
    }

    private inline fun update(block: (AggiungiPastoUiState) -> AggiungiPastoUiState) {
        _uiState.value = block(_uiState.value)
    }
}
