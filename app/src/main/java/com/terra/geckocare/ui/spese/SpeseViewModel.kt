package com.terra.geckocare.ui.spese

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.CategoriaSpesa
import com.terra.geckocare.data.local.entity.Expense
import com.terra.geckocare.data.repository.ExpenseRepository
import com.terra.geckocare.data.repository.GeckoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class CategoriaBreakdown(val categoria: CategoriaSpesa, val importo: Double, val frazione: Float, val spese: List<Expense> = emptyList())
data class AnimaleBreakdown(val geckoId: Long, val nome: String, val colore: String, val importo: Double, val frazione: Float, val spese: List<Expense> = emptyList())

data class SpeseUiState(
    val caricamento: Boolean = true,
    val nomeGecko: String = "",
    val totaleGeckoMese: Double = 0.0,
    val breakdownCategoria: List<CategoriaBreakdown> = emptyList(),
    val totaleAllevamentoMese: Double = 0.0,
    val breakdownAnimale: List<AnimaleBreakdown> = emptyList(),
)

class SpeseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val geckoRepository: GeckoRepository,
) : ViewModel() {

    private val geckoId = MutableStateFlow<Long?>(null)

    fun selezionaGecko(id: Long) {
        geckoId.value = id
    }

    private val inizioMese = LocalDate.now().withDayOfMonth(1)
    private val fineMese = inizioMese.plusMonths(1).minusDays(1)

    private val speseGecko = geckoId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else expenseRepository.expensesForGeckoInRange(id, inizioMese, fineMese)
    }

    private val geckoCorrente = geckoId.flatMapLatest { id ->
        if (id == null) flowOf(null) else geckoRepository.getById(id)
    }

    val uiState: StateFlow<SpeseUiState> = combine(
        speseGecko,
        geckoCorrente,
        expenseRepository.expensesInRange(inizioMese, fineMese),
        geckoRepository.getAll(),
    ) { speseAnimale, gecko, speseTotali, gechi ->
        val totaleGecko = speseAnimale.sumOf { it.importo }
        val gruppiCategoria = speseAnimale.groupBy { it.categoria }
        val perCategoria = gruppiCategoria
            .map { (cat, lista) -> cat to lista.sumOf { it.importo } }
            .sortedByDescending { it.second }
        val breakdownCategoria = perCategoria.map { (cat, importo) ->
            val lista = gruppiCategoria[cat].orEmpty().sortedByDescending { it.data }
            CategoriaBreakdown(cat, importo, if (totaleGecko > 0) (importo / totaleGecko).toFloat() else 0f, lista)
        }

        val totaleAllevamento = speseTotali.sumOf { it.importo }
        val mappaNomi = gechi.associateBy { it.id }
        val gruppiAnimale = speseTotali.filter { it.geckoId != null }.groupBy { it.geckoId }
        val perAnimale = gruppiAnimale
            .mapNotNull { (id, lista) -> id?.let { it to lista.sumOf { s -> s.importo } } }
            .sortedByDescending { it.second }
        val breakdownAnimale = perAnimale.mapNotNull { (id, importo) ->
            val g = mappaNomi[id] ?: return@mapNotNull null
            val lista = gruppiAnimale[id].orEmpty().sortedByDescending { it.data }
            AnimaleBreakdown(id, g.nome, g.coloreTag, importo, if (totaleAllevamento > 0) (importo / totaleAllevamento).toFloat() else 0f, lista)
        }

        SpeseUiState(
            caricamento = false,
            nomeGecko = gecko?.nome ?: "",
            totaleGeckoMese = totaleGecko,
            breakdownCategoria = breakdownCategoria,
            totaleAllevamentoMese = totaleAllevamento,
            breakdownAnimale = breakdownAnimale,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpeseUiState())
}
