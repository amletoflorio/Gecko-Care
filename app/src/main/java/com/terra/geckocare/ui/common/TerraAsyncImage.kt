package com.terra.geckocare.ui.common

import android.graphics.BitmapFactory
import android.net.Uri
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
 * Sostituto minimale di coil.compose.AsyncImage: decodifica un [File], [Uri]
 * o path assoluto ([String]) in background e lo mostra. Nessuna cache su
 * disco/memoria (non serve per foto profilo locali, poche e piccole).
 */
@Composable
fun TerraAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    var bitmap by remember(model) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(model) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                when (model) {
                    is File -> BitmapFactory.decodeFile(model.absolutePath)?.asImageBitmap()
                    is Uri -> context.contentResolver.openInputStream(model)?.use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
                    is String -> BitmapFactory.decodeFile(model)?.asImageBitmap()
                    else -> null
                }
            }.getOrNull()
        }
    }

    bitmap?.let {
        Image(bitmap = it, contentDescription = contentDescription, modifier = modifier, contentScale = contentScale)
    }
}
