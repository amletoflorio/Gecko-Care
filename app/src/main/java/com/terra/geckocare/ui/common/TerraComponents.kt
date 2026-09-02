package com.terra.geckocare.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terra.geckocare.ui.theme.JetBrainsMono
import com.terra.geckocare.ui.theme.LocalTerraColors

/** Card base: superficie + bordo sottile + angoli arrotondati, come .card nel mockup. */
@Composable
fun TerraCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalTerraColors.current.surface,
    borderColor: Color = LocalTerraColors.current.line,
    padding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(18.dp))
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(18.dp))
            .padding(padding),
        content = content,
    )
}

/** Etichetta piccola maiuscola in teal, sopra i titoli pagina (.eyebrow nel mockup). */
@Composable
fun EyebrowText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = LocalTerraColors.current.teal,
        fontFamily = JetBrainsMono,
        fontSize = 11.sp,
        letterSpacing = 1.4.sp,
    )
}

/** Titolo pagina in Fraunces (.page-title nel mockup). */
@Composable
fun PageTitle(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier, style = MaterialTheme.typography.headlineLarge, color = LocalTerraColors.current.ink)
}

/** Intestazione di sezione, maiuscola e discreta (.section-title nel mockup). */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier.padding(top = 22.dp, bottom = 10.dp),
        color = LocalTerraColors.current.inkDim,
        fontFamily = com.terra.geckocare.ui.theme.SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 0.7.sp,
    )
}

enum class ChipVariant { NEUTRAL, TEAL, CLAY, SAGE, DANGER }

/** Chip mono-spaced come i badge del mockup (.chip / .chip.on / .chip.clay / .chip.teal). */
@Composable
fun TerraChip(
    text: String,
    modifier: Modifier = Modifier,
    variant: ChipVariant = ChipVariant.NEUTRAL,
    onClick: (() -> Unit)? = null,
) {
    val colors = LocalTerraColors.current
    val (bg, fg, border) = when (variant) {
        ChipVariant.NEUTRAL -> Triple(Color.Transparent, colors.inkDim, colors.line)
        ChipVariant.TEAL -> Triple(colors.tealDim, colors.teal, Color.Transparent)
        ChipVariant.CLAY -> Triple(colors.clayDim, colors.clay, Color.Transparent)
        ChipVariant.SAGE -> Triple(colors.sageDim, colors.sage, Color.Transparent)
        ChipVariant.DANGER -> Triple(colors.dangerDim, colors.danger, Color.Transparent)
    }
    Box(
        modifier = modifier
            .background(bg, CircleShape)
            .border(BorderStroke(1.dp, border), CircleShape)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text = text, color = fg, fontFamily = JetBrainsMono, fontSize = 11.sp)
    }
}

/** Controllo segmentato a due o più opzioni (.segmented nel mockup). */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTerraColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface2, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelected(index) }
                    .background(if (selected) colors.night else Color.Transparent, RoundedCornerShape(9.dp))
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (selected) colors.ink else colors.inkFaint,
                    fontFamily = com.terra.geckocare.ui.theme.SpaceGrotesk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Pulsante pieno teal (.btn nel mockup). */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = LocalTerraColors.current
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.teal, contentColor = colors.onAccent),
    ) {
        Text(text = text, fontFamily = com.terra.geckocare.ui.theme.SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

/** Pulsante outline trasparente (.btn.ghost nel mockup). */
@Composable
fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = LocalTerraColors.current
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(1.dp, colors.line),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.ink),
    ) {
        Text(text = text, fontFamily = com.terra.geckocare.ui.theme.SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
