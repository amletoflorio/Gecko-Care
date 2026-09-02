package com.terra.geckocare.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.terra.geckocare.ui.theme.LocalTerraColors

/** Segnaposto per le schermate non ancora implementate in questa fase del progetto. */
@Composable
fun PlaceholderScreen(titolo: String, sottotitolo: String = "In arrivo") {
    val colors = LocalTerraColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = titolo, style = MaterialTheme.typography.headlineSmall, color = colors.ink, textAlign = TextAlign.Center)
        Text(text = sottotitolo, style = MaterialTheme.typography.bodyMedium, color = colors.inkFaint, textAlign = TextAlign.Center)
    }
}
