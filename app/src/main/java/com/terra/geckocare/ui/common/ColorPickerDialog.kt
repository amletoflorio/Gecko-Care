package com.terra.geckocare.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.terra.geckocare.ui.theme.JetBrainsMono
import com.terra.geckocare.ui.theme.LocalTerraColors

/** Dialog con picker HSV completo (quadrato sat/val trascinabile + slider tonalità). */
@Composable
fun ColorPickerDialog(
    coloreIniziale: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val colors = LocalTerraColors.current

    val hsvIniziale = remember(coloreIniziale) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(
            runCatching { android.graphics.Color.parseColor(coloreIniziale) }.getOrDefault(android.graphics.Color.parseColor("#C76B45")),
            hsv,
        )
        hsv
    }
    var hue by remember { mutableFloatStateOf(hsvIniziale[0]) }
    var sat by remember { mutableFloatStateOf(hsvIniziale[1]) }
    var value by remember { mutableFloatStateOf(hsvIniziale[2]) }

    val coloreCorrente = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, value)))
    val hexCorrente = "#%06X".format(0xFFFFFF and coloreCorrente.toArgb())

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(androidx.compose.foundation.BorderStroke(1.dp, colors.line), RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Text(stringResource(com.terra.geckocare.R.string.colorpicker_title), color = colors.ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))

            SatValueSquare(
                hue = hue,
                sat = sat,
                value = value,
                onChange = { s, v -> sat = s; value = v },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            )

            Spacer(Modifier.height(14.dp))

            HueSlider(
                hue = hue,
                onChange = { hue = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
            )

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(coloreCorrente)
                        .border(androidx.compose.foundation.BorderStroke(1.dp, colors.line), CircleShape),
                )
                Text(hexCorrente.uppercase(), color = colors.inkDim, fontFamily = JetBrainsMono, fontSize = 14.sp)
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                GhostButton(text = stringResource(com.terra.geckocare.R.string.common_annulla), onClick = onDismiss, modifier = Modifier.weight(1f))
                PrimaryButton(text = stringResource(com.terra.geckocare.R.string.common_conferma), onClick = { onConfirm(hexCorrente) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SatValueSquare(
    hue: Float,
    sat: Float,
    value: Float,
    onChange: (sat: Float, value: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hueColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(hueColor)
            .background(Brush.horizontalGradient(listOf(Color.White, Color.Transparent)))
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val s = (change.position.x / size.width).coerceIn(0f, 1f)
                    val v = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    onChange(s, v)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val s = (offset.x / size.width).coerceIn(0f, 1f)
                    val v = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                    onChange(s, v)
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val x = sat * size.width
            val y = (1f - value) * size.height
            drawCircle(
                color = Color.White,
                radius = 9.dp.toPx(),
                center = Offset(x, y),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx()),
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = 10.5.dp.toPx(),
                center = Offset(x, y),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
            )
        }
    }
}

@Composable
private fun HueSlider(
    hue: Float,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rainbow = remember {
        Brush.horizontalGradient((0..360 step 30).map { Color(android.graphics.Color.HSVToColor(floatArrayOf(it.toFloat(), 1f, 1f))) })
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(rainbow)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    onChange((change.position.x / size.width).coerceIn(0f, 1f) * 360f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onChange((offset.x / size.width).coerceIn(0f, 1f) * 360f)
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
            val x = (hue / 360f) * size.width
            drawCircle(
                color = Color.White,
                radius = size.height / 2f,
                center = Offset(x.coerceIn(size.height / 2f, size.width - size.height / 2f), size.height / 2f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()),
            )
        }
    }
}
