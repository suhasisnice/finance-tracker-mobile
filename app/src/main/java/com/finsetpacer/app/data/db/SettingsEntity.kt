package com.finsetpacer.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton settings row — always stored/read with id = SETTINGS_ID.
 * Mirrors the `settings` table in the spec (§5).
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = SETTINGS_ID,
    val monthlyIncome: Double,
    val fixedCosts: Double,
    val averageOutingCost: Double,
    val savingsMode: String, // "PERCENTAGE" or "TARGET_AMOUNT"
    val savingsPercentage: Double?,
    val targetAmount: Double?,
    val targetMonths: Int?,
    /** User-entered snapshot balance, driving the remaining-allowance formula. */
    val currentBankBalance: Double,
    /**
     * "yyyy-MM-dd HH:mm:ss" — set once, the moment onboarding is first completed
     * (MonthWindow.todayStart()), and preserved across later Settings edits.
     * "Spent so far" only counts transactions from this point forward, so
     * spending from before the user started using the app isn't held against them.
     */
    val trackingStartDate: String
) {
    companion object {
        const val SETTINGS_ID = 1
    }
}
