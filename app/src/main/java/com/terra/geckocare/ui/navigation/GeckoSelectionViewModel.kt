package com.terra.geckocare.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.Gecko
import com.terra.geckocare.data.repository.GeckoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Stato dell'animale "attivo" condiviso da Home/Pasti/Statistiche/Spese: uno
 * scope unico per l'intera Activity (istanziato una volta in [com.terra.geckocare.ui.TerraApp]
 * e passato ai singoli schermi) così lo switcher orizzontale in Home cambia
 * l'animale corrente senza navigare né perdere stato tra i tab.
 */
class GeckoSelectionViewModel(
    private val geckoRepository: GeckoRepository,
) : ViewModel() {

    val gechiAttivi: StateFlow<List<Gecko>> = geckoRepository.getAllAttivi()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedGeckoId = MutableStateFlow<Long?>(null)
    val selectedGeckoId: StateFlow<Long?> = _selectedGeckoId.asStateFlow()

    init {
        viewModelScope.launch {
            gechiAttivi.collect { list ->
                val stillValid = list.any { it.id == _selectedGeckoId.value }
                if (!stillValid) {
                    _selectedGeckoId.value = list.firstOrNull()?.id
                }
            }
        }
    }

    fun select(geckoId: Long) {
        _selectedGeckoId.value = geckoId
    }
}
