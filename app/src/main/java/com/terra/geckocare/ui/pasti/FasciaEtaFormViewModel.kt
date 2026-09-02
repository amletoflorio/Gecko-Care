package com.terra.geckocare.ui.pasti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.FeedingPlanRule
import com.terra.geckocare.data.repository.FeedingPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class FasciaEtaFormUiState(
    val ruleId: Long? = null,
    val nomeFase: String = "",
    val etaMinGiorni: String = "0",
    val etaMaxGiorni: String = "60",
    val frequenzaTesto: String = "",
    val noteIntegratori: String = "",
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
            _uiState.value = FasciaEtaFormUiState(
                ruleId = regola.id,
                nomeFase = regola.nomeFase,
                etaMinGiorni = regola.etaMinGiorni.toString(),
                etaMaxGiorni = regola.etaMaxGiorni.toString(),
                frequenzaTesto = regola.frequenzaTesto,
                noteIntegratori = regola.noteIntegratori,
                soloQuestoAnimale = regola.geckoId != null,
            )
        }
    }

    fun onNomeFaseChange(v: String) = update { it.copy(nomeFase = v) }
    fun onEtaMinChange(v: String) = update { it.copy(etaMinGiorni = v.filter(Char::isDigit)) }
    fun onEtaMaxChange(v: String) = update { it.copy(etaMaxGiorni = v.filter(Char::isDigit)) }
    fun onFrequenzaTestoChange(v: String) = update { it.copy(frequenzaTesto = v) }
    fun onNoteIntegratoriChange(v: String) = update { it.copy(noteIntegratori = v) }
    fun onSoloQuestoAnimaleChange(v: Boolean) = update { it.copy(soloQuestoAnimale = v) }

    fun salva(geckoId: Long?) {
        val s = _uiState.value
        if (s.nomeFase.isBlank()) return
        val min = s.etaMinGiorni.toIntOrNull() ?: 0
        val max = s.etaMaxGiorni.toIntOrNull() ?: (min + 1)
        val target = if (s.soloQuestoAnimale) geckoId else null
        viewModelScope.launch {
            if (s.ruleId != null) {
                feedingPlanRepository.update(
                    FeedingPlanRule(
                        id = s.ruleId,
                        geckoId = target,
                        etaMinGiorni = min,
                        etaMaxGiorni = max,
                        nomeFase = s.nomeFase.trim(),
                        frequenzaTesto = s.frequenzaTesto.trim(),
                        noteIntegratori = s.noteIntegratori.trim(),
                    ),
                )
            } else {
                feedingPlanRepository.insert(
                    FeedingPlanRule(
                        geckoId = target,
                        etaMinGiorni = min,
                        etaMaxGiorni = max,
                        nomeFase = s.nomeFase.trim(),
                        frequenzaTesto = s.frequenzaTesto.trim(),
                        noteIntegratori = s.noteIntegratori.trim(),
                    ),
                )
            }
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
