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
import java.util.UUID

/**
 * Una voce del form: un acquisto può contenere più articoli (es. dubia + calcio + substrato),
 * ognuno salvato come [Expense] con stessa data, ambito e [Expense.gruppo].
 */
data class VoceSpesaForm(
    val descrizione: String = "",
    val importoTesto: String = "",
    val categoria: CategoriaSpesa = CategoriaSpesa.INSETTI,
) {
    val importo: Double? get() = importoTesto.replace(',', '.').toDoubleOrNull()
}

data class AggiungiSpesaUiState(
    val expenseIdInModifica: Long? = null,
    val voci: List<VoceSpesaForm> = listOf(VoceSpesaForm()),
    val data: LocalDate = LocalDate.now(),
    val genericaAllevamento: Boolean = false,
    val salvataggioCompletato: Boolean = false,
    val eliminato: Boolean = false,
) {
    val totale: Double get() = voci.sumOf { it.importo ?: 0.0 }

    /** Salvabile se almeno una voce ha un importo valido. */
    val valida: Boolean get() = voci.any { it.importo != null }
}

class AggiungiSpesaViewModel(
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AggiungiSpesaUiState())
    val uiState: StateFlow<AggiungiSpesaUiState> = _uiState.asStateFlow()

    /** Voci dell'acquisto in modifica, così come erano nel DB. */
    private var originali: List<Expense> = emptyList()

    fun caricaPerModifica(expenseId: Long) {
        viewModelScope.launch {
            val e = expenseRepository.getById(expenseId) ?: return@launch
            val voci = expenseRepository.vociDelloStessoAcquisto(e)
            originali = voci
            update {
                it.copy(
                    expenseIdInModifica = e.id,
                    voci = voci.map { v -> VoceSpesaForm(v.note, "%.2f".format(v.importo).replace('.', ','), v.categoria) },
                    data = e.data,
                    genericaAllevamento = e.geckoId == null,
                )
            }
        }
    }

    /** La nuova voce eredita la categoria dell'ultima: di solito si inseriscono articoli simili in fila. */
    fun aggiungiVoce() = update { it.copy(voci = it.voci + VoceSpesaForm(categoria = it.voci.lastOrNull()?.categoria ?: CategoriaSpesa.INSETTI)) }

    fun rimuoviVoce(index: Int) = update { st ->
        st.copy(voci = st.voci.filterIndexed { i, _ -> i != index }.ifEmpty { listOf(VoceSpesaForm()) })
    }

    fun onDescrizioneChange(index: Int, v: String) = aggiornaVoce(index) { it.copy(descrizione = v) }
    fun onImportoChange(index: Int, v: String) = aggiornaVoce(index) { it.copy(importoTesto = v.filter { c -> c.isDigit() || c == '.' || c == ',' }) }
    fun onCategoriaChange(index: Int, v: CategoriaSpesa) = aggiornaVoce(index) { it.copy(categoria = v) }
    fun onDataChange(v: LocalDate) = update { it.copy(data = v) }
    fun onGenericaChange(v: Boolean) = update { it.copy(genericaAllevamento = v) }

    fun salva(geckoId: Long?) {
        val s = _uiState.value
        val voci = s.voci.filter { it.importo != null }
        if (voci.isEmpty()) return
        viewModelScope.launch {
            val primo = originali.firstOrNull()
            // In modifica si mantiene il geco dell'acquisto originale se non c'è una selezione.
            val geckoFinale = if (s.genericaAllevamento) null else geckoId ?: primo?.geckoId
            val gruppo = primo?.gruppo ?: UUID.randomUUID().toString()
            expenseRepository.sostituisciVoci(
                idPrecedenti = originali.map { it.id },
                nuove = voci.map { voce ->
                    Expense(
                        geckoId = geckoFinale,
                        categoria = voce.categoria,
                        importo = voce.importo!!,
                        data = s.data,
                        note = voce.descrizione.trim(),
                        gruppo = gruppo,
                    )
                },
            )
            update { it.copy(salvataggioCompletato = true) }
        }
    }

    /** Elimina l'intero acquisto, con tutte le sue voci. */
    fun elimina() {
        if (originali.isEmpty()) return
        viewModelScope.launch {
            expenseRepository.deleteByIds(originali.map { it.id })
            update { it.copy(eliminato = true) }
        }
    }

    private inline fun aggiornaVoce(index: Int, block: (VoceSpesaForm) -> VoceSpesaForm) = update { st ->
        st.copy(voci = st.voci.mapIndexed { i, v -> if (i == index) block(v) else v })
    }

    private inline fun update(block: (AggiungiSpesaUiState) -> AggiungiSpesaUiState) {
        _uiState.value = block(_uiState.value)
    }
}
