package com.terra.geckocare.domain

import java.time.Instant

enum class ActivityKind { PASTO, PESO, LUNGHEZZA, MUTA, NOTA }

/** Voce unificata per il feed "Attività recente" in Home, che fonde pasti/peso/mute/note. */
data class ActivityItem(
    val kind: ActivityKind,
    val label: String,
    val timestamp: Instant,
)
