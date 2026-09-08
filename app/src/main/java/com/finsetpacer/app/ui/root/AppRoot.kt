package com.finsetpacer.app.ui.root

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finsetpacer.app.ui.navigation.FinSetNavHost
import com.finsetpacer.app.ui.onboarding.OnboardingScreen
import com.finsetpacer.app.ui.permission.SMS_PERMISSIONS
import com.finsetpacer.app.ui.permission.SmsPermissionScreen
import com.finsetpacer.app.ui.permission.hasSmsPermissions

/**
 * Top-level router: no SMS permission yet -> permission gate (asked first, since
 * Onboarding wants SMS history to suggest an outing cost); permission granted but
 * no settings yet -> Onboarding; otherwise the bottom-nav'd main app.
 */
@Composable
fun AppRoot(viewModel: AppRootViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val suggestedOutingCost by viewModel.suggestedOutingCost.collectAsStateWithLifecycle()
    var hasSmsPermission by remember { mutableStateOf(hasSmsPermissions(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        hasSmsPermission = granted
    }

    when {
        !hasSmsPermission -> SmsPermissionScreen(
            onRequestPermission = { permissionLauncher.launch(SMS_PERMISSIONS) }
        )
        settings == null -> {
            LaunchedEffect(Unit) { viewModel.scanHistoryForOnboarding() }
            OnboardingScreen(suggestedOutingCost = suggestedOutingCost)
        }
        else -> {
            LaunchedEffect(Unit) { viewModel.refreshCurrentMonth() }
            FinSetNavHost()
        }
    }
}
