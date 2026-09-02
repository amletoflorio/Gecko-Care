package com.terra.geckocare

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.terra.geckocare.data.settings.ThemeMode
import com.terra.geckocare.di.LocalAppContainer
import com.terra.geckocare.di.terraViewModel
import com.terra.geckocare.domain.LocalCurrencyCode
import com.terra.geckocare.ui.TerraApp
import com.terra.geckocare.ui.settings.SettingsViewModel
import com.terra.geckocare.ui.theme.TerraTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as TerraApplication).container
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                val settingsViewModel: SettingsViewModel = terraViewModel()
                val themeMode by settingsViewModel.themeMode.collectAsState()
                val appCurrency by settingsViewModel.appCurrency.collectAsState()
                val darkTheme = when (themeMode) {
                    ThemeMode.SISTEMA -> isSystemInDarkTheme()
                    ThemeMode.CHIARO -> false
                    ThemeMode.SCURO -> true
                }
                CompositionLocalProvider(LocalCurrencyCode provides appCurrency.isoCode) {
                    TerraTheme(darkTheme = darkTheme) {
                        TerraApp()
                    }
                }
            }
        }
    }
}
