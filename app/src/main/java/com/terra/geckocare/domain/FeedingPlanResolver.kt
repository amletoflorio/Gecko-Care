package com.terra.geckocare.domain

import com.terra.geckocare.data.local.entity.EsitoPasto
import com.terra.geckocare.data.local.entity.Feeding
import com.terra.geckocare.data.local.entity.FeedingPlanRule
import com.terra.geckocare.data.local.entity.FeedingPlanStep
import com.terra.geckocare.data.local.entity.Gecko
import java.time.ZoneId

/**
 * Determina la fascia d'età applicabile a un'età (in mesi compiuti): tra le regole
 * candidate, un override specifico del geco ([FeedingPlanRule.geckoId] non
 * nullo) ha sempre priorità sulla regola di default per la stessa fascia.
 */
fun regolaAttuale(regole: List<FeedingPlanRule>, etaMesi: Long?): FeedingPlanRule? {
    if (etaMesi == null) return null
    val specifica = regole.firstOrNull { it.geckoId != null && etaMesi in it.etaMinMesi..it.etaMaxMesi }
    if (specifica != null) return specifica
    return regole.firstOrNull { it.geckoId == null && etaMesi in it.etaMinMesi..it.etaMaxMesi }
}

/** Ordina le fasce per età crescente, utile per la vista "Piano per età". */
fun List<FeedingPlanRule>.ordinatePerEta(): List<FeedingPlanRule> = sortedBy { it.etaMinMesi }

/**
 * Pasto previsto dallo schema (n° [numeroPasto] di un ciclo lungo [lunghezzaCiclo]):
 * integratori da spolverare e tipi di [insetti] da offrire (vuoto = non specificati).
 */
data class IntegratoriPrevisti(
    val numeroPasto: Int,
    val lunghezzaCiclo: Int,
    val conCalcio: Boolean,
    val conD3: Boolean,
    val conMultivitaminico: Boolean,
    val insetti: List<String> = emptyList(),
) {
    /** Vero se il pasto registrato ha esattamente gli integratori previsti (né mancanti né in eccesso). */
    fun rispettatoDa(pasto: PastoAggregato): Boolean =
        pasto.conCalcio == conCalcio && pasto.conD3 == conD3 && pasto.conMultivitaminico == conMultivitaminico
}

/**
 * Un pasto "logico": il form salva una riga [Feeding] per ogni specie di preda con
 * lo stesso timestamp, qui le righe vengono riunite.
 */
data class PastoAggregato(
    val dataOra: java.time.Instant,
    val conCalcio: Boolean,
    val conD3: Boolean,
    val conMultivitaminico: Boolean,
)

/**
 * Integratori previsti per il pasto di indice [indice] (0-based) all'interno della fascia.
 * Lo schema si ripete ciclicamente; la lunghezza del ciclo è il numero di pasto più alto
 * presente nello schema, e i pasti non elencati sono senza integratori.
 */
fun integratoriPerPasto(schema: List<FeedingPlanStep>, indice: Int): IntegratoriPrevisti? {
    if (schema.isEmpty()) return null
    val ciclo = schema.maxOf { it.numeroPasto }.coerceAtLeast(1)
    val numero = indice.coerceAtLeast(0) % ciclo + 1
    val step = schema.firstOrNull { it.numeroPasto == numero }
    return IntegratoriPrevisti(
        numeroPasto = numero,
        lunghezzaCiclo = ciclo,
        conCalcio = step?.conCalcio == true,
        conD3 = step?.conD3 == true,
        conMultivitaminico = step?.conMultivitaminico == true,
        insetti = step?.listaInsetti().orEmpty(),
    )
}

/**
 * Pasti accettati che contano per il ciclo della fascia [regola], in ordine cronologico.
 * Si parte dal giorno in cui il geco è entrato nella fascia (se l'età è nota), così il
 * passaggio a una nuova fascia riparte dal pasto 1. I pasti rifiutati non fanno avanzare
 * il ciclo: gli integratori non sono stati assunti e vanno riproposti.
 */
fun pastiNellaFascia(
    feedings: List<Feeding>,
    gecko: Gecko?,
    regola: FeedingPlanRule,
    zona: ZoneId = ZoneId.systemDefault(),
): List<PastoAggregato> {
    val inizio = gecko?.let { it.dataNascita ?: it.dataAcquisizione }
        ?.plusMonths(regola.etaMinMesi.toLong())
        ?.atStartOfDay(zona)
        ?.toInstant()
    return feedings
        .filter { it.esito == EsitoPasto.ACCETTATO && (inizio == null || it.dataOra >= inizio) }
        .groupBy { it.dataOra }
        .toSortedMap()
        .map { (dataOra, righe) ->
            PastoAggregato(
                dataOra = dataOra,
                conCalcio = righe.any { it.conCalcio },
                conD3 = righe.any { it.conD3 },
                conMultivitaminico = righe.any { it.conMultivitaminico },
            )
        }
}

/** Integratori da dare al prossimo pasto secondo la fascia attuale, o null se la fascia non ha uno schema. */
fun prossimiIntegratori(
    feedings: List<Feeding>,
    gecko: Gecko?,
    regola: FeedingPlanRule?,
    schema: List<FeedingPlanStep>,
): IntegratoriPrevisti? {
    if (regola == null) return null
    return integratoriPerPasto(schema, pastiNellaFascia(feedings, gecko, regola).size)
}
