package com.terra.geckocare.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.terra.geckocare.data.local.TerraDatabase
import com.terra.geckocare.data.settings.DATASTORE_FILE_NAME
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val ENTRY_DB = "terra.db"
private const val ENTRY_PHOTOS_PREFIX = "photos/"
private val ENTRY_PREFS get() = DATASTORE_FILE_NAME

private fun dbFile(context: Context): File = context.getDatabasePath("terra.db")
private fun prefsFile(context: Context): File = File(context.filesDir, "datastore/$DATASTORE_FILE_NAME")
private fun photosDir(context: Context): File = File(context.filesDir, "gecko_photos")
private fun pendingRestoreDir(context: Context): File = File(context.filesDir, "pending_restore")

/** Crea/ripristina un archivio .zip con DB, impostazioni e foto: unico punto di verità per il backup locale dell'app. */
class BackupManager(private val database: TerraDatabase) {

    /** Genera "geckocare_backup_....zip" dentro la cartella scelta dall'utente (SAF tree Uri) e lo popola. */
    suspend fun backup(context: Context, treeUri: Uri): Result<Unit> = runCatching {
        checkpointWal()

        val folder = DocumentFile.fromTreeUri(context, treeUri)
            ?: error("Cartella di backup non accessibile")
        val nomeFile = "geckocare_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.zip"
        val output = folder.createFile("application/zip", nomeFile)
            ?: error("Impossibile creare il file di backup")

        context.contentResolver.openOutputStream(output.uri)?.use { out ->
            ZipOutputStream(out).use { zip ->
                addFileEntry(zip, dbFile(context), ENTRY_DB)
                val prefs = prefsFile(context)
                if (prefs.exists()) addFileEntry(zip, prefs, ENTRY_PREFS)
                photosDir(context).listFiles()?.forEach { foto ->
                    addFileEntry(zip, foto, ENTRY_PHOTOS_PREFIX + foto.name)
                }
            }
        } ?: error("Impossibile scrivere il file di backup")
    }

    private fun checkpointWal() {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
    }

    private fun addFileEntry(zip: ZipOutputStream, file: File, entryName: String) {
        zip.putNextEntry(ZipEntry(entryName))
        file.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }

    companion object {
        /** Estrae lo zip scelto dall'utente in un'area di staging: applicato al prossimo avvio, prima che Room apra il DB. */
        fun stageRestore(context: Context, zipUri: Uri): Result<Unit> = runCatching {
            val stream: InputStream = context.contentResolver.openInputStream(zipUri) ?: error("File non leggibile")
            val staging = pendingRestoreDir(context)
            staging.deleteRecursively()
            staging.mkdirs()

            var dbTrovato = false
            ZipInputStream(stream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val destinazione = File(staging, entry.name)
                    if (!entry.isDirectory) {
                        destinazione.parentFile?.mkdirs()
                        destinazione.outputStream().use { out -> zip.copyTo(out) }
                        if (entry.name == ENTRY_DB) dbTrovato = true
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }

            if (!dbTrovato) {
                staging.deleteRecursively()
                error("Il file selezionato non è un backup valido di Gecko Care")
            }

            File(staging, "READY").writeText("1")
        }

        /** Da chiamare in Application.onCreate PRIMA di aprire il database Room. Ritorna true se un ripristino è stato applicato. */
        fun applyStagedRestoreIfPresent(context: Context): Boolean {
            val staging = pendingRestoreDir(context)
            val marker = File(staging, "READY")
            if (!marker.exists()) return false

            val stagedDb = File(staging, ENTRY_DB)
            if (stagedDb.exists()) {
                val db = dbFile(context)
                db.parentFile?.mkdirs()
                stagedDb.copyTo(db, overwrite = true)
                File(db.path + "-wal").delete()
                File(db.path + "-shm").delete()
            }

            val stagedPrefs = File(staging, ENTRY_PREFS)
            if (stagedPrefs.exists()) {
                val prefs = prefsFile(context)
                prefs.parentFile?.mkdirs()
                stagedPrefs.copyTo(prefs, overwrite = true)
            }

            val stagedPhotos = File(staging, ENTRY_PHOTOS_PREFIX.trimEnd('/'))
            if (stagedPhotos.exists()) {
                val photos = photosDir(context)
                photos.deleteRecursively()
                photos.mkdirs()
                stagedPhotos.listFiles()?.forEach { it.copyTo(File(photos, it.name), overwrite = true) }
            }

            staging.deleteRecursively()
            return true
        }
    }
}
