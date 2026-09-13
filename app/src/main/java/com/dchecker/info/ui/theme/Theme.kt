/*
 * Copyright 2026 Duck Apps Contributor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dchecker.info.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GreenPrimary = Color(0xFF00E676)
private val GreenOnPrimary = Color(0xFF003313)
private val GreenPrimaryContainer = Color(0xFF00522B)
private val GreenOnPrimaryContainer = Color(0xFF8CF5B0)
private val GreenSecondary = Color(0xFF7FDCA4)
private val GreenOnSecondary = Color(0xFF00391C)
private val GreenSecondaryContainer = Color(0xFF1B5133)
private val GreenOnSecondaryContainer = Color(0xFF9BF2BC)
private val GreenTertiary = Color(0xFFB8CC9E)
private val GreenOnTertiary = Color(0xFF243513)
private val GreenTertiaryContainer = Color(0xFF3A4C28)
private val GreenOnTertiaryContainer = Color(0xFFD4E8B8)
private val DarkBackground = Color(0xFF0A0F0C)
private val DarkOnBackground = Color(0xFFDEE5DD)
private val DarkSurface = Color(0xFF0E1511)
private val DarkOnSurface = Color(0xFFDEE5DD)
private val DarkSurfaceVariant = Color(0xFF414942)
private val DarkOnSurfaceVariant = Color(0xFFC2CDBF)
private val DarkOutline = Color(0xFF8C978A)
private val DarkOutlineVariant = Color(0xFF414942)
private val DarkError = Color(0xFFFF7A76)
private val DarkOnError = Color(0xFF490004)
private val DarkErrorContainer = Color(0xFF722B28)
private val DarkOnErrorContainer = Color(0xFFFFDAD5)

private val DCheckerDarkColors = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenOnPrimaryContainer,
    secondary = GreenSecondary,
    onSecondary = GreenOnSecondary,
    secondaryContainer = GreenSecondaryContainer,
    onSecondaryContainer = GreenOnSecondaryContainer,
    tertiary = GreenTertiary,
    onTertiary = GreenOnTertiary,
    tertiaryContainer = GreenTertiaryContainer,
    onTertiaryContainer = GreenOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
)

@Composable
fun DuckDetectorTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = DCheckerDarkColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
