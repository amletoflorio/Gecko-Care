package com.terra.geckocare.ui.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private fun PathBuilder.ellipse(cx: Float, cy: Float, rx: Float, ry: Float) {
    moveTo(cx - rx, cy)
    arcToRelative(rx, ry, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = 2 * rx, dy1 = 0f)
    arcToRelative(rx, ry, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = -2 * rx, dy1 = 0f)
    close()
}

/** Silhouette stilizzata di un geco (corpo, coda arrotolata, 4 zampe con dita) per il tab "Geckos". */
val GeckoIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Gecko",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).path(fill = SolidColor(Color.Black)) {
        // testa, collo, corpo, dorso
        ellipse(12f, 3.3f, 1.5f, 1.3f)
        ellipse(12f, 5.6f, 2f, 1.6f)
        ellipse(12f, 9f, 2.6f, 2.8f)
        ellipse(12.3f, 12.8f, 2.1f, 2.3f)

        // coda arrotolata
        ellipse(13.3f, 16.2f, 1.6f, 2f)
        ellipse(15.3f, 18.8f, 1.3f, 1.7f)
        ellipse(17.6f, 20.6f, 1f, 1.3f)
        ellipse(19.4f, 21.6f, 0.7f, 0.85f)

        // zampa anteriore sinistra + dita
        ellipse(8f, 6.8f, 1.5f, 0.9f)
        ellipse(6.1f, 6.1f, 0.35f, 0.35f)
        ellipse(5.9f, 6.9f, 0.35f, 0.35f)
        ellipse(6.1f, 7.7f, 0.35f, 0.35f)

        // zampa anteriore destra + dita
        ellipse(16f, 6.8f, 1.5f, 0.9f)
        ellipse(17.9f, 6.1f, 0.35f, 0.35f)
        ellipse(18.1f, 6.9f, 0.35f, 0.35f)
        ellipse(17.9f, 7.7f, 0.35f, 0.35f)

        // zampa posteriore sinistra + dita
        ellipse(8.3f, 12.5f, 1.6f, 0.95f)
        ellipse(6.3f, 11.8f, 0.35f, 0.35f)
        ellipse(6.1f, 12.6f, 0.35f, 0.35f)
        ellipse(6.3f, 13.4f, 0.35f, 0.35f)

        // zampa posteriore destra + dita
        ellipse(16.7f, 12f, 1.6f, 0.95f)
        ellipse(18.7f, 11.3f, 0.35f, 0.35f)
        ellipse(18.9f, 12.1f, 0.35f, 0.35f)
        ellipse(18.7f, 12.9f, 0.35f, 0.35f)
    }.build()
}
