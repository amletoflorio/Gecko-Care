package com.terra.geckocare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.terra.geckocare.R

/**
 * Font unico dell'app: Inter (bundled come file statici in res/font, pesi
 * 400/500/600/700). I tre alias sotto restano per compatibilità con gli
 * usi diretti sparsi nelle schermate (JetBrainsMono per numeri/date,
 * SpaceGrotesk per label/body, Fraunces per titoli): puntano tutti alla
 * stessa Inter, non servono più famiglie distinte.
 */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

val SpaceGrotesk = Inter
val Fraunces = Inter
val JetBrainsMono = Inter

/** Stile pronto per valori numerici (stat, peso, date) ovunque servano nell'app. */
val MonoDataStyle = TextStyle(fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal)

val TerraTypography = Typography(
    displayLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.01).em),
    displayMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp),
    displaySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    headlineLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.01).em),
    headlineMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 23.sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp),
    titleSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    bodyLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 13.5.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.06.em),
)
