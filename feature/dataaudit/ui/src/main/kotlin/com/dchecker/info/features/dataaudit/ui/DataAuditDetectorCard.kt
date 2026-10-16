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

package com.dchecker.info.features.dataaudit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dchecker.info.core.evidence.DetectorStatus
import com.dchecker.info.core.evidence.InfoKind
import com.dchecker.info.core.ui.components.DetectorCardFrame
import com.dchecker.info.core.ui.detector.CardDetectorFeature
import com.dchecker.info.core.ui.components.DetectorDetailRowBlock
import com.dchecker.info.core.ui.components.DetectorHairline
import com.dchecker.info.core.ui.components.DetectorSectionFrame
import com.dchecker.info.core.ui.components.DuckIcon
import com.dchecker.info.core.ui.components.WrapSafeText
import com.dchecker.info.features.dataaudit.detector.DataAuditDetector
import com.dchecker.info.core.ui.components.highestSectionSeverity
import com.dchecker.info.features.dataaudit.presentation.DataAuditDetailRow
import com.dchecker.info.features.dataaudit.presentation.DataAuditCardModel
import com.dchecker.info.features.dataaudit.domain.DataVerdict

private fun verdictStatus(verdict: DataVerdict): DetectorStatus = when (verdict) {
    DataVerdict.SEALED_VIOLATED, DataVerdict.MISMATCH -> DetectorStatus.danger()
    DataVerdict.SINGLE_SOURCE, DataVerdict.UNAVAILABLE -> DetectorStatus.info(InfoKind.SUPPORT)
    DataVerdict.SEALED, DataVerdict.CONSISTENT -> DetectorStatus.allClear()
}

/** The dashboard card of the data and identity audit. */
public val detectorFeature: com.dchecker.info.core.ui.detector.DetectorFeature =
    CardDetectorFeature(DataAuditDetector) { model ->
        DataAuditDetectorCard(model = model)
    }

@Composable
public fun DataAuditDetectorCard(
    model: DataAuditCardModel,
    modifier: Modifier = Modifier,
) {
    var showPoints by rememberSaveable { mutableStateOf(false) }

    if (showPoints) {
        DataAuditPointsDialog(
            points = model.points,
            onDismiss = { showPoints = false },
        )
    }

    DetectorCardFrame(
        title = model.title,
        subtitle = model.subtitle,
        status = model.status,
        verdict = model.verdict,
        summary = model.summary,
        leadingIcon = Icons.Rounded.Fingerprint,
        modifier = modifier,
    ) {
        DetectorSectionFrame(
            title = stringResource(R.string.dataaudit_section_groups),
            icon = Icons.Rounded.Shield,
            severity = highestSectionSeverity(model.groupRows.map { it.status }),
            showDivider = true,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                model.groupRows.forEachIndexed { index, row ->
                    DataAuditRow(row = row)
                    if (index < model.groupRows.lastIndex) {
                        DetectorHairline()
                    }
                }
            }
        }

        DetectorSectionFrame(
            title = stringResource(R.string.dataaudit_section_scan),
            icon = Icons.Rounded.Info,
            severity = highestSectionSeverity(model.scanRows.map { it.status }),
            showDivider = true,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                model.scanRows.forEachIndexed { index, row ->
                    DataAuditRow(row = row)
                    if (index < model.scanRows.lastIndex) {
                        DetectorHairline()
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DuckIcon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            WrapSafeText(
                text = stringResource(R.string.dataaudit_view_points, model.points.size),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = { showPoints = true }) {
                WrapSafeText(
                    text = stringResource(R.string.dataaudit_open),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun DataAuditRow(
    row: DataAuditDetailRow,
) {
    val status = when {
        row.value.contains("leak", ignoreCase = true) -> DetectorStatus.danger()
        row.value.contains("mismatch", ignoreCase = true) -> DetectorStatus.warning()
        row.value.equals("Consistent", ignoreCase = true) -> DetectorStatus.allClear()
        else -> DetectorStatus.info(InfoKind.SUPPORT)
    }
    DetectorDetailRowBlock(
        label = row.label,
        value = row.value,
        status = status,
        detail = row.detail,
        detailMonospace = false,
    )
}
