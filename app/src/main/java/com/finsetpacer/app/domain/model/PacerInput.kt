package com.finsetpacer.app.domain.model

/**
 * Plain domain-level mirror of the user's settings — kept independent of the
 * Room entity so PacerEngine has no framework dependency and is trivially
 * unit-testable on the plain JVM.
 */
data class PacerInput(
    val monthlyIncome: Double,
    val fixedCosts: Double,
    val averageOutingCost: Double,
    val currentBalance: Double,
    val savingsMode: SavingsMode,
    val savingsPercentage: Double? = null,
    val targetAmount: Double? = null,
    val targetMonths: Int? = null
)
