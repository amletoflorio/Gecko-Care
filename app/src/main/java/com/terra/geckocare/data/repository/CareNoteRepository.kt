package com.terra.geckocare.data.repository

import com.terra.geckocare.data.local.dao.CareNoteDao
import com.terra.geckocare.data.local.entity.CareNote
import kotlinx.coroutines.flow.Flow

class CareNoteRepository(
    private val careNoteDao: CareNoteDao,
) {
    fun notesForGecko(geckoId: Long): Flow<List<CareNote>> = careNoteDao.notesForGecko(geckoId)

    suspend fun insert(note: CareNote): Long = careNoteDao.insert(note)

    suspend fun update(note: CareNote) = careNoteDao.update(note)

    suspend fun delete(note: CareNote) = careNoteDao.delete(note)
}
