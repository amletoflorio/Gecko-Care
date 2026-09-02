package com.terra.geckocare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.EnvironmentLog
import com.terra.geckocare.data.repository.EnvironmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AmbienteFormUiState(
    val envLogId: Long? = null,
    val hotSpotTesto: String = "",
    val coldSpotTesto: String = "",
    val umiditaTesto: String = "",
    val notturnaTesto: String = "",
    val salvataggioCompletato: Boolean = false,
    val eliminato: Boolean = false,
) {
    val puoSalvare: Boolean
        get() = listOf(hotSpotTesto, coldSpotTesto, umiditaTesto, notturnaTesto).any { it.isNotBlank() }
}

class AmbienteFormViewModel(
    private val environmentRepository: EnvironmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AmbienteFormUiState())
    val uiState: StateFlow<AmbienteFormUiState> = _uiState.asStateFlow()

    private var originale: EnvironmentLog? = null

    fun caricaPerModifica(envLogId: Long) {
        viewModelScope.launch {
            val log = environmentRepository.getById(envLogId) ?: return@launch
            originale = log
            update {
                it.copy(
                    envLogId = log.id,
                    hotSpotTesto = log.hotSpotC?.let { v -> "%.1f".format(v) } ?: "",
                    coldSpotTesto = log.coldSpotC?.let { v -> "%.1f".format(v) } ?: "",
                    umiditaTesto = log.umiditaPercento?.let { v -> "%.0f".format(v) } ?: "",
                    notturnaTesto = log.temperaturaNotturnaC?.let { v -> "%.1f".format(v) } ?: "",
                )
            }
        }
    }

    fun onHotSpotChange(v: String) = update { it.copy(hotSpotTesto = filtraNumero(v)) }
    fun onColdSpotChange(v: String) = update { it.copy(coldSpotTesto = filtraNumero(v)) }
    fun onUmiditaChange(v: String) = update { it.copy(umiditaTesto = filtraNumero(v)) }
    fun onNotturnaChange(v: String) = update { it.copy(notturnaTesto = filtraNumero(v)) }

    fun salva(geckoId: Long) {
        val s = _uiState.value
        if (!s.puoSalvare) return
        viewModelScope.launch {
            val modifica = originale
            if (s.envLogId != null && modifica != null) {
                environmentRepository.update(
                    modifica.copy(
                        hotSpotC = s.hotSpotTesto.toDoubleOrNullIt(),
                        coldSpotC = s.coldSpotTesto.toDoubleOrNullIt(),
                        umiditaPercento = s.umiditaTesto.toDoubleOrNullIt(),
                        temperaturaNotturnaC = s.notturnaTesto.toDoubleOrNullIt(),
                    ),
                )
            } else {
                environmentRepository.insert(
                    EnvironmentLog(
                        geckoId = geckoId,
                        hotSpotC = s.hotSpotTesto.toDoubleOrNullIt(),
                        coldSpotC = s.coldSpotTesto.toDoubleOrNullIt(),
                        umiditaPercento = s.umiditaTesto.toDoubleOrNullIt(),
                        temperaturaNotturnaC = s.notturnaTesto.toDoubleOrNullIt(),
                    ),
                )
            }
            update { it.copy(salvataggioCompletato = true) }
        }
    }

    fun elimina() {
        val log = originale ?: return
        viewModelScope.launch {
            environmentRepository.delete(log)
            update { it.copy(eliminato = true) }
        }
    }

    private fun String.toDoubleOrNullIt(): Double? = replace(',', '.').trim().toDoubleOrNull()

    private fun filtraNumero(v: String) = v.filter { c -> c.isDigit() || c == '.' || c == ',' }

    private inline fun update(block: (AmbienteFormUiState) -> AmbienteFormUiState) {
        _uiState.value = block(_uiState.value)
    }
}
