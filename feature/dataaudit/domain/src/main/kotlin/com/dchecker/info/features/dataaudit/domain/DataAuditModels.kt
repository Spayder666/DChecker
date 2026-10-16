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

package com.dchecker.info.features.dataaudit.domain

/**
 * The vantage point one reading of a data point came through. Two lenses disagree only when
 * something between the reader and the source rewrites one of them, so every probe reads its point
 * through as many of these as the sandbox allows.
 */
public enum class AuditLens {
    /** The official SDK path, e.g. TelephonyManager or Settings.Secure. */
    SDK_API,

    /** A second SDK path: a legacy getter, another provider, or another manager. */
    SDK_ALT,

    /** A shell process run through ProcessBuilder: settings, ip, getprop, dumpsys, pm, uname. */
    SHELL,

    /** A direct file read of a kernel or sysfs surface. */
    KERNEL_FILE,
}

public enum class LensState {
    /** The lens produced a value. */
    VALUE,

    /** The sandbox or a permission refused the read; the state itself is evidence. */
    REFUSED,

    /** The source exists but holds nothing for this point. */
    ABSENT,
}

public data class LensReading(
    val lens: AuditLens,
    val state: LensState,
    val value: String = "",
)

public enum class DataAuditGroup {
    IDENTITY,
    TELEPHONY,
    NETWORK,
    ADVERTISING,
    PACKAGE,
    SYSTEM_ENV,
    SANDBOX,
}

public enum class DataVerdict {
    /** Two or more lenses returned the same value. */
    CONSISTENT,

    /** Populated lenses disagree: something rewrote at least one surface. */
    MISMATCH,

    /** Only one lens could read the point, so it cannot be cross-checked. */
    SINGLE_SOURCE,

    /** A surface the sandbox is meant to close: every lens was refused, as it should be. */
    SEALED,

    /** A sealed surface handed its content over: the sandbox leaks it. */
    SEALED_VIOLATED,

    /** Nothing was readable and the point is not sealed. */
    UNAVAILABLE,
}

public data class DataPoint(
    val point: String,
    val group: DataAuditGroup,
    val verdict: DataVerdict,
    val readings: List<LensReading>,
    val note: String = "",
)

public enum class DataAuditStage {
    LOADING,
    READY,
    FAILED,
}

public data class DataAuditGroupSummary(
    val group: DataAuditGroup,
    val total: Int,
    val consistent: Int,
    val mismatch: Int,
    val singleSource: Int,
    val sealed: Int,
    val sealedViolated: Int,
) {
    val worst: DataVerdict
        get() = when {
            sealedViolated > 0 -> DataVerdict.SEALED_VIOLATED
            mismatch > 0 -> DataVerdict.MISMATCH
            else -> DataVerdict.CONSISTENT
        }
}

public data class DataAuditReport(
    val stage: DataAuditStage,
    val points: List<DataPoint>,
    val groupSummaries: List<DataAuditGroupSummary>,
    val checkedCount: Int,
    val consistentCount: Int,
    val mismatchCount: Int,
    val singleSourceCount: Int,
    val sealedCount: Int,
    val sealedViolatedCount: Int,
    val errorMessage: String? = null,
) {
    public companion object {
        public fun loading(): DataAuditReport = DataAuditReport(
            stage = DataAuditStage.LOADING,
            points = emptyList(),
            groupSummaries = emptyList(),
            checkedCount = 0,
            consistentCount = 0,
            mismatchCount = 0,
            singleSourceCount = 0,
            sealedCount = 0,
            sealedViolatedCount = 0,
        )

        public fun failed(message: String): DataAuditReport = DataAuditReport(
            stage = DataAuditStage.FAILED,
            points = emptyList(),
            groupSummaries = emptyList(),
            checkedCount = 0,
            consistentCount = 0,
            mismatchCount = 0,
            singleSourceCount = 0,
            sealedCount = 0,
            sealedViolatedCount = 0,
            errorMessage = message,
        )

        /** Judges one point from its readings; the single place that decides a verdict. */
        public fun judge(
            point: String,
            group: DataAuditGroup,
            sealed: Boolean = false,
            note: String = "",
            readings: List<LensReading>,
        ): DataPoint {
            val populated = readings.filter { it.state == LensState.VALUE && it.value.isNotBlank() }
            val verdict = when {
                sealed -> if (populated.isEmpty()) DataVerdict.SEALED else DataVerdict.SEALED_VIOLATED
                populated.isEmpty() -> DataVerdict.UNAVAILABLE
                populated.size == 1 -> DataVerdict.SINGLE_SOURCE
                populated.map { it.value.trim().lowercase() }.distinct().size == 1 ->
                    DataVerdict.CONSISTENT

                else -> DataVerdict.MISMATCH
            }
            return DataPoint(
                point = point,
                group = group,
                verdict = verdict,
                readings = readings,
                note = note,
            )
        }
    }
}
