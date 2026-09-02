package com.terra.geckocare.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.GeckoPhoto
import com.terra.geckocare.data.photo.PhotoStorageManager
import com.terra.geckocare.data.repository.GeckoPhotoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val geckoPhotoRepository: GeckoPhotoRepository,
    private val photoStorageManager: PhotoStorageManager,
) : ViewModel() {

    private val geckoId = MutableStateFlow<Long?>(null)

    fun selezionaGecko(id: Long) {
        geckoId.value = id
    }

    val foto: StateFlow<List<GeckoPhoto>> = geckoId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else geckoPhotoRepository.photosForGecko(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _erroreFoto = MutableSharedFlow<Unit>()
    val erroreFoto: SharedFlow<Unit> = _erroreFoto

    fun aggiungiFoto(geckoId: Long, uri: Uri) {
        viewModelScope.launch {
            val path = runCatching { photoStorageManager.savePhoto(uri) }.getOrElse {
                _erroreFoto.emit(Unit)
                return@launch
            }
            geckoPhotoRepository.insert(GeckoPhoto(geckoId = geckoId, path = path))
        }
    }

    fun eliminaFoto(photo: GeckoPhoto) {
        viewModelScope.launch { geckoPhotoRepository.delete(photo) }
    }
}
