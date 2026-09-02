package com.terra.geckocare.data.repository

import com.terra.geckocare.data.local.dao.GeckoPhotoDao
import com.terra.geckocare.data.local.entity.GeckoPhoto
import com.terra.geckocare.data.photo.PhotoStorageManager
import kotlinx.coroutines.flow.Flow

class GeckoPhotoRepository(
    private val geckoPhotoDao: GeckoPhotoDao,
    private val photoStorageManager: PhotoStorageManager,
) {
    fun photosForGecko(geckoId: Long): Flow<List<GeckoPhoto>> = geckoPhotoDao.photosForGecko(geckoId)

    suspend fun insert(photo: GeckoPhoto): Long = geckoPhotoDao.insert(photo)

    suspend fun delete(photo: GeckoPhoto) {
        geckoPhotoDao.delete(photo)
        photoStorageManager.deletePhoto(photo.path)
    }
}
