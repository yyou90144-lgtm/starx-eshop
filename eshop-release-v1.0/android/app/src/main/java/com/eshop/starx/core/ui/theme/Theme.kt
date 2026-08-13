package com.eshop.starx.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = StarxPrimary,
    onPrimary = Color.White,
    primaryContainer = StarxPrimarySoft,
    onPrimaryContainer = StarxPrimaryStrong,
    secondary = StarxTextSoft,
    onSecondary = Color.White,
    secondaryContainer = StarxPrimarySoft,
    onSecondaryContainer = StarxPrimaryStrong,
    tertiary = StarxMuted,
    onTertiary = Color.White,
    tertiaryContainer = StarxSurfaceSoft,
    onTertiaryContainer = StarxInk,
    background = StarxPage,
    onBackground = StarxInk,
    surface = StarxSurface,
    onSurface = StarxInk,
    surfaceVariant = StarxSurfaceSoft,
    onSurfaceVariant = StarxMuted,
    outline = StarxBorder,
    outlineVariant = StarxBorder,
    surfaceBright = StarxSurface,
    surfaceDim = StarxPage,
    surfaceContainerLowest = StarxSurface,
    surfaceContainerLow = StarxSurfaceSoft,
    surfaceContainer = StarxSurface,
    surfaceContainerHigh = StarxPage,
    surfaceContainerHighest = StarxBorder,
    inverseSurface = StarxInk,
    inverseOnSurface = StarxSurface,
    inversePrimary = StarxPrimarySoft,
    error = StarxPrimaryStrong,
)

@Composable
fun StarxTheme(
    // P2 固定浅色零售风；忽略系统深色以免偏离品牌
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // darkTheme 保留参数便于以后扩展，当前始终 light
    @Suppress("UNUSED_VARIABLE")
    val ignored = darkTheme

    MaterialTheme(
        colorScheme = LightColors,
        typography = StarxTypography,
        content = content,
    )
}
