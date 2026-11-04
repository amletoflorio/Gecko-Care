package com.terra.geckocare.data.repository

import com.terra.geckocare.data.local.dao.GeckoDao
import com.terra.geckocare.data.local.dao.GeckoTagDao
import com.terra.geckocare.data.local.entity.Gecko
import com.terra.geckocare.data.local.entity.GeckoTag
import kotlinx.coroutines.flow.Flow

class GeckoRepository(
    private val geckoDao: GeckoDao,
    private val geckoTagDao: GeckoTagDao,
) {
    fun getAll(): Flow<List<Gecko>> = geckoDao.getAll()

    fun getAllAttivi(): Flow<List<Gecko>> = geckoDao.getAllAttivi()

    fun getById(id: Long): Flow<Gecko?> = geckoDao.getById(id)

    suspend fun getByIdOnce(id: Long): Gecko? = geckoDao.getByIdOnce(id)

    fun figli(id: Long): Flow<List<Gecko>> = geckoDao.figli(id)

    fun countAttivi(): Flow<Int> = geckoDao.countAttivi()

    suspend fun insert(gecko: Gecko): Long = geckoDao.insert(gecko)

    suspend fun update(gecko: Gecko) = geckoDao.update(gecko)

    suspend fun delete(gecko: Gecko) {
        geckoDao.scollegaPadre(gecko.id, gecko.nome, gecko.fotoProfiloPath)
        geckoDao.scollegaMadre(gecko.id, gecko.nome, gecko.fotoProfiloPath)
        geckoDao.delete(gecko)
    }

    fun tagsForGecko(geckoId: Long): Flow<List<GeckoTag>> = geckoTagDao.tagsForGecko(geckoId)

    fun tagsAll(): Flow<List<GeckoTag>> = geckoTagDao.tagsAll()

    fun etichetteDistinte(): Flow<List<String>> = geckoTagDao.etichetteDistinte()

    suspend fun addTag(geckoId: Long, etichetta: String) {
        val pulita = etichetta.trim()
        if (pulita.isNotEmpty()) {
            geckoTagDao.insert(GeckoTag(geckoId = geckoId, etichetta = pulita))
        }
    }

    suspend fun removeTag(geckoId: Long, etichetta: String) =
        geckoTagDao.deleteByLabel(geckoId, etichetta)
}
