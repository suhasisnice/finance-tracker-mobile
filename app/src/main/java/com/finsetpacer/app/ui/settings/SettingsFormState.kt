package com.finsetpacer.app.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.finsetpacer.app.data.db.SettingsEntity
import com.finsetpacer.app.domain.model.SavingsMode

/** Shared, screen-agnostic form state for both Onboarding and Settings (§3.A inputs). */
class SettingsFormState {
    var monthlyIncome by mutableStateOf("")
    var fixedCosts by mutableStateOf("")
    var averageOutingCost by mutableStateOf("")
    var savingsMode by mutableStateOf(SavingsMode.PERCENTAGE)
    var savingsPercentage by mutableStateOf("")
    var targetAmount by mutableStateOf("")
    var targetMonths by mutableStateOf("")

    fun loadFrom(settings: SettingsEntity) {
        monthlyIncome = settings.monthlyIncome.toPlainStringOrEmpty()
        fixedCosts = settings.fixedCosts.toPlainStringOrEmpty()
        averageOutingCost = settings.averageOutingCost.toPlainStringOrEmpty()
        savingsMode = SavingsMode.fromStorageValue(settings.savingsMode)
        savingsPercentage = settings.savingsPercentage?.toPlainStringOrEmpty() ?: ""
        targetAmount = settings.targetAmount?.toPlainStringOrEmpty() ?: ""
        targetMonths = settings.targetMonths?.toString() ?: ""
    }

    /** Basic presence/validity check before allowing Save. */
    val isValid: Boolean
        get() {
            val incomeOk = monthlyIncome.toDoubleOrNull() != null
            val fixedOk = fixedCosts.toDoubleOrNull() != null
            val outingOk = averageOutingCost.toDoubleOrNull() != null
            val modeOk = when (savingsMode) {
                SavingsMode.PERCENTAGE -> savingsPercentage.toDoubleOrNull() != null
                SavingsMode.TARGET_AMOUNT ->
                    targetAmount.toDoubleOrNull() != null && targetMonths.toIntOrNull() != null
            }
            return incomeOk && fixedOk && outingOk && modeOk
        }

    fun toEntity(): SettingsEntity = SettingsEntity(
        monthlyIncome = monthlyIncome.toDouble(),
        fixedCosts = fixedCosts.toDouble(),
        averageOutingCost = averageOutingCost.toDouble(),
        savingsMode = savingsMode.name,
        savingsPercentage = savingsPercentage.toDoubleOrNull(),
        targetAmount = targetAmount.toDoubleOrNull(),
        targetMonths = targetMonths.toIntOrNull()
    )
}

private fun Double.toPlainStringOrEmpty(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
