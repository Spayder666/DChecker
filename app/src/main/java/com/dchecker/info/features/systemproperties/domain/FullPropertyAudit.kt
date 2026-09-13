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

package com.dchecker.info.features.systemproperties.domain

/**
 * Read vantage points used by the full property audit. Each one reaches the
 * property area through a different code path, so a hooking framework that
 * rewrites one surface leaves the others intact.
 */
enum class PropertyAuditMethod {
    JAVA_REFLECTION,
    JVM_GETPROP,
    JVM_PROPERTY,
    NATIVE_CALLBACK,
    NATIVE_LEGACY,
    NATIVE_SHELL,
    BUILD_CONSTANT,
}

enum class PropertyDivergenceKind {
    JAVA_VS_NATIVE,
    NATIVE_VS_NATIVE,
    SHELL_VS_INLINE,
    FRAMEWORK_VS_PROPERTY,
}

data class PropertyDivergence(
    val property: String,
    val kind: PropertyDivergenceKind,
    val severity: SystemPropertySeverity,
    val methodValues: Map<PropertyAuditMethod, String>,
)

data class FullPropertyAudit(
    val checkedCount: Int = 0,
    val mismatchCount: Int = 0,
    val transientCount: Int = 0,
    val divergences: List<PropertyDivergence> = emptyList(),
    val methodCoverage: Map<PropertyAuditMethod, Int> = emptyMap(),
    val foreachAvailable: Boolean = false,
    val shellAvailable: Boolean = false,
) {
    val dangerDivergenceCount: Int
        get() = divergences.count { it.severity == SystemPropertySeverity.DANGER }

    val hasDivergences: Boolean
        get() = divergences.isNotEmpty()
}
