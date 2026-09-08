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

/**
 * Intermediate figures exposed so the UI can show a full breakdown.
 *
 * remainingAllowance drives [result] and is balance-based: currentBalance −
 * monthlySavingsTarget − spentSinceTrackingStart. discretionaryBudget (the
 * original §3.B income − savings − fixed-costs figure) is kept only as
 * informational context — it no longer feeds the decision, since the real
 * bank balance already reflects whatever's actually been paid.
 */
data class PacerBreakdown(
    val monthlySavingsTarget: Double,
    val discretionaryBudget: Double,
    val currentBalance: Double,
    val spentSinceTrackingStart: Double,
    val remainingAllowance: Double,
    val daysRemainingInMonth: Int,
    val safeDailyPace: Double,
    val result: PacerResult
)
