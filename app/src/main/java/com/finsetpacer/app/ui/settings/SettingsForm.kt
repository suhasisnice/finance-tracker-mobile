package com.finsetpacer.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.finsetpacer.app.domain.model.SavingsMode

@Composable
fun SettingsForm(
    state: SettingsFormState,
    saveLabel: String,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.monthlyIncome,
            onValueChange = { state.monthlyIncome = it },
            label = { Text("Monthly income (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.fixedCosts,
            onValueChange = { state.fixedCosts = it },
            label = { Text("Fixed monthly bills (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.averageOutingCost,
            onValueChange = { state.averageOutingCost = it },
            label = { Text("Average outing cost (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        SavingsModeDropdown(
            selected = state.savingsMode,
            onSelected = { state.savingsMode = it }
        )

        when (state.savingsMode) {
            SavingsMode.PERCENTAGE -> OutlinedTextField(
                value = state.savingsPercentage,
                onValueChange = { state.savingsPercentage = it },
                label = { Text("Savings target (% of income)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            SavingsMode.TARGET_AMOUNT -> {
                OutlinedTextField(
                    value = state.targetAmount,
                    onValueChange = { state.targetAmount = it },
                    label = { Text("Target amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.targetMonths,
                    onValueChange = { state.targetMonths = it },
                    label = { Text("Over how many months") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = onSave,
            enabled = state.isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(saveLabel)
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SavingsModeDropdown(
    selected: SavingsMode,
    onSelected: (SavingsMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (selected) {
        SavingsMode.PERCENTAGE -> "% of income"
        SavingsMode.TARGET_AMOUNT -> "Target amount over time"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Savings goal mode") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SavingsMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (mode) {
                                SavingsMode.PERCENTAGE -> "% of income"
                                SavingsMode.TARGET_AMOUNT -> "Target amount over time"
                            }
                        )
                    },
                    onClick = {
                        onSelected(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}
