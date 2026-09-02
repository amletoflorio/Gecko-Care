package com.terra.geckocare.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

private fun buildColorScheme(colors: TerraColors, dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = colors.teal,
        onPrimary = colors.onAccent,
        primaryContainer = colors.tealDim,
        onPrimaryContainer = colors.teal,
        secondary = colors.clay,
        onSecondary = colors.onAccent,
        secondaryContainer = colors.clayDim,
        onSecondaryContainer = colors.clay,
        tertiary = colors.sage,
        onTertiary = colors.onAccent,
        tertiaryContainer = colors.sageDim,
        onTertiaryContainer = colors.sage,
        error = colors.danger,
        onError = colors.onAccent,
        errorContainer = colors.dangerDim,
        onErrorContainer = colors.danger,
        background = colors.night,
        onBackground = colors.ink,
        surface = colors.surface,
        onSurface = colors.ink,
        surfaceVariant = colors.surface2,
        onSurfaceVariant = colors.inkDim,
        outline = colors.ink.copy(alpha = 0.16f),
        outlineVariant = colors.line,
        surfaceContainerLowest = colors.night,
        surfaceContainerLow = colors.surface,
        surfaceContainer = colors.surface2,
        surfaceContainerHigh = colors.surface2,
        surfaceContainerHighest = colors.surface2,
    )
} else {
    lightColorScheme(
        primary = colors.teal,
        onPrimary = colors.onAccent,
        primaryContainer = colors.tealDim,
        onPrimaryContainer = colors.teal,
        secondary = colors.clay,
        onSecondary = colors.onAccent,
        secondaryContainer = colors.clayDim,
        onSecondaryContainer = colors.clay,
        tertiary = colors.sage,
        onTertiary = colors.onAccent,
        tertiaryContainer = colors.sageDim,
        onTertiaryContainer = colors.sage,
        error = colors.danger,
        onError = colors.onAccent,
        errorContainer = colors.dangerDim,
        onErrorContainer = colors.danger,
        background = colors.night,
        onBackground = colors.ink,
        surface = colors.surface,
        onSurface = colors.ink,
        surfaceVariant = colors.surface2,
        onSurfaceVariant = colors.inkDim,
        outline = colors.ink.copy(alpha = 0.16f),
        outlineVariant = colors.line,
        surfaceContainerLowest = colors.night,
        surfaceContainerLow = colors.surface,
        surfaceContainer = colors.surface2,
        surfaceContainerHigh = colors.surface2,
        surfaceContainerHighest = colors.surface2,
    )
}

@Composable
fun TerraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val terraColors = remember(darkTheme) { if (darkTheme) TerraDarkColors else TerraLightColors }
    val colorScheme = remember(darkTheme) { buildColorScheme(terraColors, darkTheme) }

    CompositionLocalProvider(LocalTerraColors provides terraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TerraTypography,
            content = content,
        )
    }
}
