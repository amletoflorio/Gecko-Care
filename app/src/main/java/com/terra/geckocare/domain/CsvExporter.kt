package com.terra.geckocare.domain

import com.terra.geckocare.data.local.entity.Expense
import com.terra.geckocare.data.local.entity.Feeding
import com.terra.geckocare.data.local.entity.Gecko
import com.terra.geckocare.data.local.entity.GrowthLog
import com.terra.geckocare.data.local.entity.ShedLog
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dataOraFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val dataFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val zona = ZoneId.systemDefault()

private fun campo(v: Any?): String {
    val s = v?.toString() ?: ""
    return if (s.contains(',') || s.contains('"') || s.contains('\n')) {
        "\"${s.replace("\"", "\"\"")}\""
    } else {
        s
    }
}

private fun riga(vararg campi: Any?): String = campi.joinToString(",") { campo(it) }

/**
 * Costruisce un unico CSV con 4 sezioni (pasti, crescita, mute, spese).
 * Se [nomiGechi] è vuoto (o ha una sola voce) si assume export di un solo
 * animale e la colonna "geco" non viene ripetuta inutilmente; per l'export
 * dell'intero allevamento invece la colonna aiuta a distinguere le righe.
 */
fun buildCsvExport(
    nomiGechi: Map<Long, String>,
    feedings: List<Feeding>,
    growthLogs: List<GrowthLog>,
    shedLogs: List<ShedLog>,
    expenses: List<Expense>,
): String {
    val sb = StringBuilder()

    sb.appendLine("== PASTI ==")
    sb.appendLine(riga("geco", "data_ora", "tipo_preda", "quantita", "esito", "calcio", "d3", "multivitaminico", "note"))
    feedings.sortedBy { it.dataOra }.forEach {
        sb.appendLine(
            riga(
                nomiGechi[it.geckoId] ?: it.geckoId,
                it.dataOra.atZone(zona).format(dataOraFormatter),
                it.tipoPreda,
                it.quantita,
                it.esito.name,
                it.conCalcio,
                it.conD3,
                it.conMultivitaminico,
                it.note,
            ),
        )
    }

    sb.appendLine()
    sb.appendLine("== CRESCITA ==")
    sb.appendLine(riga("geco", "data_ora", "peso_grammi", "lunghezza_cm"))
    growthLogs.sortedBy { it.dataOra }.forEach {
        sb.appendLine(riga(nomiGechi[it.geckoId] ?: it.geckoId, it.dataOra.atZone(zona).format(dataOraFormatter), it.pesoGrammi, it.lunghezzaCm))
    }

    sb.appendLine()
    sb.appendLine("== MUTE ==")
    sb.appendLine(riga("geco", "data_ora", "completa", "note"))
    shedLogs.sortedBy { it.dataOra }.forEach {
        sb.appendLine(riga(nomiGechi[it.geckoId] ?: it.geckoId, it.dataOra.atZone(zona).format(dataOraFormatter), it.completa, it.note))
    }

    sb.appendLine()
    sb.appendLine("== SPESE ==")
    sb.appendLine(riga("geco", "data", "categoria", "importo", "note"))
    expenses.sortedBy { it.data }.forEach {
        sb.appendLine(
            riga(
                it.geckoId?.let { id -> nomiGechi[id] ?: id } ?: "allevamento",
                it.data.format(dataFormatter),
                it.categoria.name,
                it.importo,
                it.note,
            ),
        )
    }

    return sb.toString()
}
