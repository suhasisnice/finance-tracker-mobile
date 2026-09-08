package com.finsetpacer.app.domain

import com.finsetpacer.app.domain.model.PacerInput
import com.finsetpacer.app.domain.model.PacerResult
import com.finsetpacer.app.domain.model.SavingsMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PacerEngineTest {

    @Test
    fun `mode 1 savings target is percentage of income`() {
        val input = PacerInput(
            monthlyIncome = 50_000.0,
            fixedCosts = 20_000.0,
            averageOutingCost = 1_500.0,
            currentBalance = 0.0,
            savingsMode = SavingsMode.PERCENTAGE,
            savingsPercentage = 20.0
        )
        assertEquals(10_000.0, PacerEngine.monthlySavingsTarget(input), 0.001)
    }

    @Test
    fun `mode 2 savings target is amount over months`() {
        val input = PacerInput(
            monthlyIncome = 50_000.0,
            fixedCosts = 20_000.0,
            averageOutingCost = 1_500.0,
            currentBalance = 0.0,
            savingsMode = SavingsMode.TARGET_AMOUNT,
            targetAmount = 10_000.0,
            targetMonths = 6
        )
        assertEquals(10_000.0 / 6, PacerEngine.monthlySavingsTarget(input), 0.001)
    }

    @Test
    fun `safe when daily pace covers outing cost`() {
        // balance 45000, savings 20% of income 90000 = 18000, spent since tracking 3000
        // -> remaining 24000, on day 1 of a 30-day month -> 30 days remaining
        // daily pace = 800 >= outing cost 750 -> Safe
        val input = PacerInput(
            monthlyIncome = 90_000.0,
            fixedCosts = 20_000.0,
            averageOutingCost = 750.0,
            currentBalance = 45_000.0,
            savingsMode = SavingsMode.PERCENTAGE,
            savingsPercentage = 20.0
        )
        val today = LocalDate.of(2026, 4, 1) // 30-day month
        val breakdown = PacerEngine.evaluate(input, spentSinceTrackingStart = 3_000.0, today = today)

        assertTrue(breakdown.result is PacerResult.Safe)
        assertEquals(800.0, (breakdown.result as PacerResult.Safe).dailyAllowance, 0.001)
        // informational only, not part of the decision: income - savings - fixed costs
        assertEquals(52_000.0, breakdown.discretionaryBudget, 0.001)
    }

    @Test
    fun `hold off when daily pace is below outing cost`() {
        // balance 20000, savings target 18000, spent since tracking 1000
        // -> remaining 1000, 10 days remaining -> pace 100
        // outing cost 250 -> waitDays = ceil(250/100) = 3
        val input = PacerInput(
            monthlyIncome = 90_000.0,
            fixedCosts = 20_000.0,
            averageOutingCost = 250.0,
            currentBalance = 20_000.0,
            savingsMode = SavingsMode.PERCENTAGE,
            savingsPercentage = 20.0
        )
        val today = LocalDate.of(2026, 4, 21) // 30-day month, 10 days remaining
        val breakdown = PacerEngine.evaluate(input, spentSinceTrackingStart = 1_000.0, today = today)

        assertTrue(breakdown.result is PacerResult.HoldOff)
        val holdOff = breakdown.result as PacerResult.HoldOff
        assertEquals(3, holdOff.waitDays)
        assertEquals(today.plusDays(3), holdOff.nextSafeDate)
    }

    @Test
    fun `budget exceeded when remaining allowance is zero or negative`() {
        // balance 15000, savings target 18000 -> already short before any spending
        val input = PacerInput(
            monthlyIncome = 90_000.0,
            fixedCosts = 20_000.0,
            averageOutingCost = 1_500.0,
            currentBalance = 15_000.0,
            savingsMode = SavingsMode.PERCENTAGE,
            savingsPercentage = 20.0
        )
        val today = LocalDate.of(2026, 4, 15)
        val breakdown = PacerEngine.evaluate(input, spentSinceTrackingStart = 0.0, today = today)

        assertTrue(breakdown.result is PacerResult.BudgetExceeded)
    }

    @Test
    fun `spending tracked before install does not count against remaining allowance`() {
        // Same balance/savings as the safe case, but a large "spent this calendar
        // month" figure that happened before tracking started must NOT be passed
        // in here at all (TrackingWindow excludes it upstream) — evaluate() only
        // ever sees spentSinceTrackingStart, so a pre-install spend of 20000
        // simply isn't part of this number.
        val input = PacerInput(
            monthlyIncome = 90_000.0,
            fixedCosts = 20_000.0,
            averageOutingCost = 750.0,
            currentBalance = 45_000.0,
            savingsMode = SavingsMode.PERCENTAGE,
            savingsPercentage = 20.0
        )
        val today = LocalDate.of(2026, 4, 1)
        val breakdown = PacerEngine.evaluate(input, spentSinceTrackingStart = 0.0, today = today)

        assertTrue(breakdown.result !is PacerResult.BudgetExceeded)
        assertEquals(27_000.0, breakdown.remainingAllowance, 0.001)
    }

    @Test
    fun `required income percentage matches mode 2 target`() {
        val input = PacerInput(
            monthlyIncome = 40_000.0,
            fixedCosts = 10_000.0,
            averageOutingCost = 1_000.0,
            currentBalance = 0.0,
            savingsMode = SavingsMode.TARGET_AMOUNT,
            targetAmount = 12_000.0,
            targetMonths = 6
        )
        // S = 2000/month, required % = (2000/40000)*100 = 5%
        assertEquals(5.0, PacerEngine.requiredIncomePercentage(input), 0.001)
    }
}
