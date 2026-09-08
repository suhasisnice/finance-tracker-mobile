package com.finsetpacer.app.ui.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finsetpacer.app.data.db.SettingsEntity
import com.finsetpacer.app.data.repository.SettingsRepository
import com.finsetpacer.app.sms.SmsInboxScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the top-level "which screen" decision in AppRoot:
 * !hasSmsPermission -> permission gate; settings == null -> Onboarding; else main nav.
 */
@HiltViewModel
class AppRootViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val smsInboxScanner: SmsInboxScanner
) : ViewModel() {

    val settings: StateFlow<SettingsEntity?> = settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _suggestedOutingCost = MutableStateFlow<Double?>(null)
    /** Average of the last 6 months' food/entertainment SMS transactions — null until scanned. */
    val suggestedOutingCost: StateFlow<Double?> = _suggestedOutingCost.asStateFlow()

    /** First-run only: scans SMS history for a suggested outing cost, feeding OnboardingScreen. */
    fun scanHistoryForOnboarding() {
        viewModelScope.launch {
            _suggestedOutingCost.value = smsInboxScanner.estimateOutingCostFromHistory()
        }
    }

    /** Called on every app open once settings exist — backfills the current month for the dashboard. */
    fun refreshCurrentMonth() {
        viewModelScope.launch {
            smsInboxScanner.backfillCurrentMonth()
        }
    }
}
