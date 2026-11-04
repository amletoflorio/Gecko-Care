package com.terra.geckocare.ui.common

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Cache in memoria delle bitmap già decodificate, dimensionata in byte (~1/16
 * della heap). Evita di ridecodificare le stesse foto a ogni cambio di
 * schermata, che causava lavoro inutile e un "flash" vuoto prima dell'immagine.
 */
private object BitmapMemoryCache : LruCache<String, ImageBitmap>(
    (Runtime.getRuntime().maxMemory() / 16).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
) {
    override fun sizeOf(key: String, value: ImageBitmap): Int = value.width * value.height * 4
}

private fun cacheKey(model: Any?): String? = when (model) {
    // lastModified nella chiave: se il file viene sovrascritto la cache non serve la versione vecchia.
    is File -> "${model.absolutePath}@${model.lastModified()}"
    is String -> "$model@${File(model).lastModified()}"
    is Uri -> model.toString()
    else -> null
}

/**
 * Sostituto minimale di coil.compose.AsyncImage: decodifica un [File], [Uri]
 * o path assoluto ([String]) in background e lo mostra, con una piccola cache
 * in memoria (le foto profilo sono locali, poche e già ridimensionate).
 */
@Composable
fun TerraAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val key = remember(model) { cacheKey(model) }
    var bitmap by remember(model) { mutableStateOf(key?.let { BitmapMemoryCache.get(it) }) }

    LaunchedEffect(model) {
        if (bitmap != null) return@LaunchedEffect
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                when (model) {
                    is File -> BitmapFactory.decodeFile(model.absolutePath)?.asImageBitmap()
                    is Uri -> context.contentResolver.openInputStream(model)?.use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
                    is String -> BitmapFactory.decodeFile(model)?.asImageBitmap()
                    else -> null
                }
            }.getOrNull()?.also { decodificata -> key?.let { BitmapMemoryCache.put(it, decodificata) } }
        }
    }

    bitmap?.let {
        Image(bitmap = it, contentDescription = contentDescription, modifier = modifier, contentScale = contentScale)
    }
}
