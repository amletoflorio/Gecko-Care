package com.terra.geckocare.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Palette "grezza" ispirata al geco leopardino e al suo habitat desertico
 * (terracotta, sabbia, dorato). Usata direttamente dai componenti che
 * replicano trattamenti bespoke del mockup (chip, gauge, hero, badge) dove
 * i ruoli generici di Material3 non hanno un equivalente 1:1. Lo
 * [androidx.compose.material3.ColorScheme] standard (vedi Theme.kt) copre
 * invece i componenti M3 di libreria (TextField, Switch, NavigationBar...).
 *
 * Mappatura semantica dei nomi storici (invariati per non rompere gli usi
 * sparsi nelle schermate) verso i ruoli del nuovo design system:
 *  - night    -> Background
 *  - surface  -> Surface / Cards
 *  - surface2 -> Surface elevata (badge, segmented control, stati attivi)
 *  - line     -> Border / Divider
 *  - teal     -> Primary / Terracotta
 *  - clay     -> Primary Light / Sand
 *  - gold     -> Accent / Golden (anche Warning)
 *  - sage     -> Success
 *  - slate    -> Info / Umidità
 *  - danger   -> Error
 *  - ink/inkDim/inkFaint -> testo primario/secondario/disabilitato
 */
data class TerraColors(
    val night: Color,
    val surface: Color,
    val surface2: Color,
    val line: Color,
    val teal: Color,
    val tealDim: Color,
    val clay: Color,
    val clayDim: Color,
    val gold: Color,
    val goldDim: Color,
    val sage: Color,
    val sageDim: Color,
    val slate: Color,
    val slateDim: Color,
    val ink: Color,
    val inkDim: Color,
    val inkFaint: Color,
    val danger: Color,
    val dangerDim: Color,
    /** Testo sopra i colori accento (teal/clay/gold/sage/slate): sempre scuro
     * fisso per garantire contrasto AA, indipendentemente dal tema chiaro/
     * scuro attivo (i toni accento restano medio-chiari in entrambi i temi). */
    val onAccent: Color,
)

// --- Light ---
private val TerracottaLight = Color(0xFFC76B45)
private val SandLight = Color(0xFFE8B87A)
private val GoldenLight = Color(0xFFD9A441)
private val SageLight = Color(0xFF71856A)
private val InfoLight = Color(0xFF6F91A3)
private val ErrorLight = Color(0xFFB8543D)

// --- Dark ---
private val TerracottaDark = Color(0xFFD47A52)
private val SandDark = Color(0xFFC99A63)
private val GoldenDark = Color(0xFFE0B04F)
private val SageDark = Color(0xFF8FA383)
private val InfoDark = Color(0xFF7FA2B5)
private val ErrorDark = Color(0xFFC96850)

/** Testo scuro caldo fisso sopra le superfici accento, in entrambi i temi. */
private val OnAccentFixed = Color(0xFF29251F)

val TerraLightColors = TerraColors(
    night = Color(0xFFF7F3EA),
    surface = Color(0xFFFFFDF8),
    surface2 = Color(0xFFF1E8D8),
    line = Color(0xFFE8E0D3),
    teal = TerracottaLight,
    tealDim = TerracottaLight.copy(alpha = 0.14f),
    clay = SandLight,
    clayDim = SandLight.copy(alpha = 0.30f),
    gold = GoldenLight,
    goldDim = GoldenLight.copy(alpha = 0.16f),
    sage = SageLight,
    sageDim = SageLight.copy(alpha = 0.16f),
    slate = InfoLight,
    slateDim = InfoLight.copy(alpha = 0.16f),
    ink = Color(0xFF29251F),
    inkDim = Color(0xFF716A60),
    inkFaint = Color(0xFFAAA297),
    danger = ErrorLight,
    dangerDim = ErrorLight.copy(alpha = 0.16f),
    onAccent = OnAccentFixed,
)

val TerraDarkColors = TerraColors(
    night = Color(0xFF1C1916),
    surface = Color(0xFF27221E),
    surface2 = Color(0xFF332B25),
    line = Color(0xFFF5EFE6).copy(alpha = 0.08f),
    teal = TerracottaDark,
    tealDim = TerracottaDark.copy(alpha = 0.18f),
    clay = SandDark,
    clayDim = SandDark.copy(alpha = 0.22f),
    gold = GoldenDark,
    goldDim = GoldenDark.copy(alpha = 0.18f),
    sage = SageDark,
    sageDim = SageDark.copy(alpha = 0.18f),
    slate = InfoDark,
    slateDim = InfoDark.copy(alpha = 0.18f),
    ink = Color(0xFFF5EFE6),
    inkDim = Color(0xFFB8AEA2),
    inkFaint = Color(0xFFB8AEA2).copy(alpha = 0.60f),
    danger = ErrorDark,
    dangerDim = ErrorDark.copy(alpha = 0.18f),
    onAccent = OnAccentFixed,
)

val LocalTerraColors = staticCompositionLocalOf { TerraLightColors }
