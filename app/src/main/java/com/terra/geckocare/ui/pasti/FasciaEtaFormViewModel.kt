package com.terra.geckocare.ui.pasti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.ETA_MESI_SENZA_LIMITE
import com.terra.geckocare.data.local.entity.FeedingPlanRule
import com.terra.geckocare.data.local.entity.FeedingPlanStep
import com.terra.geckocare.data.repository.FeedingPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Riga dello schema integratori nel form: il numero del pasto è la posizione nella lista (1-based). */
data class RigaSchema(
    val conCalcio: Boolean = true,
    val conD3: Boolean = false,
    val conMultivitaminico: Boolean = false,
    /** Tipi di insetti per il pasto, separati da virgola. */
    val insetti: String = "",
) {
    fun listaInsetti(): List<String> = insetti.split(',').map { it.trim() }.filter { it.isNotEmpty() }

    /** Aggiunge o toglie un tipo di insetto mantenendo l'ordine degli altri. */
    fun toggleInsetto(tipo: String): RigaSchema {
        val lista = listaInsetti()
        val nuova = if (lista.any { it.equals(tipo, ignoreCase = true) }) lista.filterNot { it.equals(tipo, ignoreCase = true) } else lista + tipo
        return copy(insetti = nuova.joinToString(", "))
    }
}

data class FasciaEtaFormUiState(
    val ruleId: Long? = null,
    val nomeFase: String = "",
    val etaMinMesi: String = "0",
    /** Vuoto = fascia senza limite superiore (es. adulti). */
    val etaMaxMesi: String = "2",
    val frequenzaTesto: String = "",
    val noteIntegratori: String = "",
    val schema: List<RigaSchema> = emptyList(),
    val soloQuestoAnimale: Boolean = false,
    val salvataggioCompletato: Boolean = false,
    val eliminato: Boolean = false,
)

class FasciaEtaFormViewModel(
    private val feedingPlanRepository: FeedingPlanRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FasciaEtaFormUiState())
    val uiState: StateFlow<FasciaEtaFormUiState> = _uiState.asStateFlow()

    private var caricato = false

    fun caricaPerModifica(ruleId: Long, geckoId: Long?) {
        if (caricato) return
        caricato = true
        viewModelScope.launch {
            val regola = (if (geckoId != null) feedingPlanRepository.rulesForGeckoOrDefault(geckoId).first() else feedingPlanRepository.defaultRules().first())
                .firstOrNull { it.id == ruleId } ?: return@launch
            val schema = feedingPlanRepository.stepsForRuleOnce(regola.id)
            _uiState.value = FasciaEtaFormUiState(
                ruleId = regola.id,
                nomeFase = regola.nomeFase,
                etaMinMesi = regola.etaMinMesi.toString(),
                etaMaxMesi = if (regola.etaMaxMesi >= ETA_MESI_SENZA_LIMITE) "" else regola.etaMaxMesi.toString(),
                frequenzaTesto = regola.frequenzaTesto,
                noteIntegratori = regola.noteIntegratori,
                schema = schema.map { RigaSchema(it.conCalcio, it.conD3, it.conMultivitaminico, it.insetti) },
                soloQuestoAnimale = regola.geckoId != null,
            )
        }
    }

    fun onNomeFaseChange(v: String) = update { it.copy(nomeFase = v) }
    fun onEtaMinChange(v: String) = update { it.copy(etaMinMesi = v.filter(Char::isDigit).take(3)) }
    fun onEtaMaxChange(v: String) = update { it.copy(etaMaxMesi = v.filter(Char::isDigit).take(3)) }
    fun onFrequenzaTestoChange(v: String) = update { it.copy(frequenzaTesto = v) }
    fun onNoteIntegratoriChange(v: String) = update { it.copy(noteIntegratori = v) }
    fun onSoloQuestoAnimaleChange(v: Boolean) = update { it.copy(soloQuestoAnimale = v) }

    /** La nuova riga eredita gli insetti della precedente: di solito cambiano solo gli integratori. */
    fun aggiungiRigaSchema() = update { it.copy(schema = it.schema + RigaSchema(insetti = it.schema.lastOrNull()?.insetti.orEmpty())) }
    fun rimuoviRigaSchema(index: Int) = update { st -> st.copy(schema = st.schema.filterIndexed { i, _ -> i != index }) }
    fun aggiornaRigaSchema(index: Int, riga: RigaSchema) = update { st ->
        st.copy(schema = st.schema.mapIndexed { i, r -> if (i == index) riga else r })
    }

    fun salva(geckoId: Long?) {
        val s = _uiState.value
        if (s.nomeFase.isBlank()) return
        val min = s.etaMinMesi.toIntOrNull() ?: 0
        val max = (s.etaMaxMesi.toIntOrNull() ?: ETA_MESI_SENZA_LIMITE).coerceAtLeast(min)
        val target = if (s.soloQuestoAnimale) geckoId else null
        val regola = FeedingPlanRule(
            id = s.ruleId ?: 0,
            geckoId = target,
            etaMinMesi = min,
            etaMaxMesi = max,
            nomeFase = s.nomeFase.trim(),
            frequenzaTesto = s.frequenzaTesto.trim(),
            noteIntegratori = s.noteIntegratori.trim(),
        )
        viewModelScope.launch {
            val id = if (s.ruleId != null) {
                feedingPlanRepository.update(regola)
                s.ruleId
            } else {
                feedingPlanRepository.insert(regola)
            }
            feedingPlanRepository.replaceSteps(
                id,
                s.schema.mapIndexed { i, r ->
                    FeedingPlanStep(
                        ruleId = id,
                        numeroPasto = i + 1,
                        conCalcio = r.conCalcio,
                        conD3 = r.conD3,
                        conMultivitaminico = r.conMultivitaminico,
                        insetti = r.listaInsetti().joinToString(", "),
                    )
                },
            )
            update { it.copy(salvataggioCompletato = true) }
        }
    }

    fun elimina() {
        val id = _uiState.value.ruleId ?: return
        viewModelScope.launch {
            feedingPlanRepository.deleteById(id)
            update { it.copy(eliminato = true) }
        }
    }

    private inline fun update(block: (FasciaEtaFormUiState) -> FasciaEtaFormUiState) {
        _uiState.value = block(_uiState.value)
    }
}
