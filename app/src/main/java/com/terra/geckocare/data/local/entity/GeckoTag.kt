package com.terra.geckocare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tag libero assegnato a un geco (predefinito o creato dall'utente).
 * Nessuna tabella "catalogo tag" separata: i tag suggeriti (riproduttore,
 * in vendita, quarantena, cucciolo...) vivono lato UI, quelli custom sono
 * semplicemente nuove righe con la stessa [etichetta].
 */
@Entity(
    tableName = "gecko_tag",
    foreignKeys = [
        ForeignKey(
            entity = Gecko::class,
            parentColumns = ["id"],
            childColumns = ["geckoId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("geckoId"), Index(value = ["geckoId", "etichetta"], unique = true)],
)
data class GeckoTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val geckoId: Long,
    val etichetta: String,
)
