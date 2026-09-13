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

package com.dchecker.info.features.systemproperties.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dchecker.info.R
import com.dchecker.info.core.ui.components.WrapSafeText
import com.dchecker.info.core.ui.model.DetectionSeverity
import com.dchecker.info.core.ui.presentation.rememberStatusAppearance
import com.dchecker.info.features.systemproperties.domain.PropertyAuditMethod
import com.dchecker.info.features.systemproperties.domain.PropertyDivergenceKind
import com.dchecker.info.features.systemproperties.ui.model.SystemPropertiesMismatchModel
import com.dchecker.info.ui.theme.ShapeTokens

private enum class MismatchFilter {
    ALL,
    DANGER,
    WARNING,
    INFO,
}

@Composable
fun SystemPropertiesMismatchDialog(
    mismatches: List<SystemPropertiesMismatchModel>,
    checkedCount: Int,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(MismatchFilter.ALL) }

    val dangerCount = mismatches.count { it.status.severity == DetectionSeverity.DANGER }
    val warningCount = mismatches.count { it.status.severity == DetectionSeverity.WARNING }
    val infoCount = mismatches.count { it.status.severity != DetectionSeverity.DANGER && it.status.severity != DetectionSeverity.WARNING }

    val visible = remember(mismatches, query, filter) {
        mismatches.filter { mismatch ->
            val matchesQuery = query.isBlank() ||
                    mismatch.property.contains(query, ignoreCase = true) ||
                    mismatch.methods.any { it.value.contains(query, ignoreCase = true) }
            val matchesFilter = when (filter) {
                MismatchFilter.ALL -> true
                MismatchFilter.DANGER -> mismatch.status.severity == DetectionSeverity.DANGER
                MismatchFilter.WARNING -> mismatch.status.severity == DetectionSeverity.WARNING
                MismatchFilter.INFO -> mismatch.status.severity != DetectionSeverity.DANGER &&
                        mismatch.status.severity != DetectionSeverity.WARNING
            }
            matchesQuery && matchesFilter
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp),
            shape = ShapeTokens.CornerExtraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = ShapeTokens.CornerLargeIncreased,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.CompareArrows,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(22.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        WrapSafeText(
                            text = stringResource(R.string.sp_mismatch_dialog_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        WrapSafeText(
                            text = stringResource(R.string.sp_mismatch_dialog_subtitle, checkedCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MismatchMetricChip(
                        icon = Icons.AutoMirrored.Rounded.CompareArrows,
                        label = stringResource(R.string.sp_mismatch_chip_checked),
                        value = checkedCount.toString(),
                    )
                    MismatchMetricChip(
                        icon = Icons.Rounded.BugReport,
                        label = stringResource(R.string.sp_mismatch_chip_mismatches),
                        value = mismatches.size.toString(),
                    )
                    MismatchMetricChip(
                        icon = Icons.Rounded.ErrorOutline,
                        label = stringResource(R.string.sp_mismatch_chip_danger),
                        value = dangerCount.toString(),
                    )
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        WrapSafeText(text = stringResource(R.string.sp_mismatch_search_hint))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    shape = ShapeTokens.CornerLarge,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = filter == MismatchFilter.ALL,
                        onClick = { filter = MismatchFilter.ALL },
                        label = {
                            WrapSafeText(
                                text = stringResource(R.string.sp_mismatch_filter_all, mismatches.size)
                            )
                        },
                    )
                    FilterChip(
                        selected = filter == MismatchFilter.DANGER,
                        onClick = { filter = MismatchFilter.DANGER },
                        label = {
                            WrapSafeText(
                                text = stringResource(R.string.sp_mismatch_filter_danger, dangerCount)
                            )
                        },
                        enabled = dangerCount > 0,
                    )
                    FilterChip(
                        selected = filter == MismatchFilter.WARNING,
                        onClick = { filter = MismatchFilter.WARNING },
                        label = {
                            WrapSafeText(
                                text = stringResource(R.string.sp_mismatch_filter_warning, warningCount)
                            )
                        },
                        enabled = warningCount > 0,
                    )
                    FilterChip(
                        selected = filter == MismatchFilter.INFO,
                        onClick = { filter = MismatchFilter.INFO },
                        label = {
                            WrapSafeText(
                                text = stringResource(R.string.sp_mismatch_filter_info, infoCount)
                            )
                        },
                        enabled = infoCount > 0,
                    )
                }

                if (visible.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = ShapeTokens.CornerExtraLarge,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = if (mismatches.isEmpty()) {
                                    Icons.Rounded.Verified
                                } else {
                                    Icons.Rounded.Info
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            WrapSafeText(
                                text = if (mismatches.isEmpty()) {
                                    stringResource(R.string.sp_mismatch_empty_none)
                                } else {
                                    stringResource(R.string.sp_mismatch_empty_filter)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(visible) { mismatch ->
                            MismatchItem(mismatch = mismatch)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        WrapSafeText(
                            text = stringResource(R.string.sp_mismatch_close),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun divergenceKindLabel(
    kind: PropertyDivergenceKind,
): String {
    return when (kind) {
        PropertyDivergenceKind.JAVA_VS_NATIVE -> stringResource(R.string.sp_audit_kind_java_native)
        PropertyDivergenceKind.NATIVE_VS_NATIVE -> stringResource(R.string.sp_audit_kind_native_native)
        PropertyDivergenceKind.SHELL_VS_INLINE -> stringResource(R.string.sp_audit_kind_shell_inline)
        PropertyDivergenceKind.FRAMEWORK_VS_PROPERTY -> stringResource(R.string.sp_audit_kind_framework_property)
    }
}

@Composable
private fun auditMethodLabel(
    method: PropertyAuditMethod,
): String {
    return when (method) {
        PropertyAuditMethod.JAVA_REFLECTION -> stringResource(R.string.sp_audit_method_reflection)
        PropertyAuditMethod.JVM_GETPROP -> stringResource(R.string.sp_audit_method_jvm_getprop)
        PropertyAuditMethod.JVM_PROPERTY -> stringResource(R.string.sp_audit_method_jvm_property)
        PropertyAuditMethod.NATIVE_CALLBACK -> stringResource(R.string.sp_audit_method_native_callback)
        PropertyAuditMethod.NATIVE_LEGACY -> stringResource(R.string.sp_audit_method_native_legacy)
        PropertyAuditMethod.NATIVE_SHELL -> stringResource(R.string.sp_audit_method_native_shell)
        PropertyAuditMethod.BUILD_CONSTANT -> stringResource(R.string.sp_audit_method_build_constant)
    }
}

@Composable
private fun MismatchItem(
    mismatch: SystemPropertiesMismatchModel,
) {
    val appearance = rememberStatusAppearance(mismatch.status)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = ShapeTokens.CornerExtraLarge,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = appearance.icon,
                    contentDescription = null,
                    tint = appearance.iconTint,
                    modifier = Modifier.size(16.dp),
                )
                WrapSafeText(
                    text = mismatch.property,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = ShapeTokens.CornerLarge,
                ) {
                    WrapSafeText(
                        text = divergenceKindLabel(mismatch.kind),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            mismatch.methods.forEach { method ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    WrapSafeText(
                        text = auditMethodLabel(method.method),
                        modifier = Modifier.fillMaxWidth(0.32f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    WrapSafeText(
                        text = method.value,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = if (method.divergent) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MismatchMetricChip(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = ShapeTokens.CornerLarge,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                WrapSafeText(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                WrapSafeText(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
