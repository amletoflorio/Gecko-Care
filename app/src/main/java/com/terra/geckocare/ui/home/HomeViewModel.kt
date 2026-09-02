package com.terra.geckocare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.CareNote
import com.terra.geckocare.data.local.entity.Feeding
import com.terra.geckocare.data.local.entity.FeedingPlanRule
import com.terra.geckocare.data.local.entity.Gecko
import com.terra.geckocare.data.local.entity.GrowthLog
import com.terra.geckocare.data.local.entity.Reminder
import com.terra.geckocare.data.local.entity.ShedLog
import com.terra.geckocare.data.local.entity.TipoReminder
import com.terra.geckocare.data.repository.CareNoteRepository
import com.terra.geckocare.data.repository.EnvironmentRepository
import com.terra.geckocare.data.repository.FeedingPlanRepository
import com.terra.geckocare.data.repository.FeedingRepository
import com.terra.geckocare.data.repository.GeckoRepository
import com.terra.geckocare.data.repository.GrowthRepository
import com.terra.geckocare.data.repository.ReminderRepository
import com.terra.geckocare.data.repository.ShedRepository
import com.terra.geckocare.domain.ActivityItem
import com.terra.geckocare.domain.ActivityKind
import com.terra.geckocare.domain.INTERVALLO_MUTA_DEFAULT_GIORNI
import com.terra.geckocare.domain.etaInGiorni
import com.terra.geckocare.domain.etaLabel
import com.terra.geckocare.domain.formatAttivitaTimestamp
import com.terra.geckocare.domain.intervalloMedioMuta
import com.terra.geckocare.domain.regolaAttuale
import com.terra.geckocare.worker.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class HomeUiState(
    val caricamento: Boolean = true,
    val gecko: Gecko? = null,
    val etaLabel: String = "—",
    val prossimoPastoLabel: String = "—",
    val pesoAttualeLabel: String = "—",
    val giorniMutaLabel: String = "—",
    val hotSpotOraLabel: String = "—",
    val pesoValore: String = "—",
    val pesoTrendSu: Boolean? = null,
    val lunghezzaValore: String = "—",
    val lunghezzaTrendSu: Boolean? = null,
    val pianoNomeFase: String? = null,
    val pianoDettaglio: String = "",
    val shareCode: String = "",
    val qrContent: String = "",
    val attivitaRecente: List<ActivityItem> = emptyList(),
    val hotSpotC: String = "—",
    val coldSpotC: String = "—",
    val umidita: String = "—",
    val notturnaC: String = "—",
    val mutaGiorniTrascorsi: Int? = null,
    val mutaIntervalloTipico: Int = INTERVALLO_MUTA_DEFAULT_GIORNI,
    val noteComportamentali: List<CareNote> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val letture: List<com.terra.geckocare.data.local.entity.EnvironmentLog> = emptyList(),
    val muteStoriche: List<ShedLog> = emptyList(),
    val crescitaStorica: List<GrowthLog> = emptyList(),
)

private data class NucleoBase(
    val gecko: Gecko?,
    val growthLogs: List<GrowthLog>,
    val latestEnv: com.terra.geckocare.data.local.entity.EnvironmentLog?,
    val shedLogsComplete: List<ShedLog>,
    val shedLogsAll: List<ShedLog>,
    val latestFeeding: Feeding?,
)

private data class NucleoExtra(
    val planRules: List<FeedingPlanRule>,
    val remindersGecko: List<Reminder>,
    val remindersGenerici: List<Reminder>,
    val careNotes: List<CareNote>,
    val recentFeedings: List<Feeding>,
)

class HomeViewModel(
    private val context: android.content.Context,
    private val geckoRepository: GeckoRepository,
    private val growthRepository: GrowthRepository,
    private val shedRepository: ShedRepository,
    private val feedingRepository: FeedingRepository,
    private val careNoteRepository: CareNoteRepository,
    private val environmentRepository: EnvironmentRepository,
    private val feedingPlanRepository: FeedingPlanRepository,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    private val geckoId = MutableStateFlow<Long?>(null)

    fun selezionaGecko(id: Long) {
        geckoId.value = id
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val aggiornato = reminder.copy(attivo = !reminder.attivo)
            reminderRepository.update(aggiornato)
            reminderScheduler.schedule(aggiornato)
        }
    }

    private val nucleoBase = geckoId.flatMapLatest { id ->
        if (id == null) {
            flowOf(NucleoBase(null, emptyList(), null, emptyList(), emptyList(), null))
        } else {
            combine(
                geckoRepository.getById(id),
                growthRepository.logsForGecko(id),
                environmentRepository.latestForGecko(id),
                shedRepository.logsForGecko(id),
                feedingRepository.latestForGecko(id),
            ) { gecko, growth, env, sheds, feeding ->
                NucleoBase(gecko, growth, env, sheds.filter { it.completa }, sheds, feeding)
            }
        }
    }

    private val nucleoExtra = geckoId.flatMapLatest { id ->
        if (id == null) {
            flowOf(NucleoExtra(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()))
        } else {
            combine(
                feedingPlanRepository.rulesForGeckoOrDefault(id),
                reminderRepository.remindersForGecko(id),
                reminderRepository.remindersGenerici(),
                careNoteRepository.notesForGecko(id),
                feedingRepository.feedingsForGecko(id),
            ) { rules, remindersGecko, remindersGenerici, notes, feedings ->
                NucleoExtra(rules, remindersGecko, remindersGenerici, notes, feedings.take(8))
            }
        }
    }

    private val envLogs = geckoId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else environmentRepository.logsForGecko(id)
    }

    val uiState: StateFlow<HomeUiState> = combine(nucleoBase, nucleoExtra, envLogs) { base, extra, logs ->
        buildUiState(base, extra, logs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private fun buildUiState(base: NucleoBase, extra: NucleoExtra, envLogs: List<com.terra.geckocare.data.local.entity.EnvironmentLog>): HomeUiState {
        val gecko = base.gecko ?: return HomeUiState(caricamento = false)

        val zona = ZoneId.systemDefault()
        val oggi = java.time.LocalDate.now()

        val pesoOrdinati = base.growthLogs.mapNotNull { it.pesoGrammi?.let { p -> it.dataOra to p } }.sortedBy { it.first }
        val lunghezzaOrdinate = base.growthLogs.mapNotNull { it.lunghezzaCm?.let { l -> it.dataOra to l } }.sortedBy { it.first }
        val pesoAttuale = pesoOrdinati.lastOrNull()?.second
        val lunghezzaAttuale = lunghezzaOrdinate.lastOrNull()?.second
        val pesoTrend = if (pesoOrdinati.size >= 2) pesoOrdinati.last().second > pesoOrdinati[pesoOrdinati.size - 2].second else null
        val lunghezzaTrend = if (lunghezzaOrdinate.size >= 2) lunghezzaOrdinate.last().second > lunghezzaOrdinate[lunghezzaOrdinate.size - 2].second else null

        val ultimaMuta = base.shedLogsComplete.maxByOrNull { it.dataOra }
        val giorniMuta = ultimaMuta?.let { ChronoUnit.DAYS.between(it.dataOra.atZone(zona).toLocalDate(), oggi).toInt() }
        val intervalloTipico = intervalloMedioMuta(base.shedLogsComplete) ?: INTERVALLO_MUTA_DEFAULT_GIORNI

        val reminderPasto = extra.remindersGecko.firstOrNull { it.tipo == TipoReminder.PASTO && it.attivo }
            ?: extra.remindersGenerici.firstOrNull { it.tipo == TipoReminder.PASTO && it.attivo }
        val intervalloPasto = reminderPasto?.intervalloGiorni ?: 1
        val giorniDaUltimoPasto = base.latestFeeding?.dataOra?.let {
            ChronoUnit.DAYS.between(it.atZone(zona).toLocalDate(), oggi)
        }
        val giorniAlProssimoPasto = (intervalloPasto - (giorniDaUltimoPasto ?: 0)).coerceAtLeast(0)
        val prossimoPastoLabel = if (giorniDaUltimoPasto == null || giorniAlProssimoPasto <= 0) {
            context.getString(com.terra.geckocare.R.string.home_oggi)
        } else {
            context.getString(com.terra.geckocare.R.string.home_giorni_suffix, giorniAlProssimoPasto)
        }

        val etaGiorni = gecko.etaInGiorni()
        val regola = regolaAttuale(extra.planRules, etaGiorni)

        val attivita = buildList {
            base.latestFeeding?.let {
                val testoBase = context.getString(com.terra.geckocare.R.string.activity_pasto, it.quantita, it.tipoPreda.lowercase())
                val suffisso = if (it.conD3) {
                    " · ${context.getString(com.terra.geckocare.R.string.activity_calcio_d3)}"
                } else if (it.conCalcio) {
                    " · ${context.getString(com.terra.geckocare.R.string.activity_calcio)}"
                } else {
                    ""
                }
                add(ActivityItem(ActivityKind.PASTO, testoBase + suffisso, it.dataOra))
            }
            extra.recentFeedings.drop(1).take(3).forEach {
                add(ActivityItem(ActivityKind.PASTO, context.getString(com.terra.geckocare.R.string.activity_pasto, it.quantita, it.tipoPreda.lowercase()), it.dataOra))
            }
            pesoOrdinati.lastOrNull()?.let { (data, peso) ->
                add(ActivityItem(ActivityKind.PESO, context.getString(com.terra.geckocare.R.string.activity_peso_registrato, peso), data))
            }
            ultimaMuta?.let { add(ActivityItem(ActivityKind.MUTA, context.getString(com.terra.geckocare.R.string.activity_muta_completata), it.dataOra)) }
            extra.careNotes.firstOrNull()?.let { add(ActivityItem(ActivityKind.NOTA, it.testo, it.dataOra)) }
        }.sortedByDescending { it.timestamp }.take(5)

        val etaLabelTxt = gecko.etaLabel(context) ?: context.getString(com.terra.geckocare.R.string.eta_sconosciuta)
        val pesoLabelTxt = pesoAttuale?.let { "%.1f g".format(it) } ?: "—"
        val pianoDettaglioTxt = regola?.let { "${it.frequenzaTesto} · ${it.noteIntegratori}" } ?: context.getString(com.terra.geckocare.R.string.home_piano_dettaglio_vuoto)
        val shareCode = "GECKO-${gecko.id.toString().padStart(6, '0')}"

        val qrContent = buildList {
            add(shareCode)
            add(context.getString(com.terra.geckocare.R.string.qr_export_header, gecko.nome))
            if (gecko.morph.isNotBlank()) add(context.getString(com.terra.geckocare.R.string.qr_export_morph, gecko.morph))
            add(context.getString(com.terra.geckocare.R.string.qr_export_eta, etaLabelTxt))
            if (pesoAttuale != null) add(context.getString(com.terra.geckocare.R.string.qr_export_peso, pesoLabelTxt))
            add(context.getString(com.terra.geckocare.R.string.qr_export_piano, pianoDettaglioTxt))
        }.joinToString("\n")

        return HomeUiState(
            caricamento = false,
            gecko = gecko,
            etaLabel = etaLabelTxt,
            prossimoPastoLabel = prossimoPastoLabel,
            pesoAttualeLabel = pesoLabelTxt,
            giorniMutaLabel = giorniMuta?.let { context.getString(com.terra.geckocare.R.string.home_giorni_suffix, it) } ?: "—",
            hotSpotOraLabel = base.latestEnv?.hotSpotC?.let { "%.0f°C".format(it) } ?: "—",
            pesoValore = pesoLabelTxt,
            pesoTrendSu = pesoTrend,
            lunghezzaValore = lunghezzaAttuale?.let { "%.1f cm".format(it) } ?: "—",
            lunghezzaTrendSu = lunghezzaTrend,
            pianoNomeFase = regola?.nomeFase,
            pianoDettaglio = pianoDettaglioTxt,
            shareCode = shareCode,
            qrContent = qrContent,
            attivitaRecente = attivita,
            hotSpotC = base.latestEnv?.hotSpotC?.let { "%.0f°C".format(it) } ?: "—",
            coldSpotC = base.latestEnv?.coldSpotC?.let { "%.0f°C".format(it) } ?: "—",
            umidita = base.latestEnv?.umiditaPercento?.let { "%.0f%%".format(it) } ?: "—",
            notturnaC = base.latestEnv?.temperaturaNotturnaC?.let { "%.0f°C".format(it) } ?: "—",
            mutaGiorniTrascorsi = giorniMuta,
            mutaIntervalloTipico = intervalloTipico,
            noteComportamentali = extra.careNotes,
            reminders = extra.remindersGecko,
            letture = envLogs.take(5),
            muteStoriche = base.shedLogsAll.take(5),
            crescitaStorica = base.growthLogs.takeLast(5).reversed(),
        )
    }
}

fun ActivityItem.timestampLabel(context: android.content.Context): String = formatAttivitaTimestamp(context, timestamp)
