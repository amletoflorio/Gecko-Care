package com.terra.geckocare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.GrowthLog
import com.terra.geckocare.data.repository.GrowthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CrescitaFormUiState(
    val growthLogId: Long? = null,
    val pesoTesto: String = "",
    val lunghezzaTesto: String = "",
    val salvataggioCompletato: Boolean = false,
    val eliminato: Boolean = false,
) {
    val puoSalvare: Boolean
        get() = pesoTesto.isNotBlank() || lunghezzaTesto.isNotBlank()
}

class CrescitaFormViewModel(
    private val growthRepository: GrowthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrescitaFormUiState())
    val uiState: StateFlow<CrescitaFormUiState> = _uiState.asStateFlow()

    private var originale: GrowthLog? = null

    fun caricaPerModifica(growthLogId: Long) {
        viewModelScope.launch {
            val log = growthRepository.getById(growthLogId) ?: return@launch
            originale = log
            update {
                it.copy(
                    growthLogId = log.id,
                    pesoTesto = log.pesoGrammi?.let { v -> "%.1f".format(v) } ?: "",
                    lunghezzaTesto = log.lunghezzaCm?.let { v -> "%.1f".format(v) } ?: "",
                )
            }
        }
    }

    fun onPesoChange(v: String) = update { it.copy(pesoTesto = filtraNumero(v)) }
    fun onLunghezzaChange(v: String) = update { it.copy(lunghezzaTesto = filtraNumero(v)) }

    fun salva(geckoId: Long) {
        val s = _uiState.value
        if (!s.puoSalvare) return
        viewModelScope.launch {
            val modifica = originale
            if (s.growthLogId != null && modifica != null) {
                growthRepository.update(
                    modifica.copy(
                        pesoGrammi = s.pesoTesto.toDoubleOrNullIt(),
                        lunghezzaCm = s.lunghezzaTesto.toDoubleOrNullIt(),
                    ),
                )
            } else {
                growthRepository.insert(
                    GrowthLog(
                        geckoId = geckoId,
                        pesoGrammi = s.pesoTesto.toDoubleOrNullIt(),
                        lunghezzaCm = s.lunghezzaTesto.toDoubleOrNullIt(),
                    ),
                )
            }
            update { it.copy(salvataggioCompletato = true) }
        }
    }

    fun elimina() {
        val log = originale ?: return
        viewModelScope.launch {
            growthRepository.delete(log)
            update { it.copy(eliminato = true) }
        }
    }

    private fun String.toDoubleOrNullIt(): Double? = replace(',', '.').trim().toDoubleOrNull()

    private fun filtraNumero(v: String) = v.filter { c -> c.isDigit() || c == '.' || c == ',' }

    private inline fun update(block: (CrescitaFormUiState) -> CrescitaFormUiState) {
        _uiState.value = block(_uiState.value)
    }
}
