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

package com.dchecker.info.features.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.dchecker.info.R
import com.dchecker.info.core.ui.components.WrapSafeText
import com.dchecker.info.features.licenses.ui.OpenSourceLicensesEntry
import com.dchecker.info.features.licenses.ui.OpenSourceLicensesScreen
import com.dchecker.info.features.settings.ui.components.AboutCard
import com.dchecker.info.features.settings.ui.components.AuthorCard
import com.dchecker.info.features.settings.ui.model.SettingsUiState
import com.dchecker.info.ui.theme.ShapeTokens

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onCrlNetworkingChange: (Boolean) -> Unit,
    onCheckForUpdates: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showingLicenses by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (showingLicenses) {
            OpenSourceLicensesScreen(
                onBack = { showingLicenses = false },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    WrapSafeText(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    WrapSafeText(
                        text = stringResource(R.string.settings_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeTokens.CornerExtraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Surface(
                                shape = ShapeTokens.CornerLarge,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.NetworkCheck,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                WrapSafeText(
                                    text = stringResource(R.string.settings_crl_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                WrapSafeText(
                                    text = stringResource(R.string.settings_crl_summary),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Switch(
                                checked = uiState.isCrlNetworkingEnabled,
                                onCheckedChange = onCrlNetworkingChange,
                            )
                        }

                        WrapSafeText(
                            text = stringResource(R.string.settings_crl_footer),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                AppLanguageCard()

                AboutCard(
                    versionName = uiState.versionName,
                    versionCode = uiState.versionCode,
                    buildTimeUtc = uiState.buildTimeUtc,
                    buildHash = uiState.buildHash,
                    updateStatus = uiState.updateStatus,
                    onCheckForUpdates = onCheckForUpdates,
                )

                OpenSourceLicensesEntry(
                    onClick = { showingLicenses = true },
                )

                AuthorCard()

                Spacer(modifier = Modifier.height(72.dp))
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
        shape = ShapeTokens.CornerExtraLarge,
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
                    shape = ShapeTokens.CornerLarge,
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
