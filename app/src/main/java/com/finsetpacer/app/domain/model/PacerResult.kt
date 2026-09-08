package com.finsetpacer.app.domain.model

import java.time.LocalDate

/**
 * The three UI states from spec §3.C, in priority order (Exceeded is checked first).
 */
sealed class PacerResult {
    data class Safe(val dailyAllowance: Double) : PacerResult()

    data class HoldOff(
        val waitDays: Int,
        val nextSafeDate: LocalDate,
        val dailyAllowance: Double
    ) : PacerResult()

    object BudgetExceeded : PacerResult()
}

/** Intermediate figures from §3.A/B, exposed so the UI can show a full breakdown. */
data class PacerBreakdown(
    val monthlySavingsTarget: Double,
    val discretionaryBudget: Double,
    val currentMonthSpent: Double,
    val remainingAllowance: Double,
    val daysRemainingInMonth: Int,
    val safeDailyPace: Double,
    val result: PacerResult
)
