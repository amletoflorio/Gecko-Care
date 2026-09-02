package com.terra.geckocare.data.local

import androidx.room.TypeConverter
import com.terra.geckocare.data.local.entity.CategoriaSpesa
import com.terra.geckocare.data.local.entity.EsitoPasto
import com.terra.geckocare.data.local.entity.Sesso
import com.terra.geckocare.data.local.entity.StatoGecko
import com.terra.geckocare.data.local.entity.TipoReminder
import java.time.Instant
import java.time.LocalDate

/**
 * Tutte le date sono persistite come epoch-day (Long), tutti i timestamp come
 * epoch-millis (Long): evita ambiguità di timezone e mantiene le colonne ordinabili.
 */
class Converters {

    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromEpochMilli(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun instantToEpochMilli(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toSesso(value: String?): Sesso? = value?.let { Sesso.valueOf(it) }

    @TypeConverter
    fun fromSesso(value: Sesso?): String? = value?.name

    @TypeConverter
    fun toStatoGecko(value: String?): StatoGecko? = value?.let { StatoGecko.valueOf(it) }

    @TypeConverter
    fun fromStatoGecko(value: StatoGecko?): String? = value?.name

    @TypeConverter
    fun toEsitoPasto(value: String?): EsitoPasto? = value?.let { EsitoPasto.valueOf(it) }

    @TypeConverter
    fun fromEsitoPasto(value: EsitoPasto?): String? = value?.name

    @TypeConverter
    fun toCategoriaSpesa(value: String?): CategoriaSpesa? = value?.let { CategoriaSpesa.valueOf(it) }

    @TypeConverter
    fun fromCategoriaSpesa(value: CategoriaSpesa?): String? = value?.name

    @TypeConverter
    fun toTipoReminder(value: String?): TipoReminder? = value?.let { TipoReminder.valueOf(it) }

    @TypeConverter
    fun fromTipoReminder(value: TipoReminder?): String? = value?.name
}
