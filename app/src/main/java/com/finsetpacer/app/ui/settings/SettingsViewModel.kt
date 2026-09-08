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

    fun save(state: SettingsFormState, onSaved: () -> Unit) {
        if (!state.isValid) return
        viewModelScope.launch {
            settingsRepository.save(state.toEntity())
            onSaved()
        }
    }
}
