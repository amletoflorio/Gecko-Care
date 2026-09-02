package com.terra.geckocare.ui.gechi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.Feeding
import com.terra.geckocare.data.local.entity.Gecko
import com.terra.geckocare.data.local.entity.Reminder
import com.terra.geckocare.data.local.entity.TipoReminder
import com.terra.geckocare.data.repository.ExpenseRepository
import com.terra.geckocare.data.repository.FeedingRepository
import com.terra.geckocare.data.repository.GeckoRepository
import com.terra.geckocare.data.repository.GrowthRepository
import com.terra.geckocare.data.repository.ReminderRepository
import com.terra.geckocare.domain.etaLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class GeckoRowUiModel(
    val id: Long,
    val nome: String,
    val morph: String,
    val fotoProfiloPath: String?,
    val coloreTag: String,
    val pesoLabel: String,
    val etaLabel: String,
    val tags: List<String>,
)

data class GechiUiState(
    val caricamento: Boolean = true,
    val righe: List<GeckoRowUiModel> = emptyList(),
    val totaleAnimali: Int = 0,
    val pastoOggiCount: Int = 0,
    val speseMeseCorrente: Double = 0.0,
    val tagDisponibili: List<String> = emptyList(),
    val tagSelezionato: String? = null,
) {
    val righeFiltrate: List<GeckoRowUiModel>
        get() = if (tagSelezionato == null) righe else righe.filter { tagSelezionato in it.tags }
}

private data class GeckoEnriched(
    val gecko: Gecko,
    val pesoGrammi: Double?,
    val tags: List<String>,
    val ultimoPasto: Feeding?,
    val remindersPasto: List<Reminder>,
)

class GechiViewModel(
    private val context: android.content.Context,
    private val geckoRepository: GeckoRepository,
    private val growthRepository: GrowthRepository,
    private val feedingRepository: FeedingRepository,
    private val reminderRepository: ReminderRepository,
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {

    private val tagSelezionato = MutableStateFlow<String?>(null)

    private fun enrichedFlow(gecko: Gecko) = combine(
        growthRepository.latestWeightForGecko(gecko.id),
        geckoRepository.tagsForGecko(gecko.id),
        feedingRepository.latestForGecko(gecko.id),
        reminderRepository.remindersForGecko(gecko.id),
    ) { peso, tags, ultimoPasto, reminders ->
        GeckoEnriched(
            gecko = gecko,
            pesoGrammi = peso?.pesoGrammi,
            tags = tags.map { it.etichetta },
            ultimoPasto = ultimoPasto,
            remindersPasto = reminders.filter { it.tipo == TipoReminder.PASTO && it.attivo },
        )
    }

    private val enrichedList = geckoRepository.getAllAttivi().flatMapLatest { gechi ->
        if (gechi.isEmpty()) flowOf(emptyList()) else combine(gechi.map(::enrichedFlow)) { it.toList() }
    }

    private val meseCorrenteInizio: LocalDate = LocalDate.now().withDayOfMonth(1)
    private val meseCorrenteFine: LocalDate = meseCorrenteInizio.plusMonths(1).minusDays(1)

    val uiState: StateFlow<GechiUiState> = combine(
        enrichedList,
        reminderRepository.remindersGenerici(),
        expenseRepository.totaleAllevamentoInRange(meseCorrenteInizio, meseCorrenteFine),
        tagSelezionato,
    ) { enriched, remindersGenerici, speseTotale, tagSel ->
        val remindersPastoGenerici = remindersGenerici.filter { it.tipo == TipoReminder.PASTO && it.attivo }
        val righe = enriched.map { it.toRowUiModel() }
        val pastoOggiCount = enriched.count { it.isPastoPrevistoOggi(remindersPastoGenerici) }
        val tagDisponibili = enriched.flatMap { it.tags }.distinct().sorted()

        GechiUiState(
            caricamento = false,
            righe = righe,
            totaleAnimali = righe.size,
            pastoOggiCount = pastoOggiCount,
            speseMeseCorrente = speseTotale,
            tagDisponibili = tagDisponibili,
            tagSelezionato = tagSel,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GechiUiState())

    fun selezionaTag(tag: String?) {
        tagSelezionato.value = tag
    }

    private fun GeckoEnriched.toRowUiModel() = GeckoRowUiModel(
        id = gecko.id,
        nome = gecko.nome,
        morph = gecko.morph,
        fotoProfiloPath = gecko.fotoProfiloPath,
        coloreTag = gecko.coloreTag,
        pesoLabel = pesoGrammi?.let { "%.1f g".format(it) } ?: "—",
        etaLabel = gecko.etaLabel(context) ?: "—",
        tags = tags,
    )

    private fun GeckoEnriched.isPastoPrevistoOggi(remindersPastoGenerici: List<Reminder>): Boolean {
        val intervallo = (remindersPasto.firstOrNull() ?: remindersPastoGenerici.firstOrNull())?.intervalloGiorni ?: 1
        val ultimaData = ultimoPasto?.dataOra?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: return true
        return ChronoUnit.DAYS.between(ultimaData, LocalDate.now()) >= intervallo
    }
}
