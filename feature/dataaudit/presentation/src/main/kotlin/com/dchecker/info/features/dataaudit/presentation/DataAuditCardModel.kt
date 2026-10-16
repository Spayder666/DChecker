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

package com.dchecker.info.features.dataaudit.presentation

import com.dchecker.info.core.evidence.DetectorStatus
import com.dchecker.info.core.report.DetectorHeadline
import com.dchecker.info.core.report.DetectorReport
import com.dchecker.info.core.report.ReportBlock
import com.dchecker.info.core.report.ReportRow
import com.dchecker.info.core.evidence.InfoKind
import com.dchecker.info.features.dataaudit.domain.DataAuditGroup
import com.dchecker.info.features.dataaudit.domain.DataAuditReport
import com.dchecker.info.features.dataaudit.domain.DataAuditStage
import com.dchecker.info.features.dataaudit.domain.DataPoint
import com.dchecker.info.features.dataaudit.domain.DataVerdict

public data class DataAuditCardModel(
    override public val title: String,
    public val subtitle: String,
    override public val status: DetectorStatus,
    override public val verdict: String,
    override public val summary: String,
    public val groupRows: List<DataAuditDetailRow>,
    public val scanRows: List<DataAuditDetailRow>,
    public val impactItems: List<String>,
    public val points: List<DataPoint>,
) : DetectorHeadline {
    public val consistentCount: Int
        get() = scanRows.firstOrNull { it.label == "Consistent" }?.value?.toIntOrNull() ?: 0

    public val sealedViolatedCount: Int
        get() = scanRows.firstOrNull { it.label == "Sealed violated" }?.value?.toIntOrNull() ?: 0
}

public fun DataAuditReport.toCardModel(): DataAuditCardModel {
    val groupRows = groupSummaries.map { summary ->
        DataAuditDetailRow(
            label = groupLabel(summary.group),
            value = groupSummaryValue(summary),
            status = when (summary.worst) {
                DataVerdict.SEALED_VIOLATED -> DetectorStatus.danger()
                DataVerdict.MISMATCH -> DetectorStatus.warning()
                else -> DetectorStatus.allClear()
            },
            detail = "consistent ${summary.consistent} · single ${summary.singleSource} · " +
                    "sealed ${summary.sealed} · violated ${summary.sealedViolated}",
        )
    }

    val scanRows = when (stage) {
        DataAuditStage.LOADING -> listOf(
            DataAuditDetailRow("Points checked", "Pending", DetectorStatus.info(InfoKind.SUPPORT)),
        )

        DataAuditStage.FAILED -> listOf(
            DataAuditDetailRow("Points checked", "Failed", DetectorStatus.info(InfoKind.ERROR)),
        )

        DataAuditStage.READY -> listOf(
            DataAuditDetailRow(
                label = "Points checked",
                value = checkedCount.toString(),
                status = DetectorStatus.info(InfoKind.SUPPORT),
                detail = "Each point is read through every vantage the sandbox allows.",
            ),
            DataAuditDetailRow(
                label = "Consistent",
                value = consistentCount.toString(),
                status = if (consistentCount > 0) DetectorStatus.allClear() else DetectorStatus.info(InfoKind.SUPPORT),
            ),
            DataAuditDetailRow(
                label = "Mismatched",
                value = mismatchCount.toString(),
                status = if (mismatchCount > 0) DetectorStatus.warning() else DetectorStatus.allClear(),
                detail = "Lenses disagree: something rewrote at least one reading surface.",
            ),
            DataAuditDetailRow(
                label = "Sealed surfaces",
                value = sealedCount.toString(),
                status = if (sealedCount > 0) DetectorStatus.allClear() else DetectorStatus.info(InfoKind.SUPPORT),
                detail = "Refusals are the conformant answer for these.",
            ),
            DataAuditDetailRow(
                label = "Sealed violated",
                value = sealedViolatedCount.toString(),
                status = if (sealedViolatedCount > 0) DetectorStatus.danger() else DetectorStatus.allClear(),
                detail = "A sealed surface returned its content: the sandbox leaks it.",
            ),
            DataAuditDetailRow(
                label = "Single source",
                value = singleSourceCount.toString(),
                status = DetectorStatus.info(InfoKind.SUPPORT),
                detail = "Only one lens could read these; they cannot be cross-checked.",
            ),
        )
    }

    val impact = when (stage) {
        DataAuditStage.READY -> buildList {
            if (sealedViolatedCount > 0) {
                add("Sealed surfaces answered with content: identifiers or kernel state this " +
                        "sandbox should not expose reached the app, so privacy boundaries are weaker " +
                        "than the OS intends.")
            }
            if (mismatchCount > 0) {
                add("Different vantage points returned different values for the same data point, " +
                        "which means a hook or a spoofing layer rewrites one of the reading surfaces.")
            }
            if (isEmpty()) {
                add("Identifiers, telephony and network surfaces agree across lenses, and every " +
                        "sealed surface stayed closed.")
            }
            add("Refused reads are kept as evidence: a refusal without a permission grant is the " +
                    "conformant answer, and one with a grant can flag selective rewriting.")
        }

        DataAuditStage.LOADING -> listOf("Reading identifiers, telephony, network, and sealed surfaces.")
        DataAuditStage.FAILED -> listOf(errorMessage ?: "The data audit failed to run.")
    }

    return DataAuditCardModel(
        title = "Data & Identity Audit",
        subtitle = buildSubtitle(),
        status = when (stage) {
            DataAuditStage.LOADING -> DetectorStatus.info(InfoKind.SUPPORT)
            DataAuditStage.FAILED -> DetectorStatus.info(InfoKind.ERROR)
            DataAuditStage.READY -> when {
                sealedViolatedCount > 0 -> DetectorStatus.danger()
                mismatchCount > 0 -> DetectorStatus.warning()
                else -> DetectorStatus.allClear()
            }
        },
        verdict = buildVerdict(),
        summary = buildSummary(),
        groupRows = groupRows,
        scanRows = scanRows,
        impactItems = impact,
        points = points,
    )
}

private fun DataAuditReport.buildSubtitle(): String = when (stage) {
    DataAuditStage.LOADING -> "identifiers · telephony · network · sealed surfaces"
    DataAuditStage.FAILED -> "data audit failed"
    DataAuditStage.READY ->
        "$checkedCount points · $consistentCount consistent · $mismatchCount mismatched · " +
                "$sealedViolatedCount sealed violated"
}

private fun DataAuditReport.buildVerdict(): String = when (stage) {
    DataAuditStage.LOADING -> "Reading identity and sealed surfaces through every lens"
    DataAuditStage.FAILED -> "Data & identity audit failed"
    DataAuditStage.READY -> when {
        sealedViolatedCount > 0 -> "$sealedViolatedCount sealed surface(s) leak their content"
        mismatchCount > 0 -> "$mismatchCount data point(s) disagree across lenses"
        else -> "Every data point reads consistently across lenses"
    }
}

private fun DataAuditReport.buildSummary(): String = when (stage) {
    DataAuditStage.LOADING ->
        "Device identifiers, telephony, network surfaces, advertising keys, and sealed kernel " +
                "surfaces are being read through every vantage the sandbox allows."

    DataAuditStage.FAILED -> errorMessage ?: "The data audit failed before evidence could be collected."

    DataAuditStage.READY -> when {
        sealedViolatedCount > 0 ->
            "At least one surface the sandbox must keep closed returned its content. Whatever " +
                    "read it can read what other apps and the OS intend to hide."

        mismatchCount > 0 ->
            "Some data points return different values depending on the reading surface. A hook, " +
                    "a spoofing module, or selective hiding is rewriting one of the lenses."

        else ->
            "All readable data points agree across lenses and every sealed surface refused, " +
                    "which is the conformant answer."
    }
}

private fun groupLabel(group: DataAuditGroup): String = when (group) {
    DataAuditGroup.IDENTITY -> "Identity"
    DataAuditGroup.TELEPHONY -> "Telephony"
    DataAuditGroup.NETWORK -> "Network"
    DataAuditGroup.ADVERTISING -> "Advertising"
    DataAuditGroup.PACKAGE -> "Packages"
    DataAuditGroup.SYSTEM_ENV -> "System & environment"
    DataAuditGroup.SANDBOX -> "Sealed surfaces"
}

private fun groupSummaryValue(
    summary: com.dchecker.info.features.dataaudit.domain.DataAuditGroupSummary,
): String = when (summary.worst) {
    DataVerdict.SEALED_VIOLATED -> "${summary.sealedViolated} leaked"
    DataVerdict.MISMATCH -> "${summary.mismatch} mismatched"
    else -> "Consistent"
}

public fun verdictStatus(verdict: DataVerdict): DetectorStatus = when (verdict) {
    DataVerdict.SEALED_VIOLATED, DataVerdict.MISMATCH -> DetectorStatus.danger()
    DataVerdict.SINGLE_SOURCE, DataVerdict.UNAVAILABLE -> DetectorStatus.info(InfoKind.SUPPORT)
    DataVerdict.SEALED, DataVerdict.CONSISTENT -> DetectorStatus.allClear()
}

public fun DataAuditCardModel.toDetectorReport(): DetectorReport = DetectorReport(
    title = title,
    verdict = verdict,
    severity = status.severity,
    quickFacts = groupRows.map { com.dchecker.info.core.report.ReportFact(it.label, it.value) },
    blocks = listOf(
        ReportBlock.Rows("Groups", groupRows.map { ReportRow(it.label, it.value, it.detail) }),
        ReportBlock.Rows("Scan", scanRows.map { ReportRow(it.label, it.value, it.detail) }),
    ),
)

public data class DataAuditDetailRow(
    public val label: String,
    public val value: String,
    public val status: DetectorStatus,
    public val detail: String? = null,
)
