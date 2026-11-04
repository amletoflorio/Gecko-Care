package com.terra.geckocare.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v3: aggiunge il registro delle defecazioni. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `defecation_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `geckoId` INTEGER NOT NULL, " +
                "`dataOra` INTEGER NOT NULL, `normale` INTEGER NOT NULL, `note` TEXT NOT NULL, " +
                "FOREIGN KEY(`geckoId`) REFERENCES `gecko`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_defecation_log_geckoId` ON `defecation_log` (`geckoId`)")
    }
}

/**
 * v4: fasce del piano in mesi invece che in giorni (1 mese ≈ 30 giorni, estremi
 * inclusi: 0–60 gg → 0–1 mesi, 61–365 → 2–11, ...) e schema integratori per pasto.
 * SQLite < 3.25 (minSdk 26) non supporta RENAME COLUMN: la tabella viene ricreata.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `feeding_plan_rule_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `geckoId` INTEGER, " +
                "`etaMinMesi` INTEGER NOT NULL, `etaMaxMesi` INTEGER NOT NULL, `nomeFase` TEXT NOT NULL, `frequenzaTesto` TEXT NOT NULL, " +
                "`noteIntegratori` TEXT NOT NULL, FOREIGN KEY(`geckoId`) REFERENCES `gecko`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL(
            """
            INSERT INTO feeding_plan_rule_new (id, geckoId, etaMinMesi, etaMaxMesi, nomeFase, frequenzaTesto, noteIntegratori)
            SELECT id, geckoId,
                CAST(ROUND(etaMinGiorni / 30.0) AS INTEGER),
                MIN(MAX(CAST(ROUND((etaMaxGiorni + 1) / 30.0) AS INTEGER) - 1, CAST(ROUND(etaMinGiorni / 30.0) AS INTEGER)), 999),
                nomeFase, frequenzaTesto, noteIntegratori
            FROM feeding_plan_rule
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE feeding_plan_rule")
        db.execSQL("ALTER TABLE feeding_plan_rule_new RENAME TO feeding_plan_rule")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_feeding_plan_rule_geckoId` ON `feeding_plan_rule` (`geckoId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `feeding_plan_step` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ruleId` INTEGER NOT NULL, " +
                "`numeroPasto` INTEGER NOT NULL, `conCalcio` INTEGER NOT NULL, `conD3` INTEGER NOT NULL, `conMultivitaminico` INTEGER NOT NULL, " +
                "FOREIGN KEY(`ruleId`) REFERENCES `feeding_plan_rule`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_feeding_plan_step_ruleId` ON `feeding_plan_step` (`ruleId`)")
    }
}

/** v5: genitori del geco (genetica) e tipi di insetti per pasto nello schema del piano. */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `gecko` ADD COLUMN `padreId` INTEGER")
        db.execSQL("ALTER TABLE `gecko` ADD COLUMN `madreId` INTEGER")
        db.execSQL("ALTER TABLE `gecko` ADD COLUMN `padreEsterno` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `gecko` ADD COLUMN `madreEsterno` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `feeding_plan_step` ADD COLUMN `insetti` TEXT NOT NULL DEFAULT ''")
    }
}

/** v6: foto dei genitori esterni. */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `gecko` ADD COLUMN `padreEsternoFotoPath` TEXT")
        db.execSQL("ALTER TABLE `gecko` ADD COLUMN `madreEsternoFotoPath` TEXT")
    }
}

/** v7: raggruppamento delle voci di spesa in un unico acquisto. */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `expense` ADD COLUMN `gruppo` TEXT")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expense_gruppo` ON `expense` (`gruppo`)")
    }
}
