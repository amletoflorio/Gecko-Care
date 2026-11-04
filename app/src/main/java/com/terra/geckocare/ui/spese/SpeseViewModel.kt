package com.terra.geckocare.ui.spese

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.CategoriaSpesa
import com.terra.geckocare.data.local.entity.Expense
import com.terra.geckocare.data.repository.ExpenseRepository
import com.terra.geckocare.data.repository.GeckoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

data class CategoriaBreakdown(val categoria: CategoriaSpesa, val importo: Double, val frazione: Float, val spese: List<Expense> = emptyList())

/** Riga "per animale"; [geckoId] nullo = gruppo delle spese generiche dell'allevamento. */
data class AnimaleBreakdown(val geckoId: Long?, val nome: String, val colore: String, val importo: Double, val frazione: Float, val spese: List<Expense> = emptyList())

/** Acquisto dell'elenco spese del mese: una o più voci con stessa data e ambito; [nomeGecko] nullo = spesa generica. */
data class AcquistoUi(val voci: List<Expense>, val nomeGecko: String?, val coloreGecko: String?) {
    val primoId: Long get() = voci.first().id
    val data get() = voci.first().data
    val totale: Double get() = voci.sumOf { it.importo }
}

data class SpeseUiState(
    val caricamento: Boolean = true,
    val mese: YearMonth = YearMonth.now(),
    val nomeGecko: String = "",
    val totaleGeckoMese: Double = 0.0,
    val breakdownCategoria: List<CategoriaBreakdown> = emptyList(),
    val acquistiGecko: List<AcquistoUi> = emptyList(),
    val totaleAllevamentoMese: Double = 0.0,
    val totaleGenericheMese: Double = 0.0,
    val breakdownAnimale: List<AnimaleBreakdown> = emptyList(),
    val acquistiAllevamento: List<AcquistoUi> = emptyList(),
)

class SpeseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val geckoRepository: GeckoRepository,
) : ViewModel() {

    private val geckoId = MutableStateFlow<Long?>(null)
    private val mese = MutableStateFlow(YearMonth.now())

    fun selezionaGecko(id: Long) {
        geckoId.value = id
    }

    fun mesePrecedente() {
        mese.value = mese.value.minusMonths(1)
    }

    /** Non si va oltre il mese corrente. */
    fun meseSuccessivo() {
        if (mese.value < YearMonth.now()) mese.value = mese.value.plusMonths(1)
    }

    private val speseGecko = combine(geckoId, mese) { id, m -> id to m }.flatMapLatest { (id, m) ->
        if (id == null) flowOf(emptyList()) else expenseRepository.expensesForGeckoInRange(id, m.atDay(1), m.atEndOfMonth())
    }

    private val speseTotaliMese = mese.flatMapLatest { m -> expenseRepository.expensesInRange(m.atDay(1), m.atEndOfMonth()) }

    private val geckoCorrente = geckoId.flatMapLatest { id ->
        if (id == null) flowOf(null) else geckoRepository.getById(id)
    }

    val uiState: StateFlow<SpeseUiState> = combine(
        speseGecko,
        geckoCorrente,
        speseTotaliMese,
        geckoRepository.getAll(),
        mese,
    ) { speseAnimale, gecko, speseTotali, gechi, meseCorrente ->
        val mappaGechi = gechi.associateBy { it.id }
        val ordine = compareByDescending<Expense> { it.data }.thenByDescending { it.id }
        fun acquisti(spese: List<Expense>): List<AcquistoUi> = spese.groupBy { it.chiaveGruppo }.values
            .map { voci ->
                val ordinate = voci.sortedBy { it.id }
                val g = ordinate.first().geckoId?.let(mappaGechi::get)
                AcquistoUi(ordinate, g?.nome, g?.coloreTag)
            }
            .sortedWith(compareByDescending<AcquistoUi> { it.data }.thenByDescending { it.primoId })

        val totaleGecko = speseAnimale.sumOf { it.importo }
        val gruppiCategoria = speseAnimale.groupBy { it.categoria }
        val breakdownCategoria = gruppiCategoria
            .map { (cat, lista) -> CategoriaBreakdown(cat, lista.sumOf { it.importo }, 0f, lista.sortedWith(ordine)) }
            .sortedByDescending { it.importo }
            .map { it.copy(frazione = if (totaleGecko > 0) (it.importo / totaleGecko).toFloat() else 0f) }

        val totaleAllevamento = speseTotali.sumOf { it.importo }
        val generiche = speseTotali.filter { it.geckoId == null }
        // Spese di gechi eliminati (geckoId azzerato dal SET_NULL) finiscono tra le generiche.
        val perAnimale = speseTotali.filter { it.geckoId != null }.groupBy { it.geckoId!! }
            .mapNotNull { (id, lista) ->
                val g = mappaGechi[id] ?: return@mapNotNull null
                AnimaleBreakdown(id, g.nome, g.coloreTag, lista.sumOf { it.importo }, 0f, lista.sortedWith(ordine))
            }
        val gruppoGeneriche = generiche.takeIf { it.isNotEmpty() }?.let {
            AnimaleBreakdown(null, "", "", it.sumOf { s -> s.importo }, 0f, it.sortedWith(ordine))
        }
        val breakdownAnimale = (perAnimale + listOfNotNull(gruppoGeneriche))
            .sortedByDescending { it.importo }
            .map { it.copy(frazione = if (totaleAllevamento > 0) (it.importo / totaleAllevamento).toFloat() else 0f) }

        SpeseUiState(
            caricamento = false,
            mese = meseCorrente,
            nomeGecko = gecko?.nome ?: "",
            totaleGeckoMese = totaleGecko,
            breakdownCategoria = breakdownCategoria,
            acquistiGecko = acquisti(speseAnimale),
            totaleAllevamentoMese = totaleAllevamento,
            totaleGenericheMese = generiche.sumOf { it.importo },
            breakdownAnimale = breakdownAnimale,
            acquistiAllevamento = acquisti(speseTotali),
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpeseUiState())
}
