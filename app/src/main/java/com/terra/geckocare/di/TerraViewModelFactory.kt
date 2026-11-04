package com.terra.geckocare.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.terra.geckocare.ui.gechi.AggiungiGeckoViewModel
import com.terra.geckocare.ui.gechi.GechiViewModel
import com.terra.geckocare.ui.home.AmbienteFormViewModel
import com.terra.geckocare.ui.home.CareNoteFormViewModel
import com.terra.geckocare.ui.home.CrescitaFormViewModel
import com.terra.geckocare.ui.home.GalleryViewModel
import com.terra.geckocare.ui.home.HomeViewModel
import com.terra.geckocare.ui.home.MutaFormViewModel
import com.terra.geckocare.ui.home.ReminderFormViewModel
import com.terra.geckocare.ui.navigation.GeckoSelectionViewModel
import com.terra.geckocare.ui.pasti.AggiungiPastoViewModel
import com.terra.geckocare.ui.pasti.FasciaEtaFormViewModel
import com.terra.geckocare.ui.pasti.FeciFormViewModel
import com.terra.geckocare.ui.pasti.PastiViewModel
import com.terra.geckocare.ui.settings.SettingsViewModel
import com.terra.geckocare.ui.spese.AggiungiSpesaViewModel
import com.terra.geckocare.ui.spese.SpeseViewModel
import com.terra.geckocare.ui.stats.StatsViewModel

/** Factory manuale: costruisce ogni ViewModel a partire da [AppContainer], senza Hilt. */
class TerraViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = when (modelClass) {
            GeckoSelectionViewModel::class.java -> GeckoSelectionViewModel(container.geckoRepository)

            GechiViewModel::class.java -> GechiViewModel(
                container.appContext,
                container.geckoRepository,
                container.growthRepository,
                container.feedingRepository,
                container.reminderRepository,
                container.expenseRepository,
            )

            AggiungiGeckoViewModel::class.java -> AggiungiGeckoViewModel(
                container.geckoRepository,
                container.photoStorageManager,
            )

            HomeViewModel::class.java -> HomeViewModel(
                container.appContext,
                container.geckoRepository,
                container.growthRepository,
                container.shedRepository,
                container.feedingRepository,
                container.careNoteRepository,
                container.environmentRepository,
                container.feedingPlanRepository,
                container.reminderRepository,
                container.reminderScheduler,
            )

            CareNoteFormViewModel::class.java -> CareNoteFormViewModel(container.careNoteRepository)

            AmbienteFormViewModel::class.java -> AmbienteFormViewModel(container.environmentRepository)

            MutaFormViewModel::class.java -> MutaFormViewModel(container.shedRepository)

            CrescitaFormViewModel::class.java -> CrescitaFormViewModel(container.growthRepository)

            GalleryViewModel::class.java -> GalleryViewModel(container.geckoPhotoRepository, container.photoStorageManager)

            ReminderFormViewModel::class.java -> ReminderFormViewModel(
                container.reminderRepository,
                container.reminderScheduler,
            )

            PastiViewModel::class.java -> PastiViewModel(
                container.appContext,
                container.feedingRepository,
                container.feedingPlanRepository,
                container.geckoRepository,
                container.defecationRepository,
            )

            AggiungiPastoViewModel::class.java -> AggiungiPastoViewModel(
                container.appContext,
                container.feedingRepository,
                container.feedingPlanRepository,
                container.geckoRepository,
            )

            FasciaEtaFormViewModel::class.java -> FasciaEtaFormViewModel(container.feedingPlanRepository)

            FeciFormViewModel::class.java -> FeciFormViewModel(container.defecationRepository)

            StatsViewModel::class.java -> StatsViewModel(
                container.appContext,
                container.growthRepository,
                container.feedingRepository,
                container.shedRepository,
                container.expenseRepository,
                container.geckoRepository,
                container.defecationRepository,
            )

            SpeseViewModel::class.java -> SpeseViewModel(container.expenseRepository, container.geckoRepository)

            AggiungiSpesaViewModel::class.java -> AggiungiSpesaViewModel(container.expenseRepository)

            SettingsViewModel::class.java -> SettingsViewModel(
                container.appContext,
                container.settingsRepository,
                container.backupManager,
                container.backupScheduler,
            )

            else -> throw IllegalArgumentException("ViewModel non registrato in TerraViewModelFactory: $modelClass")
        }
        return viewModel as T
    }
}
