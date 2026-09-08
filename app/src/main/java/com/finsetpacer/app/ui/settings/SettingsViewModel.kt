package com.finsetpacer.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finsetpacer.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /** @param existingTrackingStartDate must be preserved as-is — Settings edits never reset it. */
    fun save(state: SettingsFormState, existingTrackingStartDate: String, onSaved: () -> Unit) {
        if (!state.isValid) return
        viewModelScope.launch {
            settingsRepository.save(state.toEntity(trackingStartDate = existingTrackingStartDate))
            onSaved()
        }
    }
}
