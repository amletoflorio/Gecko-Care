package com.terra.geckocare.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.terra.geckocare.data.local.entity.ETA_MESI_SENZA_LIMITE

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
                    (geckoId, etaMinMesi, etaMaxMesi, nomeFase, frequenzaTesto, noteIntegratori)
                VALUES (NULL, ${rule.etaMinMesi}, ${rule.etaMaxMesi}, '${rule.nomeFase}', '${rule.frequenzaTesto}', '${rule.noteIntegratori}')
                """.trimIndent(),
            )
        }
    }

    private data class DefaultRule(
        val etaMinMesi: Int,
        val etaMaxMesi: Int,
        val nomeFase: String,
        val frequenzaTesto: String,
        val noteIntegratori: String,
    )

    companion object {
        // Mesi compiuti, estremi inclusi: fasce indicative, non cliniche.
        private val DEFAULT_FEEDING_PLAN_RULES = listOf(
            DefaultRule(
                etaMinMesi = 0,
                etaMaxMesi = 1,
                nomeFase = "Neonato",
                frequenzaTesto = "Ogni giorno",
                noteIntegratori = "Calcio con D3 quasi ad ogni pasto",
            ),
            DefaultRule(
                etaMinMesi = 2,
                etaMaxMesi = 11,
                nomeFase = "Giovanile",
                frequenzaTesto = "A giorni alterni",
                noteIntegratori = "Calcio con D3 alla maggior parte dei pasti",
            ),
            DefaultRule(
                etaMinMesi = 12,
                etaMaxMesi = 17,
                nomeFase = "Subadulto",
                frequenzaTesto = "3-4 volte a settimana",
                noteIntegratori = "Calcio con D3 2-3 volte a settimana",
            ),
            DefaultRule(
                etaMinMesi = 18,
                etaMaxMesi = ETA_MESI_SENZA_LIMITE,
                nomeFase = "Adulto",
                frequenzaTesto = "2-3 volte a settimana",
                noteIntegratori = "Calcio senza D3 quasi sempre, D3 1x/settimana, multivitaminico 1x/settimana",
            ),
        )
    }
}
