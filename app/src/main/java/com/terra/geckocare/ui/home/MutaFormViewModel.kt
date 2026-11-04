package com.terra.geckocare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.ShedLog
import com.terra.geckocare.data.repository.ShedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

data class MutaFormUiState(
    val shedLogId: Long? = null,
    val dataOra: Instant = Instant.now(),
    val completa: Boolean = true,
    val note: String = "",
    val salvataggioCompletato: Boolean = false,
    val eliminato: Boolean = false,
)

class MutaFormViewModel(
    private val shedRepository: ShedRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MutaFormUiState())
    val uiState: StateFlow<MutaFormUiState> = _uiState.asStateFlow()

    private var originale: ShedLog? = null

    fun caricaPerModifica(shedLogId: Long) {
        viewModelScope.launch {
            val log = shedRepository.getById(shedLogId) ?: return@launch
            originale = log
            update { it.copy(shedLogId = log.id, dataOra = log.dataOra, completa = log.completa, note = log.note) }
        }
    }

    fun onDataOraChange(v: Instant) = update { it.copy(dataOra = v) }
    fun onCompletaChange(v: Boolean) = update { it.copy(completa = v) }
    fun onNoteChange(v: String) = update { it.copy(note = v) }

    fun salva(geckoId: Long) {
        val s = _uiState.value
        viewModelScope.launch {
            val modifica = originale
            if (s.shedLogId != null && modifica != null) {
                shedRepository.update(modifica.copy(dataOra = s.dataOra, completa = s.completa, note = s.note.trim()))
            } else {
                shedRepository.insert(ShedLog(geckoId = geckoId, dataOra = s.dataOra, completa = s.completa, note = s.note.trim()))
            }
            update { it.copy(salvataggioCompletato = true) }
        }
    }

    fun elimina() {
        val log = originale ?: return
        viewModelScope.launch {
            shedRepository.delete(log)
            update { it.copy(eliminato = true) }
        }
    }

    private inline fun update(block: (MutaFormUiState) -> MutaFormUiState) {
        _uiState.value = block(_uiState.value)
    }
}
