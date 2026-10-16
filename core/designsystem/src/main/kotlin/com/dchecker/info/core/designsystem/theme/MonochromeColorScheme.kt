/*
 * Copyright 2026 Duck Apps Contributor
 * If you have any questions, suggestions, or other inquiries, please email Eltavine <me@eltavine.com>.
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

package com.dchecker.info.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Every accent role is the text color, so buttons, switches, links and progress invert with the
// theme: black on light, white on dark. The only hues left in the app are the status colors of
// DuckPalette, which carry a detector's evidence, and the error colors, which Material keeps.
// The neutrals are Apple's grouped grays.
// DChecker: the dark scheme carries the fork's dark-green identity instead of pure
// monochrome; status hues still come from DuckPalette.

internal val LightMonochromeScheme: ColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E5EA),
    onPrimaryContainer = Color.Black,
    inversePrimary = Color.White,
    secondary = Color(0xFF636366),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5E5EA),
    onSecondaryContainer = Color.Black,
    tertiary = Color(0xFF636366),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE5E5EA),
    onTertiaryContainer = Color.Black,
    background = Color(0xFFF2F2F7),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF636366),
    surfaceTint = Color.Black,
    inverseSurface = Color(0xFF1C1C1E),
    inverseOnSurface = Color.White,
    outline = Color(0xFFAEAEB2),
    outlineVariant = Color(0xFFC6C6C8),
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE5E5EA),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF9F9FB),
    surfaceContainer = Color(0xFFF2F2F7),
    surfaceContainerHigh = Color(0xFFEBEBF0),
    surfaceContainerHighest = Color(0xFFE5E5EA),
)

internal val DarkMonochromeScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF00E676),
    onPrimary = Color(0xFF003313),
    primaryContainer = Color(0xFF00522B),
    onPrimaryContainer = Color(0xFF8CF5B0),
    inversePrimary = Color(0xFF003917),
    secondary = Color(0xFF7FDCA4),
    onSecondary = Color(0xFF00391C),
    secondaryContainer = Color(0xFF1B5133),
    onSecondaryContainer = Color(0xFF9BF2BC),
    tertiary = Color(0xFFB8CC9E),
    onTertiary = Color(0xFF243513),
    tertiaryContainer = Color(0xFF3A4C28),
    onTertiaryContainer = Color(0xFFD4E8B8),
    background = Color(0xFF0A0F0C),
    onBackground = Color(0xFFDEE5DD),
    surface = Color(0xFF0E1511),
    onSurface = Color(0xFFDEE5DD),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC2CDBF),
    surfaceTint = Color(0xFF00E676),
    inverseSurface = Color(0xFFDEE5DD),
    inverseOnSurface = Color(0xFF0A0F0C),
    outline = Color(0xFF8C978A),
    outlineVariant = Color(0xFF414942),
    surfaceBright = Color(0xFF1A221C),
    surfaceDim = Color(0xFF070B09),
    surfaceContainerLowest = Color(0xFF050806),
    surfaceContainerLow = Color(0xFF0E1511),
    surfaceContainer = Color(0xFF121A15),
    surfaceContainerHigh = Color(0xFF1A221C),
    surfaceContainerHighest = Color(0xFF242D26),
)
