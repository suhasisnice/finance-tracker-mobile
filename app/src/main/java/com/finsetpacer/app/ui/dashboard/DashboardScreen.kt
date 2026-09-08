package com.finsetpacer.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finsetpacer.app.domain.model.PacerBreakdown
import com.finsetpacer.app.domain.model.PacerResult
import com.finsetpacer.app.ui.theme.PacerAmber
import com.finsetpacer.app.ui.theme.PacerGreen
import com.finsetpacer.app.ui.theme.PacerRed
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val breakdown by viewModel.breakdown.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Your Pace") }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val current = breakdown
            if (current == null) {
                Text("Crunching your numbers…", style = MaterialTheme.typography.bodyLarge)
            } else {
                PacerResultCard(current)
                BreakdownCard(current)
            }
        }
    }
}

@Composable
private fun PacerResultCard(breakdown: PacerBreakdown) {
    val (headline, detail, color) = when (val result = breakdown.result) {
        is PacerResult.Safe -> Triple(
            "You're safe to go out today!",
            "Daily allowance: ₹${result.dailyAllowance.formatMoney()}",
            PacerGreen
        )
        is PacerResult.HoldOff -> Triple(
            "Hold off! Wait ${result.waitDays} day${if (result.waitDays == 1) "" else "s"}",
            "Next safe outing date: ${result.nextSafeDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
            PacerAmber
        )
        PacerResult.BudgetExceeded -> Triple(
            "Budget exceeded!",
            "You've spent your entire discretionary budget for this month.",
            PacerRed
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(text = detail, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun BreakdownCard(breakdown: PacerBreakdown) {
    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("This month", style = MaterialTheme.typography.titleMedium)
            BreakdownRow("Current balance", breakdown.currentBalance)
            BreakdownRow("Savings target", breakdown.monthlySavingsTarget)
            BreakdownRow("Spent since you started tracking", breakdown.spentSinceTrackingStart)
            BreakdownRow("Remaining allowance", breakdown.remainingAllowance)
            BreakdownRow("Days remaining", breakdown.daysRemainingInMonth.toDouble(), isMoney = false)
            BreakdownRow("Safe daily pace", breakdown.safeDailyPace)
            Text(
                text = "Theoretical monthly discretionary budget (income − savings − bills): " +
                    "₹${breakdown.discretionaryBudget.formatMoney()}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: Double, isMoney: Boolean = true) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .background(Color.Transparent)) {
        Text(
            text = "$label: ${if (isMoney) "₹${value.formatMoney()}" else value.roundToInt().toString()}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun Double.formatMoney(): String = "%,.2f".format(this)
