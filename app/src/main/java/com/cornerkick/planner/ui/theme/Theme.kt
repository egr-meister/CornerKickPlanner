package com.cornerkick.planner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * The app uses one bold light color scheme in both system modes so the
 * "Orange Set-Piece Board" identity stays consistent. Dynamic color is disabled
 * on purpose to keep the strong orange/black/white palette.
 */
private val CornerKickColorScheme = lightColorScheme(
    primary = StrongOrange,
    onPrimary = WhiteText,
    primaryContainer = SoftOrangePanel,
    onPrimaryContainer = CardDarkText,
    secondary = DarkGraphite,
    onSecondary = WhiteText,
    secondaryContainer = DarkGraphite,
    onSecondaryContainer = WhiteText,
    tertiary = DeepOrange,
    onTertiary = WhiteText,
    background = LightAppBackground,
    onBackground = DarkText,
    surface = WhiteCard,
    onSurface = DarkText,
    surfaceVariant = SoftOrangePanel,
    onSurfaceVariant = SecondaryGrayText,
    outline = MutedLabelGray,
    error = ErrorRed,
    onError = WhiteText,
)

@Composable
fun CornerKickPlannerTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CornerKickColorScheme,
        typography = AppTypography,
        content = content,
    )
}
