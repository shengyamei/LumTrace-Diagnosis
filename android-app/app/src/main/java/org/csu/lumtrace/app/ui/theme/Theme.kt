package org.csu.lumtrace.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isSpecified

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF00796B),
    background = Color(0xFFF7F9FC),
    surface = Color.White
)

private fun TextStyle.scaled(scale: Float): TextStyle =
    if (fontSize.isSpecified && lineHeight.isSpecified)
        copy(fontSize = fontSize * scale, lineHeight = lineHeight * scale)
    else this

@Composable
fun LumTraceTheme(
    fontScale: Float = 1.15f,
    content: @Composable () -> Unit
) {
    val base = Typography()
    val typography = Typography(
        headlineLarge = base.headlineLarge.scaled(fontScale),
        headlineMedium = base.headlineMedium.scaled(fontScale),
        titleLarge = base.titleLarge.scaled(fontScale),
        titleMedium = base.titleMedium.scaled(fontScale),
        bodyLarge = base.bodyLarge.scaled(fontScale),
        bodyMedium = base.bodyMedium.scaled(fontScale),
        labelLarge = base.labelLarge.scaled(fontScale)
    )
    MaterialTheme(colorScheme = LightColors, typography = typography, content = content)
}