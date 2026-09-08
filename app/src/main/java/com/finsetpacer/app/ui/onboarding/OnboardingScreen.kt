package com.finsetpacer.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finsetpacer.app.ui.settings.SettingsForm
import com.finsetpacer.app.ui.settings.SettingsFormState
import com.finsetpacer.app.ui.theme.PacerGreen

/**
 * First-run setup — collects the §2 "Settings & Inputs" (income, fixed bills,
 * outing cost, savings goal mode). Once saved, AppRoot's settings Flow updates
 * and this screen is replaced automatically (no explicit navigation call needed).
 *
 * @param suggestedOutingCost average of the user's last 6 months of food/
 * entertainment SMS transactions (see AppRootViewModel.scanHistoryForOnboarding),
 * pre-filled into the outing cost field once available — still user-editable.
 */
@Composable
fun OnboardingScreen(
    suggestedOutingCost: Double?,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val formState = remember { SettingsFormState() }

    LaunchedEffect(suggestedOutingCost) {
        if (suggestedOutingCost != null && formState.averageOutingCost.isBlank()) {
            formState.averageOutingCost = "%.0f".format(suggestedOutingCost)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Let's set up your pace",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tell us about your income, bills, and savings goal so FinSet Pacer " +
                "can tell you when it's safe to go out.",
            style = MaterialTheme.typography.bodyLarge
        )
        if (suggestedOutingCost != null) {
            Text(
                text = "We pre-filled \"Average outing cost\" using the average of your last " +
                    "6 months of food & entertainment spending from SMS — feel free to adjust it.",
                style = MaterialTheme.typography.bodyLarge,
                color = PacerGreen
            )
        }
        SettingsForm(
            state = formState,
            saveLabel = "Get started",
            onSave = { viewModel.save(formState) }
        )
    }
}
