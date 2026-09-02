package com.terra.geckocare.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Precompila le 4 fasce d'età standard del leopardino al primo avvio (creazione DB).
 * Righe con geckoId NULL = regole di default, modificabili/eliminabili dall'utente
 * e sostituibili da override specifici per animale.
 */
class DefaultDataCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        DEFAULT_FEEDING_PLAN_RULES.forEach { rule ->
            db.execSQL(
                """
                INSERT INTO feeding_plan_rule
                    (geckoId, etaMinGiorni, etaMaxGiorni, nomeFase, frequenzaTesto, noteIntegratori)
                VALUES (NULL, ${rule.etaMinGiorni}, ${rule.etaMaxGiorni}, '${rule.nomeFase}', '${rule.frequenzaTesto}', '${rule.noteIntegratori}')
                """.trimIndent(),
            )
        }
    }

    private data class DefaultRule(
        val etaMinGiorni: Int,
        val etaMaxGiorni: Int,
        val nomeFase: String,
        val frequenzaTesto: String,
        val noteIntegratori: String,
    )

    companion object {
        // 1 mese approssimato a 30 giorni: coerente con l'uso comune in ambito
        // erpetologico amatoriale, dove le fasce sono indicative e non cliniche.
        private val DEFAULT_FEEDING_PLAN_RULES = listOf(
            DefaultRule(
                etaMinGiorni = 0,
                etaMaxGiorni = 60,
                nomeFase = "Neonato",
                frequenzaTesto = "Ogni giorno",
                noteIntegratori = "Calcio con D3 quasi ad ogni pasto",
            ),
            DefaultRule(
                etaMinGiorni = 61,
                etaMaxGiorni = 365,
                nomeFase = "Giovanile",
                frequenzaTesto = "A giorni alterni",
                noteIntegratori = "Calcio con D3 alla maggior parte dei pasti",
            ),
            DefaultRule(
                etaMinGiorni = 366,
                etaMaxGiorni = 548,
                nomeFase = "Subadulto",
                frequenzaTesto = "3-4 volte a settimana",
                noteIntegratori = "Calcio con D3 2-3 volte a settimana",
            ),
            DefaultRule(
                etaMinGiorni = 549,
                etaMaxGiorni = 999999,
                nomeFase = "Adulto",
                frequenzaTesto = "2-3 volte a settimana",
                noteIntegratori = "Calcio senza D3 quasi sempre, D3 1x/settimana, multivitaminico 1x/settimana",
            ),
        )
    }
}
