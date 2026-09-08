package com.finsetpacer.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finsetpacer.app.data.repository.SettingsRepository
import com.finsetpacer.app.ui.settings.SettingsFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    fun save(state: SettingsFormState) {
        if (!state.isValid) return
        viewModelScope.launch {
            settingsRepository.save(state.toEntity())
        }
    }
}
