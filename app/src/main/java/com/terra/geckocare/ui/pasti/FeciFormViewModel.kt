package com.terra.geckocare.ui.pasti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.DefecationLog
import com.terra.geckocare.data.repository.DefecationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

data class FeciFormUiState(
    val logIdInModifica: Long? = null,
    val dataOra: Instant = Instant.now(),
    val normale: Boolean = true,
    val note: String = "",
    val salvataggioCompletato: Boolean = false,
    val eliminato: Boolean = false,
)

class FeciFormViewModel(
    private val defecationRepository: DefecationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeciFormUiState())
    val uiState: StateFlow<FeciFormUiState> = _uiState.asStateFlow()

    private var originale: DefecationLog? = null

    fun caricaPerModifica(logId: Long) {
        viewModelScope.launch {
            val log = defecationRepository.getById(logId) ?: return@launch
            originale = log
            update { it.copy(logIdInModifica = log.id, dataOra = log.dataOra, normale = log.normale, note = log.note) }
        }
    }

    fun onDataOraChange(v: Instant) = update { it.copy(dataOra = v) }
    fun onNormaleChange(v: Boolean) = update { it.copy(normale = v) }
    fun onNoteChange(v: String) = update { it.copy(note = v) }

    fun salva(geckoId: Long) {
        val s = _uiState.value
        viewModelScope.launch {
            val modifica = originale
            if (s.logIdInModifica != null && modifica != null) {
                defecationRepository.update(modifica.copy(dataOra = s.dataOra, normale = s.normale, note = s.note.trim()))
            } else {
                defecationRepository.insert(DefecationLog(geckoId = geckoId, dataOra = s.dataOra, normale = s.normale, note = s.note.trim()))
            }
            update { it.copy(salvataggioCompletato = true) }
        }
    }

    fun elimina() {
        val log = originale ?: return
        viewModelScope.launch {
            defecationRepository.delete(log)
            update { it.copy(eliminato = true) }
        }
    }

    private inline fun update(block: (FeciFormUiState) -> FeciFormUiState) {
        _uiState.value = block(_uiState.value)
    }
}
