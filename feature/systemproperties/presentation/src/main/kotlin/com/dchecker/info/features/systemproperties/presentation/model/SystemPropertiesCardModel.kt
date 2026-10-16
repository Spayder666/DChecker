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

package com.dchecker.info.features.systemproperties.presentation.model

import com.dchecker.info.capability.systemproperties.domain.SystemPropertyCategory
import com.dchecker.info.core.evidence.DetectorStatus
import com.dchecker.info.core.report.DetectorHeadline
import com.dchecker.info.features.systemproperties.domain.PropertyAuditMethod
import com.dchecker.info.features.systemproperties.domain.PropertyDivergenceKind

data class SystemPropertiesCardModel(
    override val title: String,
    val subtitle: String,
    override val status: DetectorStatus,
    override val verdict: String,
    override val summary: String,
    val headerFacts: List<SystemPropertiesHeaderFactModel>,
    val coreRows: List<SystemPropertiesDetailRowModel>,
    val bootRows: List<SystemPropertiesDetailRowModel>,
    val buildRows: List<SystemPropertiesDetailRowModel>,
    val sourceRows: List<SystemPropertiesDetailRowModel>,
    val consistencyRows: List<SystemPropertiesDetailRowModel>,
    val infoRows: List<SystemPropertiesDetailRowModel>,
    val impactItems: List<SystemPropertiesImpactItemModel>,
    val methodRows: List<SystemPropertiesDetailRowModel>,
    val scanRows: List<SystemPropertiesDetailRowModel>,
    val auditRows: List<SystemPropertiesDetailRowModel> = emptyList(),
    val auditMismatches: List<SystemPropertiesMismatchModel> = emptyList(),
    val auditAvailable: Boolean = false,
    val auditCheckedCount: Int = 0,
) : DetectorHeadline

/** The facts in the card's header, in the order the export lists them. */
enum class SystemPropertiesHeaderFact(val label: String) {
    CRITICAL("Critical"),
    REVIEW("Review"),
    BOOT("Boot"),
    BUILD("Build"),
}

data class SystemPropertiesHeaderFactModel(
    val fact: SystemPropertiesHeaderFact,
    val value: String,
    val status: DetectorStatus,
) {
    val label: String get() = fact.label
}

data class SystemPropertiesDetailRowModel(
    val label: String,
    val value: String,
    val status: DetectorStatus,
    val detail: String? = null,
    val detailMonospace: Boolean = false,
)

public data class SystemPropertiesMismatchMethodModel(
    val method: PropertyAuditMethod,
    val value: String,
    val divergent: Boolean,
)

public data class SystemPropertiesMismatchModel(
    val property: String,
    val kind: PropertyDivergenceKind,
    val category: SystemPropertyCategory,
    val status: DetectorStatus,
    val methods: List<SystemPropertiesMismatchMethodModel>,
)

data class SystemPropertiesImpactItemModel(
    val text: String,
    val status: DetectorStatus,
)
