package com.example.vitruvianredux.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class ThemeMode { SYSTEM, LIGHT, DARK }

fun ThemeMode.resolveDarkTheme(systemInDarkTheme: Boolean): Boolean =
    when (this) {
        ThemeMode.SYSTEM -> systemInDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

@Immutable
data class AppStatusColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color
)

@Immutable
data class AppBrushes(
    val screenBackground: Brush,
    val primaryAccent: Brush,
    val celebration: Brush
)

@Immutable
data class AppChartColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val positive: Color,
    val warning: Color,
    val negative: Color,
    val neutral: Color,
    val comparison: Color
) {
    val series: List<Color>
        get() = listOf(
            primary,
            secondary,
            tertiary,
            positive,
            warning,
            negative,
            neutral,
            comparison
        )
}

private val DarkColorScheme = darkColorScheme(
    primary = MonoTextPrimary,
    onPrimary = MonoBlack,
    primaryContainer = MonoSurfaceHighest,
    onPrimaryContainer = MonoTextPrimary,

    secondary = MonoTextSecondary,
    onSecondary = MonoBlack,
    secondaryContainer = MonoSurfaceHigh,
    onSecondaryContainer = MonoTextPrimary,

    tertiary = MonoTextMuted,
    onTertiary = MonoBlack,
    tertiaryContainer = MonoSurfaceHigh,
    onTertiaryContainer = MonoTextPrimary,

    background = MonoBlack,
    onBackground = MonoTextPrimary,

    surface = MonoSurface,
    onSurface = MonoTextPrimary,
    surfaceVariant = MonoSurfaceHigh,
    onSurfaceVariant = MonoTextSecondary,
    surfaceContainer = MonoSurface,
    surfaceContainerHigh = MonoSurfaceHigh,
    surfaceContainerHighest = MonoSurfaceHighest,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedContainer,
    onErrorContainer = Color.White,

    outline = MonoOutline,
    outlineVariant = MonoOutlineSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = PrimaryOrangeContainer,
    onPrimaryContainer = LightTextPrimary,

    secondary = LightTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceHighest,
    onSecondaryContainer = LightTextPrimary,

    tertiary = PrimaryOrangeActive,
    onTertiary = Color.White,
    tertiaryContainer = PrimaryOrangeContainer,
    onTertiaryContainer = LightTextPrimary,

    background = LightBackground,
    onBackground = LightTextPrimary,

    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceHigh,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceHigh,
    surfaceContainerHighest = LightSurfaceHighest,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedContainerLight,
    onErrorContainer = LightTextPrimary,

    outline = LightOutline,
    outlineVariant = LightOutlineSubtle
)

private val DarkAppStatusColors = AppStatusColors(
    success = SuccessGreen,
    onSuccess = MonoBlack,
    successContainer = SuccessGreenContainer,
    onSuccessContainer = Color.White,
    warning = WarningAmber,
    onWarning = MonoBlack,
    warningContainer = WarningAmberContainer,
    onWarningContainer = Color.White,
    info = InfoBlue,
    onInfo = Color.White,
    infoContainer = InfoBlueContainer,
    onInfoContainer = Color.White
)

private val LightAppStatusColors = AppStatusColors(
    success = SuccessGreen,
    onSuccess = Color.White,
    successContainer = SuccessGreenContainerLight,
    onSuccessContainer = SuccessGreenContainer,
    warning = WarningAmber,
    onWarning = LightTextPrimary,
    warningContainer = WarningAmberContainerLight,
    onWarningContainer = WarningAmberContainer,
    info = InfoBlue,
    onInfo = Color.White,
    infoContainer = InfoBlueContainerLight,
    onInfoContainer = InfoBlueContainer
)

private val DarkAppBrushes = AppBrushes(
    screenBackground = Brush.verticalGradient(
        listOf(MonoBlack, MonoSurface, MonoSurfaceHigh)
    ),
    primaryAccent = Brush.linearGradient(
        listOf(MonoTextPrimary, MonoTextSecondary)
    ),
    celebration = Brush.linearGradient(
        listOf(WarningAmber, PrimaryOrange)
    )
)

private val LightAppBrushes = AppBrushes(
    screenBackground = Brush.verticalGradient(
        listOf(LightBackground, LightSurface, PrimaryOrangeContainer)
    ),
    primaryAccent = Brush.linearGradient(
        listOf(PrimaryOrange, PrimaryOrangeActive)
    ),
    celebration = Brush.linearGradient(
        listOf(WarningAmber, PrimaryOrange)
    )
)

private val DarkAppChartColors = AppChartColors(
    primary = PrimaryOrange,
    secondary = WarningAmber,
    tertiary = ChartGold,
    positive = SuccessGreen,
    warning = ChartCopper,
    negative = ErrorRed,
    neutral = ChartSlate,
    comparison = ChartStone
)

private val LightAppChartColors = AppChartColors(
    primary = PrimaryOrangeActive,
    secondary = WarningAmber,
    tertiary = ChartGoldDark,
    positive = SuccessGreen,
    warning = ChartCopperDark,
    negative = ErrorRed,
    neutral = ChartSlateDark,
    comparison = ChartStoneDark
)

private val LocalAppStatusColors = staticCompositionLocalOf { DarkAppStatusColors }
private val LocalAppBrushes = staticCompositionLocalOf { DarkAppBrushes }
private val LocalAppChartColors = staticCompositionLocalOf { DarkAppChartColors }

val MaterialTheme.appStatusColors: AppStatusColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppStatusColors.current

val MaterialTheme.appBrushes: AppBrushes
    @Composable
    @ReadOnlyComposable
    get() = LocalAppBrushes.current

val MaterialTheme.appChartColors: AppChartColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppChartColors.current

@Composable
fun VitruvianProjectPhoenixTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkColors = themeMode.resolveDarkTheme(isSystemInDarkTheme())

    CompositionLocalProvider(
        LocalAppStatusColors provides if (useDarkColors) DarkAppStatusColors else LightAppStatusColors,
        LocalAppBrushes provides if (useDarkColors) DarkAppBrushes else LightAppBrushes,
        LocalAppChartColors provides if (useDarkColors) DarkAppChartColors else LightAppChartColors
    ) {
        MaterialTheme(
            colorScheme = if (useDarkColors) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = ExpressiveShapes,
            content = content
        )
    }
}
