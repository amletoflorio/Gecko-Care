package com.terra.geckocare.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Copia la foto scelta dal photo picker di sistema (Uri content:// con
 * permesso valido solo per la sessione corrente) nello storage interno
 * dell'app come JPEG quadrato, e restituisce il path assoluto locale da
 * salvare in Room. Così l'immagine sopravvive ai riavvii senza dipendere
 * da permessi su Uri esterni che il sistema può revocare.
 */
class PhotoStorageManager(
    private val context: Context,
) {
    private val photosDir: File by lazy {
        File(context.filesDir, "gecko_photos").apply { mkdirs() }
    }

    suspend fun savePhoto(sourceUri: Uri, maxSizePx: Int = 640): String = withContext(Dispatchers.IO) {
        val squared = loadUprightSquareBitmap(sourceUri, maxSizePx)
        val file = File(photosDir, "${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out -> squared.compress(Bitmap.CompressFormat.JPEG, 90, out) }
        squared.recycle()
        file.absolutePath
    }

    suspend fun deletePhoto(path: String?) = withContext(Dispatchers.IO) {
        if (!path.isNullOrBlank()) {
            runCatching { File(path).takeIf { it.exists() }?.delete() }
        }
    }

    private fun loadUprightSquareBitmap(uri: Uri, maxSizePx: Int): Bitmap {
        // ImageDecoder (API 28+) è più tollerante di BitmapFactory su JPEG con
        // metadata moderni (es. Ultra HDR / gainmap) che su alcune build Skia
        // mandano in errore la decodifica classica bounds-first.
        val viaImageDecoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            runCatching { decodeWithImageDecoder(uri, maxSizePx) }.getOrNull()
        } else {
            null
        }
        val decoded = viaImageDecoder ?: decodeWithBitmapFactory(uri, maxSizePx)

        val upright = applyExifRotation(context.contentResolver.openInputStream(uri), decoded)
        val squared = centerCropSquare(upright)
        return if (squared.width > maxSizePx) squared.scale(maxSizePx, maxSizePx) else squared
    }

    private fun decodeWithImageDecoder(uri: Uri, maxSizePx: Int): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.isMutableRequired = true
            decoder.setTargetSampleSize(calculateInSampleSize(info.size.width, info.size.height, maxSizePx))
        }
    }

    private fun decodeWithBitmapFactory(uri: Uri, maxSizePx: Int): Bitmap {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: error("Impossibile leggere l'immagine selezionata")

        val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxSizePx)
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOptions) }
            ?: error("Impossibile decodificare l'immagine selezionata")
    }

    private fun calculateInSampleSize(width: Int, height: Int, target: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (w / 2 >= target && h / 2 >= target) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }

    private fun applyExifRotation(exifStream: java.io.InputStream?, bitmap: Bitmap): Bitmap {
        val orientation = exifStream?.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bitmap

        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) bitmap.recycle()
        return rotated
    }

    private fun centerCropSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        val cropped = Bitmap.createBitmap(bitmap, x, y, size, size)
        if (cropped !== bitmap) bitmap.recycle()
        return cropped
    }
}
