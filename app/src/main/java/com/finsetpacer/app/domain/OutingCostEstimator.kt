package com.finsetpacer.app.domain

/**
 * Suggests §2's `Average Outing Cost` input from historical spending instead
 * of the user having to guess a number — average of past leisure-category
 * (food/entertainment) transaction amounts. Pure Kotlin, JVM-testable.
 */
object OutingCostEstimator {

    /** Null when there's no leisure history yet — caller should leave the field blank/manual. */
    fun estimate(leisureAmounts: List<Double>): Double? =
        if (leisureAmounts.isEmpty()) null else leisureAmounts.average()
}
