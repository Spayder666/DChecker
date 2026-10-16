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

package com.dchecker.info.features.dataaudit.data

import android.content.Context
import com.dchecker.info.core.detector.DetectorScanner
import com.dchecker.info.features.dataaudit.domain.DataAuditGroup
import com.dchecker.info.features.dataaudit.domain.DataAuditGroupSummary
import com.dchecker.info.features.dataaudit.domain.DataAuditReport
import com.dchecker.info.features.dataaudit.domain.DataAuditStage
import com.dchecker.info.features.dataaudit.domain.DataVerdict
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Collects the data and identity audit: every probe reads its point through every vantage the
 * sandbox allows, and the report aggregates the verdicts per group.
 */
public class DataAuditRepository(
    private val context: Context,
) : DetectorScanner<DataAuditReport> {

    override suspend fun scan(): DataAuditReport = withContext(Dispatchers.IO) {
        runCatching { scanInternal() }
            .getOrElse { throwable ->
                DataAuditReport.failed(throwable.message ?: "Data audit scan failed.")
            }
    }

    private fun scanInternal(): DataAuditReport {
        val points = DataAuditProbes(context).collect()
        if (points.isEmpty()) {
            return DataAuditReport.failed("No data audit probes produced any reading.")
        }

        val groupSummaries = DataAuditGroup.entries.map { group ->
            val groupPoints = points.filter { it.group == group }
            DataAuditGroupSummary(
                group = group,
                total = groupPoints.size,
                consistent = groupPoints.count { it.verdict == DataVerdict.CONSISTENT },
                mismatch = groupPoints.count { it.verdict == DataVerdict.MISMATCH },
                singleSource = groupPoints.count { it.verdict == DataVerdict.SINGLE_SOURCE },
                sealed = groupPoints.count { it.verdict == DataVerdict.SEALED },
                sealedViolated = groupPoints.count { it.verdict == DataVerdict.SEALED_VIOLATED },
            )
        }

        return DataAuditReport(
            stage = DataAuditStage.READY,
            points = points,
            groupSummaries = groupSummaries,
            checkedCount = points.size,
            consistentCount = points.count { it.verdict == DataVerdict.CONSISTENT },
            mismatchCount = points.count { it.verdict == DataVerdict.MISMATCH },
            singleSourceCount = points.count { it.verdict == DataVerdict.SINGLE_SOURCE },
            sealedCount = points.count { it.verdict == DataVerdict.SEALED },
            sealedViolatedCount = points.count { it.verdict == DataVerdict.SEALED_VIOLATED },
        )
    }
}
