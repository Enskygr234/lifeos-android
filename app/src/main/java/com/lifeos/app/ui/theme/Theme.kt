package com.lifeos.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Purple = Color(0xFF8B5CF6)
val PurpleLight = Color(0xFFA78BFA)
val Blue = Color(0xFF3B82F6)
val Cyan = Color(0xFF22D3EE)
val Success = Color(0xFF34D399)
val Warning = Color(0xFFFBBF24)
val Danger = Color(0xFFF87171)

val AccentGradient = Brush.linearGradient(listOf(Purple, Blue))

/** Alışkanlık renk paleti */
val HabitColors = listOf(
    Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFF22D3EE), Color(0xFF34D399),
    Color(0xFFFBBF24), Color(0xFFF87171), Color(0xFFEC4899), Color(0xFFA3E635),
)

private val DarkColors = darkColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2E1F5E),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Blue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1B2D55),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = Cyan,
    background = Color(0xFF0B0B14),
    onBackground = Color(0xFFE8E8F0),
    surface = Color(0xFF0F0F1C),
    onSurface = Color(0xFFE8E8F0),
    surfaceVariant = Color(0xFF1C1C31),
    onSurfaceVariant = Color(0xFFA3A3BD),
    surfaceContainerLowest = Color(0xFF0B0B14),
    surfaceContainerLow = Color(0xFF13131F),
    surfaceContainer = Color(0xFF151526),
    surfaceContainerHigh = Color(0xFF1C1C31),
    surfaceContainerHighest = Color(0xFF25253D),
    outline = Color(0xFF3A3A55),
    outlineVariant = Color(0xFF2A2A40),
    error = Danger,
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE4FF),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = Color(0xFF2563EB),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE7FF),
    onSecondaryContainer = Color(0xFF0B2A6B),
    tertiary = Color(0xFF0891B2),
    background = Color(0xFFF6F5FB),
    onBackground = Color(0xFF16162A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF16162A),
    surfaceVariant = Color(0xFFEDEBF7),
    onSurfaceVariant = Color(0xFF5B5B76),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF9F8FD),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF1EFFA),
    surfaceContainerHighest = Color(0xFFE8E5F5),
    outline = Color(0xFFC9C6DC),
    outlineVariant = Color(0xFFE2E0EE),
    error = Color(0xFFDC2626),
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
)

@Composable
fun LifeOSTheme(themeMode: String, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
