package com.finsetpacer.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PacerGreen,
    secondary = PacerGreenLight,
    error = PacerRed,
    background = PacerSurface,
    surface = PacerSurface,
    onSurface = PacerOnSurface
)

private val DarkColors = darkColorScheme(
    primary = PacerGreenLight,
    secondary = PacerGreen,
    error = PacerRed
)

@Composable
fun FinSetPacerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
