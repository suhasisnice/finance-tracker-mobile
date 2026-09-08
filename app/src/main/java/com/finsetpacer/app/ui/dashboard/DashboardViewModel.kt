package com.finsetpacer.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finsetpacer.app.data.db.SettingsEntity
import com.finsetpacer.app.data.repository.SettingsRepository
import com.finsetpacer.app.data.repository.TransactionRepository
import com.finsetpacer.app.domain.PacerEngine
import com.finsetpacer.app.domain.model.PacerBreakdown
import com.finsetpacer.app.domain.model.PacerInput
import com.finsetpacer.app.domain.model.SavingsMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    transactionRepository: TransactionRepository
) : ViewModel() {

    val breakdown: StateFlow<PacerBreakdown?> = combine(
        settingsRepository.observeSettings().filterNotNull(),
        transactionRepository.observeCurrentMonth()
    ) { settings, transactions ->
        val spent = transactions.sumOf { it.amount }
        PacerEngine.evaluate(settings.toPacerInput(), currentMonthSpent = spent)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

private fun SettingsEntity.toPacerInput(): PacerInput = PacerInput(
    monthlyIncome = monthlyIncome,
    fixedCosts = fixedCosts,
    averageOutingCost = averageOutingCost,
    savingsMode = SavingsMode.fromStorageValue(savingsMode),
    savingsPercentage = savingsPercentage,
    targetAmount = targetAmount,
    targetMonths = targetMonths
)
