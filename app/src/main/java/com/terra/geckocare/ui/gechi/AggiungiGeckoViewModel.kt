package com.terra.geckocare.ui.gechi

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terra.geckocare.data.local.entity.Gecko
import com.terra.geckocare.data.local.entity.Sesso
import com.terra.geckocare.data.local.entity.StatoGecko
import com.terra.geckocare.data.photo.PhotoStorageManager
import com.terra.geckocare.data.repository.GeckoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

val PaletteColoriGecko = listOf("#5B9C93", "#AF8A63", "#89A082", "#7C93AD")

data class GeckoFormUiState(
    val geckoId: Long? = null,
    val nome: String = "",
    val morph: String = "",
    val sesso: Sesso = Sesso.SCONOSCIUTO,
    val dataNascita: LocalDate? = null,
    val dataAcquisizione: LocalDate? = null,
    val fotoSalvataPath: String? = null,
    val fotoPendingUri: Uri? = null,
    val descrizione: String = "",
    val coloreTag: String = PaletteColoriGecko.first(),
    val tagSelezionati: Set<String> = emptySet(),
    val nomeMancante: Boolean = false,
    val salvataggioInCorso: Boolean = false,
    val salvataggioCompletato: Boolean = false,
    val caricatoPerModifica: Boolean = false,
    val erroreFoto: Boolean = false,
)

class AggiungiGeckoViewModel(
    private val geckoRepository: GeckoRepository,
    private val photoStorageManager: PhotoStorageManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GeckoFormUiState(coloreTag = PaletteColoriGecko.random()),
    )
    val uiState: StateFlow<GeckoFormUiState> = _uiState.asStateFlow()

    fun caricaPerModifica(geckoId: Long) {
        if (_uiState.value.caricatoPerModifica) return
        viewModelScope.launch {
            val gecko = geckoRepository.getByIdOnce(geckoId) ?: return@launch
            val tags = geckoRepository.tagsForGecko(geckoId).first().map { it.etichetta }.toSet()
            _uiState.value = GeckoFormUiState(
                geckoId = gecko.id,
                nome = gecko.nome,
                morph = gecko.morph,
                sesso = gecko.sesso,
                dataNascita = gecko.dataNascita,
                dataAcquisizione = gecko.dataAcquisizione,
                fotoSalvataPath = gecko.fotoProfiloPath,
                descrizione = gecko.descrizione,
                coloreTag = gecko.coloreTag,
                tagSelezionati = tags,
                caricatoPerModifica = true,
            )
        }
    }

    fun onNomeChange(v: String) = update { it.copy(nome = v, nomeMancante = false) }
    fun onMorphChange(v: String) = update { it.copy(morph = v) }
    fun onSessoChange(v: Sesso) = update { it.copy(sesso = v) }
    fun onDataNascitaChange(v: LocalDate?) = update { it.copy(dataNascita = v) }
    fun onDataAcquisizioneChange(v: LocalDate?) = update { it.copy(dataAcquisizione = v) }
    fun onDescrizioneChange(v: String) = update { it.copy(descrizione = v) }
    fun onColoreChange(v: String) = update { it.copy(coloreTag = v) }
    fun onFotoScelta(uri: Uri) = update { it.copy(fotoPendingUri = uri) }

    fun toggleTag(tag: String) = update {
        val nuovi = if (tag in it.tagSelezionati) it.tagSelezionati - tag else it.tagSelezionati + tag
        it.copy(tagSelezionati = nuovi)
    }

    fun salva() {
        val stato = _uiState.value
        if (stato.nome.isBlank()) {
            update { it.copy(nomeMancante = true) }
            return
        }
        update { it.copy(salvataggioInCorso = true, erroreFoto = false) }
        viewModelScope.launch {
            var erroreFoto = false
            val fotoPath = stato.fotoPendingUri?.let { uri ->
                runCatching {
                    stato.fotoSalvataPath?.let { vecchio -> photoStorageManager.deletePhoto(vecchio) }
                    photoStorageManager.savePhoto(uri)
                }.onFailure { erroreFoto = true }.getOrNull()
            } ?: stato.fotoSalvataPath

            val geckoId = if (stato.geckoId != null) {
                val esistente = geckoRepository.getByIdOnce(stato.geckoId)
                geckoRepository.update(
                    Gecko(
                        id = stato.geckoId,
                        nome = stato.nome.trim(),
                        morph = stato.morph.trim(),
                        sesso = stato.sesso,
                        dataNascita = stato.dataNascita,
                        dataAcquisizione = stato.dataAcquisizione,
                        fotoProfiloPath = fotoPath,
                        descrizione = stato.descrizione.trim(),
                        coloreTag = stato.coloreTag,
                        stato = esistente?.stato ?: StatoGecko.ATTIVO,
                        dataCreazione = esistente?.dataCreazione ?: java.time.Instant.now(),
                    ),
                )
                stato.geckoId
            } else {
                geckoRepository.insert(
                    Gecko(
                        nome = stato.nome.trim(),
                        morph = stato.morph.trim(),
                        sesso = stato.sesso,
                        dataNascita = stato.dataNascita,
                        dataAcquisizione = stato.dataAcquisizione,
                        fotoProfiloPath = fotoPath,
                        descrizione = stato.descrizione.trim(),
                        coloreTag = stato.coloreTag,
                    ),
                )
            }

            val tagEsistenti = geckoRepository.tagsForGecko(geckoId).first().map { it.etichetta }.toSet()
            (stato.tagSelezionati - tagEsistenti).forEach { geckoRepository.addTag(geckoId, it) }
            (tagEsistenti - stato.tagSelezionati).forEach { geckoRepository.removeTag(geckoId, it) }

            update { it.copy(salvataggioInCorso = false, salvataggioCompletato = true, erroreFoto = erroreFoto) }
        }
    }

    private inline fun update(block: (GeckoFormUiState) -> GeckoFormUiState) {
        _uiState.value = block(_uiState.value)
    }
}
