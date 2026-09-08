package com.finsetpacer.app.ui.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finsetpacer.app.data.db.SettingsEntity
import com.finsetpacer.app.data.repository.SettingsRepository
import com.finsetpacer.app.sms.SmsInboxScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the top-level "which screen" decision in AppRoot:
 * settings == null -> Onboarding; else permission gate / main nav (see AppRoot.kt).
 */
@HiltViewModel
class AppRootViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val smsInboxScanner: SmsInboxScanner
) : ViewModel() {

    val settings: StateFlow<SettingsEntity?> = settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Called once SMS permission is confirmed granted — backfills the current month. */
    fun onSmsPermissionGranted() {
        viewModelScope.launch {
            smsInboxScanner.backfillCurrentMonth()
        }
    }
}
