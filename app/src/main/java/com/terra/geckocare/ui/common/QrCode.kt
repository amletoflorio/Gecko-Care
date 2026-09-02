package com.terra.geckocare.ui.common

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.terra.geckocare.ui.theme.LocalTerraColors

/** Genera un QR code come [ImageBitmap], colorato con i toni del tema corrente. */
@Composable
fun rememberQrCodeBitmap(content: String, sizePx: Int = 240): ImageBitmap {
    val colors = LocalTerraColors.current
    val darkArgb = colors.ink.toArgb()
    val lightArgb = colors.surface.toArgb()
    return remember(content, sizePx, darkArgb, lightArgb) {
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bitmap.setPixel(x, y, if (matrix[x, y]) darkArgb else lightArgb)
            }
        }
        bitmap.asImageBitmap()
    }
}
