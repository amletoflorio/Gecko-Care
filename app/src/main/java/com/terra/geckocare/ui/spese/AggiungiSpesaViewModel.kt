package com.terra.geckocare.ui.spese

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.CategoriaSpesa
import com.terra.geckocare.data.local.entity.Expense
import com.terra.geckocare.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AggiungiSpesaUiState(
    val expenseIdInModifica: Long? = null,
    val categoria: CategoriaSpesa = CategoriaSpesa.INSETTI,
    val importoTesto: String = "",
    val data: LocalDate = LocalDate.now(),
    val note: String = "",
    val genericaAllevamento: Boolean = false,
    val salvataggioCompletato: Boolean = false,
    val eliminato: Boolean = false,
)

class AggiungiSpesaViewModel(
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AggiungiSpesaUiState())
    val uiState: StateFlow<AggiungiSpesaUiState> = _uiState.asStateFlow()

    private var originale: Expense? = null

    fun caricaPerModifica(expenseId: Long) {
        viewModelScope.launch {
            val e = expenseRepository.getById(expenseId) ?: return@launch
            originale = e
            update {
                it.copy(
                    expenseIdInModifica = e.id,
                    categoria = e.categoria,
                    importoTesto = "%.2f".format(e.importo).replace('.', ','),
                    data = e.data,
                    note = e.note,
                    genericaAllevamento = e.geckoId == null,
                )
            }
        }
    }

    fun onCategoriaChange(v: CategoriaSpesa) = update { it.copy(categoria = v) }
    fun onImportoChange(v: String) = update { it.copy(importoTesto = v.filter { c -> c.isDigit() || c == '.' || c == ',' }) }
    fun onDataChange(v: LocalDate) = update { it.copy(data = v) }
    fun onNoteChange(v: String) = update { it.copy(note = v) }
    fun onGenericaChange(v: Boolean) = update { it.copy(genericaAllevamento = v) }

    fun salva(geckoId: Long?) {
        val s = _uiState.value
        val importo = s.importoTesto.replace(',', '.').toDoubleOrNull() ?: return
        viewModelScope.launch {
            val modifica = originale
            if (s.expenseIdInModifica != null && modifica != null) {
                expenseRepository.update(
                    modifica.copy(
                        geckoId = if (s.genericaAllevamento) null else geckoId ?: modifica.geckoId,
                        categoria = s.categoria,
                        importo = importo,
                        data = s.data,
                        note = s.note.trim(),
                    ),
                )
            } else {
                expenseRepository.insert(
                    Expense(
                        geckoId = if (s.genericaAllevamento) null else geckoId,
                        categoria = s.categoria,
                        importo = importo,
                        data = s.data,
                        note = s.note.trim(),
                    ),
                )
            }
            update { it.copy(salvataggioCompletato = true) }
        }
    }

    fun elimina() {
        val e = originale ?: return
        viewModelScope.launch {
            expenseRepository.delete(e)
            update { it.copy(eliminato = true) }
        }
    }

    private inline fun update(block: (AggiungiSpesaUiState) -> AggiungiSpesaUiState) {
        _uiState.value = block(_uiState.value)
    }
}
