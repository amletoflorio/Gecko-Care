package com.terra.geckocare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.CareNote
import com.terra.geckocare.data.repository.CareNoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CareNoteFormViewModel(
    private val careNoteRepository: CareNoteRepository,
) : ViewModel() {

    private val _testo = MutableStateFlow("")
    val testo: StateFlow<String> = _testo.asStateFlow()

    private val _salvataggioCompletato = MutableStateFlow(false)
    val salvataggioCompletato: StateFlow<Boolean> = _salvataggioCompletato.asStateFlow()

    fun onTestoChange(v: String) {
        _testo.value = v
    }

    fun salva(geckoId: Long) {
        val pulito = _testo.value.trim()
        if (pulito.isEmpty()) return
        viewModelScope.launch {
            careNoteRepository.insert(CareNote(geckoId = geckoId, testo = pulito))
            _salvataggioCompletato.value = true
        }
    }
}
