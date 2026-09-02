package com.terra.geckocare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.Reminder
import com.terra.geckocare.data.local.entity.TipoReminder
import com.terra.geckocare.data.repository.ReminderRepository
import com.terra.geckocare.worker.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReminderFormUiState(
    val reminderId: Long? = null,
    val tipo: TipoReminder = TipoReminder.PASTO,
    val frequenzaTesto: String = "",
    val intervalloGiorni: Int = 1,
    val orarioMinuti: Int = 19 * 60,
    val attivo: Boolean = true,
    val salvataggioCompletato: Boolean = false,
    val eliminato: Boolean = false,
)

class ReminderFormViewModel(
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderFormUiState())
    val uiState: StateFlow<ReminderFormUiState> = _uiState.asStateFlow()

    private var caricato = false

    fun caricaPerModifica(reminderId: Long) {
        if (caricato) return
        caricato = true
        viewModelScope.launch {
            reminderRepository.getByIdOnce(reminderId)?.let { r ->
                _uiState.value = ReminderFormUiState(
                    reminderId = r.id,
                    tipo = r.tipo,
                    frequenzaTesto = r.frequenzaTesto,
                    intervalloGiorni = r.intervalloGiorni,
                    orarioMinuti = r.orarioMinuti,
                    attivo = r.attivo,
                )
            }
        }
    }

    fun onTipoChange(v: TipoReminder) = update { it.copy(tipo = v) }
    fun onFrequenzaTestoChange(v: String) = update { it.copy(frequenzaTesto = v) }
    fun onIntervalloChange(v: Int) = update { it.copy(intervalloGiorni = v.coerceAtLeast(1)) }
    fun onOrarioChange(minuti: Int) = update { it.copy(orarioMinuti = minuti.coerceIn(0, 1439)) }
    fun onAttivoChange(v: Boolean) = update { it.copy(attivo = v) }

    fun salva(geckoId: Long?) {
        val s = _uiState.value
        viewModelScope.launch {
            val salvato = if (s.reminderId != null) {
                val reminder = Reminder(
                    id = s.reminderId,
                    geckoId = geckoId,
                    tipo = s.tipo,
                    frequenzaTesto = s.frequenzaTesto,
                    intervalloGiorni = s.intervalloGiorni,
                    orarioMinuti = s.orarioMinuti,
                    attivo = s.attivo,
                )
                reminderRepository.update(reminder)
                reminder
            } else {
                val nuovoId = reminderRepository.insert(
                    Reminder(
                        geckoId = geckoId,
                        tipo = s.tipo,
                        frequenzaTesto = s.frequenzaTesto,
                        intervalloGiorni = s.intervalloGiorni,
                        orarioMinuti = s.orarioMinuti,
                        attivo = s.attivo,
                    ),
                )
                Reminder(
                    id = nuovoId,
                    geckoId = geckoId,
                    tipo = s.tipo,
                    frequenzaTesto = s.frequenzaTesto,
                    intervalloGiorni = s.intervalloGiorni,
                    orarioMinuti = s.orarioMinuti,
                    attivo = s.attivo,
                )
            }
            reminderScheduler.schedule(salvato)
            update { it.copy(salvataggioCompletato = true) }
        }
    }

    fun elimina() {
        val id = _uiState.value.reminderId ?: return
        viewModelScope.launch {
            reminderRepository.delete(id)
            reminderScheduler.cancel(id)
            update { it.copy(eliminato = true) }
        }
    }

    private inline fun update(block: (ReminderFormUiState) -> ReminderFormUiState) {
        _uiState.value = block(_uiState.value)
    }
}
