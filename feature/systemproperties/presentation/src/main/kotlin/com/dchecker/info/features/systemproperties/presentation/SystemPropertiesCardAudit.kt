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

package com.dchecker.info.features.systemproperties.presentation

import com.dchecker.info.capability.systemproperties.domain.SystemPropertySeverity
import com.dchecker.info.core.evidence.DetectorStatus
import com.dchecker.info.core.evidence.InfoKind
import com.dchecker.info.features.systemproperties.domain.FullPropertyAudit
import com.dchecker.info.features.systemproperties.domain.PropertyAuditMethod
import com.dchecker.info.features.systemproperties.domain.PropertyDivergenceKind
import com.dchecker.info.features.systemproperties.domain.SystemPropertiesReport
import com.dchecker.info.features.systemproperties.domain.SystemPropertiesStage
import com.dchecker.info.features.systemproperties.presentation.model.SystemPropertiesDetailRowModel
import com.dchecker.info.features.systemproperties.presentation.model.SystemPropertiesMismatchMethodModel
import com.dchecker.info.features.systemproperties.presentation.model.SystemPropertiesMismatchModel

internal fun buildAuditRows(report: SystemPropertiesReport): List<SystemPropertiesDetailRowModel> {
    val audit = report.fullAudit
    return when (report.stage) {
        SystemPropertiesStage.LOADING -> listOf(
            SystemPropertiesDetailRowModel(
                label = "Full property audit",
                value = "Pending",
                status = DetectorStatus.info(InfoKind.SUPPORT),
            ),
        )

        SystemPropertiesStage.FAILED -> listOf(
            SystemPropertiesDetailRowModel(
                label = "Full property audit",
                value = "Failed",
                status = DetectorStatus.info(InfoKind.ERROR),
            ),
        )

        SystemPropertiesStage.READY -> if (audit.checkedCount == 0) {
            listOf(
                SystemPropertiesDetailRowModel(
                    label = "Full property audit",
                    value = "Unavailable",
                    status = DetectorStatus.info(InfoKind.SUPPORT),
                    detail = "Native property enumeration and shell getprop were both unavailable.",
                ),
            )
        } else {
            buildList {
                add(
                    SystemPropertiesDetailRowModel(
                        label = "Properties checked",
                        value = audit.checkedCount.toString(),
                        status = DetectorStatus.info(InfoKind.SUPPORT),
                        detail = "Union of native foreach keys, shell getprop keys, and JVM getprop keys.",
                        ),
                )
                add(
                    SystemPropertiesDetailRowModel(
                        label = "Mismatched fields",
                        value = audit.mismatchCount.toString(),
                        status = when {
                            audit.dangerDivergenceCount > 0 -> DetectorStatus.danger()
                            audit.hasDivergences -> DetectorStatus.warning()
                            else -> DetectorStatus.allClear()
                        },
                        detail = "Fields where at least two read methods returned different stable values.",
                    ),
                )
                if (audit.transientCount > 0) {
                    add(
                        SystemPropertiesDetailRowModel(
                            label = "Transient skips",
                            value = audit.transientCount.toString(),
                            status = DetectorStatus.info(InfoKind.SUPPORT),
                            detail = "Values that changed between the two read passes and were not reported.",
                        ),
                    )
                }
                audit.methodCoverage.forEach { (method, count) ->
                    add(
                        SystemPropertiesDetailRowModel(
                            label = auditMethodLabel(method),
                            value = "$count visible",
                            status = if (count > 0) {
                                DetectorStatus.allClear()
                            } else {
                                DetectorStatus.info(InfoKind.SUPPORT)
                            },

                        ),
                    )
                }
            }
        }
    }
}

internal fun buildAuditMismatches(
    report: SystemPropertiesReport,
): List<SystemPropertiesMismatchModel> {
    return report.fullAudit.divergences.map { divergence ->
        val normalizedCounts = divergence.methodValues.values
            .map { it.trim().lowercase() }
            .groupingBy { it }
            .eachCount()
        val majorityValue = normalizedCounts.maxByOrNull { it.value }?.key
        SystemPropertiesMismatchModel(
            property = divergence.property,
            kind = divergence.kind,
            category = com.dchecker.info.capability.systemproperties.domain.SystemPropertyCategory.FULL_AUDIT,
            status = when (divergence.severity) {
                SystemPropertySeverity.DANGER -> DetectorStatus.danger()
                SystemPropertySeverity.WARNING -> DetectorStatus.warning()
                SystemPropertySeverity.NEUTRAL -> DetectorStatus.info(InfoKind.SUPPORT)
                SystemPropertySeverity.SAFE -> DetectorStatus.allClear()
            },
            methods = divergence.methodValues.map { (method, value) ->
                SystemPropertiesMismatchMethodModel(
                    method = method,
                    value = value,
                    divergent = value.trim().lowercase() != majorityValue,
                )
            },
        )
    }
}

public fun auditMethodLabel(
    method: PropertyAuditMethod,
): String {
    return when (method) {
        PropertyAuditMethod.JAVA_REFLECTION -> "Java reflection"
        PropertyAuditMethod.JVM_GETPROP -> "getprop (JVM)"
        PropertyAuditMethod.JVM_PROPERTY -> "System.getProperty"
        PropertyAuditMethod.NATIVE_CALLBACK -> "Native callback"
        PropertyAuditMethod.NATIVE_LEGACY -> "Native legacy get"
        PropertyAuditMethod.NATIVE_SHELL -> "Native shell getprop"
        PropertyAuditMethod.BUILD_CONSTANT -> "Build constant"
    }
}
