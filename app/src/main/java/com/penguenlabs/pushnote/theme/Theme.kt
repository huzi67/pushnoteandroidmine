package com.penguenlabs.pushnote.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val lightColorScheme = lightColorScheme(
    primary = neuLightPrimary,
    primaryContainer = neuLightPrimaryContainer,
    onPrimary = neuLightOnPrimary,
    secondary = neuLightSecondary,
    secondaryContainer = neuLightSecondaryContainer,
    onSecondary = neuLightOnSecondary,
    tertiary = neuLightTertiary,
    tertiaryContainer = neuLightTertiaryContainer,
    onTertiary = neuLightOnTertiary,
    error = neuLightError,
    errorContainer = neuLightErrorContainer,
    onError = neuLightOnError,
    onErrorContainer = neuLightOnError,
    background = neuLightBackground,
    onBackground = neuLightOnBackground,
    surface = neuLightSurface,
    onSurface = neuLightOnSurface,
    surfaceVariant = neuLightSurfaceVariant,
    onSurfaceVariant = neuLightOnSurfaceVariant,
    outline = neuLightOutline,
    outlineVariant = neuLightOutlineVariant,
    inverseSurface = neuBlack,
    inverseOnSurface = neuWhite,
    inversePrimary = neuLightPrimary,
    scrim = neuShadow
)

private val darkColorScheme = darkColorScheme(
    primary = neuDarkPrimary,
    primaryContainer = neuDarkPrimaryContainer,
    onPrimary = neuDarkOnPrimary,
    secondary = neuDarkSecondary,
    secondaryContainer = neuDarkSecondaryContainer,
    onSecondary = neuDarkOnSecondary,
    tertiary = neuDarkTertiary,
    tertiaryContainer = neuDarkTertiaryContainer,
    onTertiary = neuDarkOnTertiary,
    error = neuDarkError,
    errorContainer = neuDarkErrorContainer,
    onError = neuDarkOnError,
    onErrorContainer = neuDarkOnError,
    background = neuDarkBackground,
    onBackground = neuDarkOnBackground,
    surface = neuDarkSurface,
    onSurface = neuDarkOnSurface,
    surfaceVariant = neuDarkSurfaceVariant,
    onSurfaceVariant = neuDarkOnSurfaceVariant,
    outline = neuDarkOutline,
    outlineVariant = neuDarkOutlineVariant,
    inverseSurface = neuWhite,
    inverseOnSurface = neuBlack,
    inversePrimary = neuDarkPrimary,
    scrim = neuShadow
)

@Composable
fun PushNoteTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    // Neubrutalism enforces a fixed bold palette; dynamic color is disabled.
    val colors = if (darkTheme) darkColorScheme else lightColorScheme

    MaterialTheme(
        colorScheme = colors,
        content = content,
        typography = typography,
        shapes = MaterialTheme.shapes
    )
}
