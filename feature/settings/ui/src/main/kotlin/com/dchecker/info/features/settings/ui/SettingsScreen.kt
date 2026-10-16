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

package com.dchecker.info.features.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Speed
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.dchecker.info.core.designsystem.theme.DuckTheme
import com.dchecker.info.core.ui.components.WrapSafeText
import com.dchecker.info.features.settings.presentation.model.SettingsUiState
import com.dchecker.info.features.settings.ui.components.AboutSection
import com.dchecker.info.features.settings.ui.components.ConsentSettingItem
import com.dchecker.info.features.settings.ui.components.ContributorNameWordmark
import com.dchecker.info.features.settings.ui.components.ContributorsSection
import com.dchecker.info.features.settings.ui.components.SettingsGroup
import com.dchecker.info.features.settings.ui.components.SettingsSection
import com.dchecker.info.features.settings.ui.components.SettingsSwitchItem
import com.dchecker.info.features.settings.ui.components.UiStyleSettingItem
import io.github.xiaotong6666.uihelper.adaptive.AdaptiveContent
import io.github.xiaotong6666.uihelper.mode.LocalUiMode
import io.github.xiaotong6666.uihelper.mode.UiMode

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    consentToggles: List<ConsentToggle>,
    onCheckForUpdates: () -> Unit,
    onGitHubAccelerationChange: (Boolean) -> Unit,
    onUiModeChange: (UiMode) -> Unit,
    onOpenLicenses: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues,
    pageModifier: Modifier = Modifier,
) {
    val uiMode = LocalUiMode.current
    AdaptiveContent(
        miuix = {
            SettingsMiuixPage(
                uiState = uiState,
                uiMode = uiMode,
                consentToggles = consentToggles,
                onCheckForUpdates = onCheckForUpdates,
                onGitHubAccelerationChange = onGitHubAccelerationChange,
                onUiModeChange = onUiModeChange,
                onOpenLicenses = onOpenLicenses,
                scaffoldPadding = scaffoldPadding,
                pageModifier = pageModifier,
            )
        },
        material = {
            SettingsPage(
                uiState = uiState,
                uiMode = uiMode,
                consentToggles = consentToggles,
                onCheckForUpdates = onCheckForUpdates,
                onGitHubAccelerationChange = onGitHubAccelerationChange,
                onUiModeChange = onUiModeChange,
                onOpenLicenses = onOpenLicenses,
                modifier = modifier,
                scaffoldPadding = scaffoldPadding,
            )
        },
    )
}

@Composable
private fun SettingsPage(
    uiState: SettingsUiState,
    uiMode: UiMode,
    consentToggles: List<ConsentToggle>,
    onCheckForUpdates: () -> Unit,
    onGitHubAccelerationChange: (Boolean) -> Unit,
    onUiModeChange: (UiMode) -> Unit,
    onOpenLicenses: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues,
) {
    val materialOverscrollEffect = rememberOverscrollEffect()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DuckTheme.palette.groupedBackground)
            .overscroll(materialOverscrollEffect),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState(), overscrollEffect = materialOverscrollEffect)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                    val styleItems = listOf(
                        stringResource(R.string.settings_style_miuix),
                        stringResource(R.string.settings_style_material),
                    )
                    SettingsGroup {
                        UiStyleSettingItem(
                            title = stringResource(R.string.settings_ui_style_title),
                            description = stringResource(R.string.settings_ui_style_description),
                            items = styleItems,
                            selectedIndex = if (uiMode == UiMode.Miuix) 0 else 1,
                            onItemSelected = { index ->
                                onUiModeChange(if (index == 0) UiMode.Miuix else UiMode.Material)
                            },
                        )
                    }
                }

                if (consentToggles.isNotEmpty()) {
                    SettingsSection(title = stringResource(R.string.settings_section_detection)) {
                        SettingsGroup {
                            consentToggles.forEachIndexed { index, toggle ->
                                ConsentSettingItem(
                                    toggle = toggle,
                                    index = index,
                                    count = consentToggles.size,
                                )
                            }
                        }
                    }
                }

                SettingsSection(title = stringResource(R.string.settings_section_network)) {
                    SettingsGroup {
                        SettingsSwitchItem(
                            headline = stringResource(R.string.github_acceleration_title),
                            summary = stringResource(R.string.github_acceleration_summary),
                            icon = Icons.Rounded.Speed,
                            checked = uiState.gitHubAccelerationEnabled,
                            onCheckedChange = onGitHubAccelerationChange,
                        )
                    }
                }

                AppLanguageCard()

                AboutSection(
                    uiState = uiState,
                    onCheckForUpdates = onCheckForUpdates,
                    onOpenLicenses = onOpenLicenses,
                )

                ContributorsSection()
                ContributorNameWordmark()
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

private val LanguageOptions = listOf(
    null to R.string.settings_language_system,
    "en" to R.string.settings_language_en,
    "ru" to R.string.settings_language_ru,
    "zh-CN" to R.string.settings_language_zh,
)

@Composable
private fun AppLanguageCard() {
    val current = AppCompatDelegate.getApplicationLocales()
    val currentTag = current.toLanguageTags().takeIf { it.isNotEmpty() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    WrapSafeText(
                        text = stringResource(R.string.settings_language_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    WrapSafeText(
                        text = stringResource(R.string.settings_language_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LanguageOptions.forEach { (tag, labelRes) ->
                    val selected = currentTag == tag
                    FilterChip(
                        selected = selected,
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(
                                if (tag == null) {
                                    LocaleListCompat.getEmptyLocaleList()
                                } else {
                                    LocaleListCompat.forLanguageTags(tag)
                                }
                            )
                        },
                        label = { WrapSafeText(text = stringResource(labelRes)) },
                    )
                }
            }
        }
    }
}
