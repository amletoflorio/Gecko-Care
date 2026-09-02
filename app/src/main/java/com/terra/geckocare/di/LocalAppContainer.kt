package com.terra.geckocare.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("LocalAppContainer non fornito: avvolgi il contenuto con CompositionLocalProvider(LocalAppContainer provides ...)")
}

/** Sostituto locale di hiltViewModel(): stessa ergonomia, DI manuale via [AppContainer]. */
@Composable
inline fun <reified VM : ViewModel> terraViewModel(): VM {
    val container = LocalAppContainer.current
    return viewModel(factory = TerraViewModelFactory(container))
}
