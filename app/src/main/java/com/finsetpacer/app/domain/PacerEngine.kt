package com.finsetpacer.app.domain

import com.finsetpacer.app.domain.model.PacerBreakdown
import com.finsetpacer.app.domain.model.PacerInput
import com.finsetpacer.app.domain.model.PacerResult
import com.finsetpacer.app.domain.model.SavingsMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

/**
 * Pure implementation of the Mathematical Pacing Engine, spec §3.
 * No Android/Room dependency — safe to unit test on the plain JVM.
 */
object PacerEngine {

    /**
     * §3.A — Mode 1: S = I * (P / 100). Mode 2: S = T / M.
     */
    fun monthlySavingsTarget(input: PacerInput): Double = when (input.savingsMode) {
        SavingsMode.PERCENTAGE -> {
            val percentage = input.savingsPercentage ?: 0.0
            input.monthlyIncome * (percentage / 100.0)
        }
        SavingsMode.TARGET_AMOUNT -> {
            val target = input.targetAmount ?: 0.0
            val months = input.targetMonths?.takeIf { it > 0 } ?: 1
            target / months
        }
    }

    /** Mode 2 helper from §3.A: required income % = (S / I) * 100. */
    fun requiredIncomePercentage(input: PacerInput): Double {
        if (input.monthlyIncome == 0.0) return 0.0
        return (monthlySavingsTarget(input) / input.monthlyIncome) * 100.0
    }

    /**
     * Full breakdown for the current point in the month. remainingAllowance —
     * and therefore [PacerResult] — is balance-based, not the original §3.B
     * income-derived figure: currentBalance − monthlySavingsTarget −
     * spentSinceTrackingStart. This is deliberate (user-requested): using the
     * real bank balance avoids double-counting fixed costs already paid, and
     * "spent so far" excludes anything from before the user started tracking
     * (see TrackingWindow) so pre-install spending isn't held against them.
     * discretionaryBudget (income − savings − fixed costs) is retained purely
     * as informational context in the breakdown, not part of the decision.
     *
     * @param spentSinceTrackingStart sum of SMS debits from trackingStartDate onward.
     * @param today defaults to the real current date; overridable for tests.
     */
    fun evaluate(
        input: PacerInput,
        spentSinceTrackingStart: Double,
        today: LocalDate = LocalDate.now()
    ): PacerBreakdown {
        val savingsTarget = monthlySavingsTarget(input)
        val discretionaryBudget = input.monthlyIncome - savingsTarget - input.fixedCosts
        val remainingAllowance = input.currentBalance - savingsTarget - spentSinceTrackingStart

        val daysInMonth = today.lengthOfMonth()
        val daysRemaining = daysInMonth - today.dayOfMonth + 1

        val safeDailyPace = remainingAllowance / daysRemaining

        val result = when {
            remainingAllowance <= 0.0 -> PacerResult.BudgetExceeded
            safeDailyPace >= input.averageOutingCost -> PacerResult.Safe(safeDailyPace)
            else -> {
                val waitDays = ceil(input.averageOutingCost / safeDailyPace).toInt()
                PacerResult.HoldOff(
                    waitDays = waitDays,
                    nextSafeDate = today.plus(waitDays.toLong(), ChronoUnit.DAYS),
                    dailyAllowance = safeDailyPace
                )
            }
        }

        return PacerBreakdown(
            monthlySavingsTarget = savingsTarget,
            discretionaryBudget = discretionaryBudget,
            currentBalance = input.currentBalance,
            spentSinceTrackingStart = spentSinceTrackingStart,
            remainingAllowance = remainingAllowance,
            daysRemainingInMonth = daysRemaining,
            safeDailyPace = safeDailyPace,
            result = result
        )
    }
}
