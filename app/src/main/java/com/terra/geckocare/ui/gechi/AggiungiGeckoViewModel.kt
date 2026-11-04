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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Genitore scelto nel form: un geco dell'allevamento ([geckoId]) oppure un nome libero
 * ([nomeEsterno]) con foto facoltativa, già salvata ([fotoSalvataPath]) o appena scelta ([fotoPendingUri]).
 */
data class GenitoreScelto(
    val geckoId: Long? = null,
    val nomeEsterno: String = "",
    val fotoSalvataPath: String? = null,
    val fotoPendingUri: Uri? = null,
) {
    val impostato: Boolean get() = geckoId != null || nomeEsterno.isNotBlank()
}

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
    val padre: GenitoreScelto = GenitoreScelto(),
    val madre: GenitoreScelto = GenitoreScelto(),
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

    /** Gechi selezionabili come genitori (tutti, anche deceduti/ceduti: la genealogia resta valida). */
    val gechi: StateFlow<List<Gecko>> = geckoRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
                padre = GenitoreScelto(gecko.padreId, gecko.padreEsterno, gecko.padreEsternoFotoPath),
                madre = GenitoreScelto(gecko.madreId, gecko.madreEsterno, gecko.madreEsternoFotoPath),
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
    fun onPadreChange(v: GenitoreScelto) = update { it.copy(padre = v) }
    fun onMadreChange(v: GenitoreScelto) = update { it.copy(madre = v) }
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

            val esistentePrima = stato.geckoId?.let { geckoRepository.getByIdOnce(it) }
            val fotoPadre = salvaFotoGenitore(stato.padre, esistentePrima?.padreEsternoFotoPath) { erroreFoto = true }
            val fotoMadre = salvaFotoGenitore(stato.madre, esistentePrima?.madreEsternoFotoPath) { erroreFoto = true }

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
                        padreId = stato.padre.geckoId,
                        madreId = stato.madre.geckoId,
                        padreEsterno = stato.padre.nomeEsterno.trim(),
                        madreEsterno = stato.madre.nomeEsterno.trim(),
                        padreEsternoFotoPath = fotoPadre,
                        madreEsternoFotoPath = fotoMadre,
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
                        padreId = stato.padre.geckoId,
                        madreId = stato.madre.geckoId,
                        padreEsterno = stato.padre.nomeEsterno.trim(),
                        madreEsterno = stato.madre.nomeEsterno.trim(),
                        padreEsternoFotoPath = fotoPadre,
                        madreEsternoFotoPath = fotoMadre,
                    ),
                )
            }

            val tagEsistenti = geckoRepository.tagsForGecko(geckoId).first().map { it.etichetta }.toSet()
            (stato.tagSelezionati - tagEsistenti).forEach { geckoRepository.addTag(geckoId, it) }
            (tagEsistenti - stato.tagSelezionati).forEach { geckoRepository.removeTag(geckoId, it) }

            update { it.copy(salvataggioInCorso = false, salvataggioCompletato = true, erroreFoto = erroreFoto) }
        }
    }

    /**
     * Foto da salvare per un genitore esterno: copia nello storage quella appena scelta e
     * cancella il file precedente se sostituito, rimosso o se il genitore è ora dell'allevamento.
     */
    private suspend fun salvaFotoGenitore(g: GenitoreScelto, fotoPrecedente: String?, onErrore: () -> Unit): String? {
        val nuova = when {
            g.geckoId != null || g.nomeEsterno.isBlank() -> null
            g.fotoPendingUri != null -> runCatching { photoStorageManager.savePhoto(g.fotoPendingUri) }
                .onFailure { onErrore() }
                .getOrNull() ?: g.fotoSalvataPath
            else -> g.fotoSalvataPath
        }
        if (fotoPrecedente != null && fotoPrecedente != nuova) photoStorageManager.deletePhoto(fotoPrecedente)
        return nuova
    }

    private inline fun update(block: (GeckoFormUiState) -> GeckoFormUiState) {
        _uiState.value = block(_uiState.value)
    }
}
