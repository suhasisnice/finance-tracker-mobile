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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    transactionRepository: TransactionRepository
) : ViewModel() {

    // flatMapLatest, not combine: the transactions window itself depends on
    // settings.trackingStartDate, so it has to be derived per-settings-emission.
    val breakdown: StateFlow<PacerBreakdown?> = settingsRepository.observeSettings()
        .filterNotNull()
        .flatMapLatest { settings ->
            transactionRepository.observeSinceTracking(settings.trackingStartDate)
                .map { transactions ->
                    val spent = transactions.sumOf { it.amount }
                    PacerEngine.evaluate(settings.toPacerInput(), spentSinceTrackingStart = spent)
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

private fun SettingsEntity.toPacerInput(): PacerInput = PacerInput(
    monthlyIncome = monthlyIncome,
    fixedCosts = fixedCosts,
    averageOutingCost = averageOutingCost,
    currentBalance = currentBankBalance,
    savingsMode = SavingsMode.fromStorageValue(savingsMode),
    savingsPercentage = savingsPercentage,
    targetAmount = targetAmount,
    targetMonths = targetMonths
)
