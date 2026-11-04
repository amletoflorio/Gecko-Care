package com.terra.geckocare.domain

import com.terra.geckocare.data.local.entity.EsitoPasto
import com.terra.geckocare.data.local.entity.Feeding
import com.terra.geckocare.data.local.entity.FeedingPlanRule
import com.terra.geckocare.data.local.entity.FeedingPlanStep
import com.terra.geckocare.data.local.entity.Gecko
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class FeedingPlanResolverTest {

    private val zona = ZoneOffset.UTC
    private val regola = FeedingPlanRule(id = 1, etaMinMesi = 3, etaMaxMesi = 12, nomeFase = "Juvenile", frequenzaTesto = "", noteIntegratori = "")
    private val schema = listOf(
        FeedingPlanStep(ruleId = 1, numeroPasto = 1, conCalcio = true, conD3 = true),
        FeedingPlanStep(ruleId = 1, numeroPasto = 2, conCalcio = true),
        FeedingPlanStep(ruleId = 1, numeroPasto = 4, conMultivitaminico = true),
    )
    private val gecko = Gecko(id = 1, nome = "Test", dataNascita = LocalDate.of(2026, 1, 1))

    private fun pasto(giorno: LocalDate, esito: EsitoPasto = EsitoPasto.ACCETTATO, preda: String = "grillo") =
        Feeding(geckoId = 1, dataOra = giorno.atTime(18, 0).toInstant(zona), tipoPreda = preda, esito = esito)

    @Test
    fun `schema ciclico con pasti non elencati senza integratori`() {
        val terzo = integratoriPerPasto(schema, 2)!!
        assertEquals(3, terzo.numeroPasto)
        assertEquals(4, terzo.lunghezzaCiclo)
        assertFalse(terzo.conCalcio || terzo.conD3 || terzo.conMultivitaminico)

        val quinto = integratoriPerPasto(schema, 4)!!
        assertEquals(1, quinto.numeroPasto)
        assertTrue(quinto.conCalcio && quinto.conD3)
    }

    @Test
    fun `schema vuoto non produce previsione`() {
        assertNull(integratoriPerPasto(emptyList(), 0))
    }

    @Test
    fun `conta solo pasti accettati dall'inizio della fascia e unisce le prede`() {
        val inizioFascia = LocalDate.of(2026, 4, 1)
        val feedings = listOf(
            pasto(inizioFascia.minusDays(2)),
            pasto(inizioFascia.plusDays(1)),
            pasto(inizioFascia.plusDays(1), preda = "dubia"),
            pasto(inizioFascia.plusDays(3), esito = EsitoPasto.RIFIUTATO),
            pasto(inizioFascia.plusDays(4)),
        )
        val pasti = pastiNellaFascia(feedings, gecko, regola, zona)
        assertEquals(2, pasti.size)
        assertEquals(3, integratoriPerPasto(schema, pasti.size)!!.numeroPasto)
    }

    @Test
    fun `rispettato richiede esattamente gli integratori previsti`() {
        val previsto = integratoriPerPasto(schema, 0)!!
        val soloD3 = PastoAggregato(java.time.Instant.EPOCH, conCalcio = false, conD3 = true, conMultivitaminico = false)
        val calcioD3 = soloD3.copy(conCalcio = true)
        assertFalse(previsto.rispettatoDa(soloD3))
        assertTrue(previsto.rispettatoDa(calcioD3))
    }

    @Test
    fun `insetti dello step riportati nel pasto previsto`() {
        val conInsetti = listOf(
            FeedingPlanStep(ruleId = 1, numeroPasto = 1, conCalcio = true, insetti = "Grilli, Dubia"),
            FeedingPlanStep(ruleId = 1, numeroPasto = 2, insetti = " , Tarme ,"),
        )
        assertEquals(listOf("Grilli", "Dubia"), integratoriPerPasto(conInsetti, 0)!!.insetti)
        assertEquals(listOf("Tarme"), integratoriPerPasto(conInsetti, 1)!!.insetti)
        assertTrue(integratoriPerPasto(schema, 2)!!.insetti.isEmpty())
    }
}
