package com.terra.geckocare.domain

import com.terra.geckocare.data.local.entity.FeedingPlanRule

/**
 * Determina la fascia d'età applicabile a un'età (in giorni): tra le regole
 * candidate, un override specifico del geco ([FeedingPlanRule.geckoId] non
 * nullo) ha sempre priorità sulla regola di default per la stessa fascia.
 */
fun regolaAttuale(regole: List<FeedingPlanRule>, etaGiorni: Long?): FeedingPlanRule? {
    if (etaGiorni == null) return null
    val specifica = regole.firstOrNull { it.geckoId != null && etaGiorni in it.etaMinGiorni..it.etaMaxGiorni }
    if (specifica != null) return specifica
    return regole.firstOrNull { it.geckoId == null && etaGiorni in it.etaMinGiorni..it.etaMaxGiorni }
}

/** Ordina le fasce per età crescente, utile per la vista "Piano per età". */
fun List<FeedingPlanRule>.ordinatePerEta(): List<FeedingPlanRule> = sortedBy { it.etaMinGiorni }
