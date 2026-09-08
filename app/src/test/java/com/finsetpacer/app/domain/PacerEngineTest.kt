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
            savingsMode = SavingsMode.TARGET_AMOUNT,
            targetAmount = 10_000.0,
            targetMonths = 6
        )
        assertEquals(10_000.0 / 6, PacerEngine.monthlySavingsTarget(input), 0.001)
    }

    @Test
    fun `safe when daily pace covers outing cost`() {
        // income 90000, savings 20% = 18000, fixed 20000 -> discretionary 52000
        // spent 10000 -> remaining 42000, on day 1 of a 30-day month -> 30 days remaining
        // daily pace = 1400 >= outing cost 1000 -> Safe
        val input = PacerInput(
            monthlyIncome = 90_000.0,
            fixedCosts = 20_000.0,
            averageOutingCost = 1_000.0,
            savingsMode = SavingsMode.PERCENTAGE,
            savingsPercentage = 20.0
        )
        val today = LocalDate.of(2026, 4, 1) // 30-day month
        val breakdown = PacerEngine.evaluate(input, currentMonthSpent = 10_000.0, today = today)

        assertTrue(breakdown.result is PacerResult.Safe)
        assertEquals(1_400.0, (breakdown.result as PacerResult.Safe).dailyAllowance, 0.001)
    }

    @Test
    fun `hold off when daily pace is below outing cost`() {
        // discretionary 52000, spent 45000 -> remaining 7000, 10 days remaining -> pace 700
        // outing cost 1500 -> waitDays = ceil(1500/700) = 3
        val input = PacerInput(
            monthlyIncome = 90_000.0,
            fixedCosts = 20_000.0,
            averageOutingCost = 1_500.0,
            savingsMode = SavingsMode.PERCENTAGE,
            savingsPercentage = 20.0
        )
        val today = LocalDate.of(2026, 4, 21) // 30-day month, 10 days remaining
        val breakdown = PacerEngine.evaluate(input, currentMonthSpent = 45_000.0, today = today)

        assertTrue(breakdown.result is PacerResult.HoldOff)
        val holdOff = breakdown.result as PacerResult.HoldOff
        assertEquals(3, holdOff.waitDays)
        assertEquals(today.plusDays(3), holdOff.nextSafeDate)
    }

    @Test
    fun `budget exceeded when remaining allowance is zero or negative`() {
        val input = PacerInput(
            monthlyIncome = 90_000.0,
            fixedCosts = 20_000.0,
            averageOutingCost = 1_500.0,
            savingsMode = SavingsMode.PERCENTAGE,
            savingsPercentage = 20.0
        )
        val today = LocalDate.of(2026, 4, 15)
        val breakdown = PacerEngine.evaluate(input, currentMonthSpent = 60_000.0, today = today)

        assertTrue(breakdown.result is PacerResult.BudgetExceeded)
    }

    @Test
    fun `required income percentage matches mode 2 target`() {
        val input = PacerInput(
            monthlyIncome = 40_000.0,
            fixedCosts = 10_000.0,
            averageOutingCost = 1_000.0,
            savingsMode = SavingsMode.TARGET_AMOUNT,
            targetAmount = 12_000.0,
            targetMonths = 6
        )
        // S = 2000/month, required % = (2000/40000)*100 = 5%
        assertEquals(5.0, PacerEngine.requiredIncomePercentage(input), 0.001)
    }
}
