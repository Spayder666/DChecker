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

package com.eltavine.duckdetector.features.systemproperties.data.probes

import com.eltavine.duckdetector.features.systemproperties.data.native.FullPropertyNativeEntry
import com.eltavine.duckdetector.features.systemproperties.data.native.FullPropertyNativeSnapshot
import com.eltavine.duckdetector.features.systemproperties.data.utils.PropertyReadAccess
import com.eltavine.duckdetector.features.systemproperties.domain.FullPropertyAudit
import com.eltavine.duckdetector.features.systemproperties.domain.PropertyAuditMethod
import com.eltavine.duckdetector.features.systemproperties.domain.PropertyDivergence
import com.eltavine.duckdetector.features.systemproperties.domain.PropertyDivergenceKind
import com.eltavine.duckdetector.features.systemproperties.domain.SystemPropertySeverity

/**
 * Framework constants captured at scan time. Kept as a plain data class so the
 * comparator stays unit-testable without android.os.Build on the JVM.
 */
data class AuditBuildConstants(
    val model: String,
    val brand: String,
    val device: String,
    val manufacturer: String,
    val product: String,
    val board: String,
    val bootloader: String,
    val hardware: String,
    val fingerprint: String,
    val id: String,
    val display: String,
    val versionRelease: String,
    val versionSdk: String,
    val versionIncremental: String,
    val versionSecurityPatch: String,
    val buildTimeMillis: Long,
    val propertySourceOrder: String?,
) {
    companion object {
        fun fromBuild(): AuditBuildConstants {
            return AuditBuildConstants(
                model = android.os.Build.MODEL.orEmpty(),
                brand = android.os.Build.BRAND.orEmpty(),
                device = android.os.Build.DEVICE.orEmpty(),
                manufacturer = android.os.Build.MANUFACTURER.orEmpty(),
                product = android.os.Build.PRODUCT.orEmpty(),
                board = android.os.Build.BOARD.orEmpty(),
                bootloader = android.os.Build.BOOTLOADER.orEmpty(),
                hardware = android.os.Build.HARDWARE.orEmpty(),
                fingerprint = android.os.Build.FINGERPRINT.orEmpty(),
                id = android.os.Build.ID.orEmpty(),
                display = android.os.Build.DISPLAY.orEmpty(),
                versionRelease = android.os.Build.VERSION.RELEASE.orEmpty(),
                versionSdk = android.os.Build.VERSION.SDK_INT.toString(),
                versionIncremental = android.os.Build.VERSION.INCREMENTAL.orEmpty(),
                versionSecurityPatch = android.os.Build.VERSION.SECURITY_PATCH.orEmpty(),
                buildTimeMillis = android.os.Build.TIME,
                propertySourceOrder = null,
            )
        }
    }
}

/**
 * Compares every system property across all read vantage points (Java
 * reflection, JVM getprop dump, System.getProperty, native callback read,
 * legacy 92-byte native read, native shell getprop) and reports fields where
 * the methods disagree. A stable disagreement means some layer between the
 * reader and the property area rewrites that surface.
 *
 * Volatile properties legitimately change between reads, so divergences are
 * confirmed by a second inline read pass before they are reported, and known
 * volatile prefixes are downgraded to informational severity.
 */
class FullPropertyAuditComparator(
    private val readAccess: PropertyReadAccess,
) {

    fun audit(
        nativeSnapshot: FullPropertyNativeSnapshot,
        getpropSnapshot: Map<String, String>,
        buildConstants: AuditBuildConstants,
    ): FullPropertyAudit {
        val keys = (nativeSnapshot.entries.keys.asSequence() +
                nativeSnapshot.shellProperties.keys.asSequence() +
                getpropSnapshot.keys.asSequence()).toSortedSet()

        val coverage = LinkedHashMap<PropertyAuditMethod, Int>()
        val divergences = mutableListOf<PropertyDivergence>()
        var transientCount = 0

        val candidates = mutableListOf<Pair<String, Map<PropertyAuditMethod, String>>>()
        for (key in keys) {
            val methodValues = collectFirstPassValues(key, nativeSnapshot, getpropSnapshot)
            methodValues.forEach { (method, value) ->
                if (value.isNotBlank()) {
                    coverage[method] = (coverage[method] ?: 0) + 1
                }
            }
            val populated = methodValues.filterValues { it.isNotBlank() }
            if (populated.isEmpty() || distinctNormalized(key, populated).size <= 1) {
                continue
            }
            candidates += key to populated
        }

        for ((key, populated) in candidates) {
            if (!isStableMismatch(key, populated)) {
                transientCount++
                continue
            }
            divergences += buildDivergence(key, populated)
        }

        divergences += buildFrameworkDivergences(
            buildConstants = buildConstants,
            nativeSnapshot = nativeSnapshot,
            getpropSnapshot = getpropSnapshot,
        )

        divergences.sortWith(
            compareBy(
                { severityRank(it.severity) },
                { it.property },
            )
        )

        return FullPropertyAudit(
            checkedCount = keys.size,
            mismatchCount = divergences.size,
            transientCount = transientCount,
            divergences = divergences,
            methodCoverage = coverage,
            foreachAvailable = nativeSnapshot.foreachAvailable,
            shellAvailable = nativeSnapshot.shellAvailable,
        )
    }

    private fun collectFirstPassValues(
        key: String,
        nativeSnapshot: FullPropertyNativeSnapshot,
        getpropSnapshot: Map<String, String>,
    ): Map<PropertyAuditMethod, String> {
        val values = LinkedHashMap<PropertyAuditMethod, String>()
        values[PropertyAuditMethod.JAVA_REFLECTION] = readAccess.readReflectionValue(key)
        values[PropertyAuditMethod.JVM_GETPROP] = getpropSnapshot[key].orEmpty().trim()
        values[PropertyAuditMethod.JVM_PROPERTY] = readAccess.readJvmValue(key)
        values[PropertyAuditMethod.NATIVE_CALLBACK] =
            sanitizeNativeValue(nativeSnapshot.callbackValue(key))
        values[PropertyAuditMethod.NATIVE_LEGACY] =
            sanitizeNativeValue(nativeSnapshot.legacyValue(key))
        values[PropertyAuditMethod.NATIVE_SHELL] =
            nativeSnapshot.shellProperties[key].orEmpty().trim()
        return values
    }

    private fun isStableMismatch(
        key: String,
        firstPass: Map<PropertyAuditMethod, String>,
    ): Boolean {
        val hasInline = firstPass.containsKey(PropertyAuditMethod.JAVA_REFLECTION) ||
                firstPass.containsKey(PropertyAuditMethod.JVM_PROPERTY) ||
                firstPass.containsKey(PropertyAuditMethod.NATIVE_CALLBACK) ||
                firstPass.containsKey(PropertyAuditMethod.NATIVE_LEGACY)
        if (!hasInline) {
            // Only shell dumps disagree; there is no cheap inline re-read for
            // them, so report the disagreement and let severity speak.
            return true
        }

        val secondInline = readAccess.readInlineNativeEntry(key)
        val secondPass = LinkedHashMap<PropertyAuditMethod, String>()
        secondPass[PropertyAuditMethod.JAVA_REFLECTION] = readAccess.readReflectionValue(key)
        secondPass[PropertyAuditMethod.JVM_PROPERTY] = readAccess.readJvmValue(key)
        secondPass[PropertyAuditMethod.NATIVE_CALLBACK] =
            sanitizeNativeValue(secondInline?.callbackValue.orEmpty())
        secondPass[PropertyAuditMethod.NATIVE_LEGACY] =
            sanitizeNativeValue(secondInline?.legacyValue.orEmpty())

        val confirmedPopulated = secondPass.filterValues { it.isNotBlank() }
        // A disagreement that vanished between passes is a legitimately
        // changing value, not evidence of tampering.
        return !(confirmedPopulated.isEmpty() ||
                distinctNormalized(key, confirmedPopulated).size <= 1)
    }

    private fun buildDivergence(
        key: String,
        populated: Map<PropertyAuditMethod, String>,
    ): PropertyDivergence {
        val hasJava = populated.containsKey(PropertyAuditMethod.JAVA_REFLECTION) ||
                populated.containsKey(PropertyAuditMethod.JVM_PROPERTY)
        val hasNativeInline = populated.containsKey(PropertyAuditMethod.NATIVE_CALLBACK) ||
                populated.containsKey(PropertyAuditMethod.NATIVE_LEGACY)

        val kind = when {
            hasJava && hasNativeInline -> PropertyDivergenceKind.JAVA_VS_NATIVE
            populated.containsKey(PropertyAuditMethod.NATIVE_CALLBACK) &&
                    populated.containsKey(PropertyAuditMethod.NATIVE_LEGACY) ->
                PropertyDivergenceKind.NATIVE_VS_NATIVE

            else -> PropertyDivergenceKind.SHELL_VS_INLINE
        }
        return PropertyDivergence(
            property = key,
            kind = kind,
            severity = severityFor(key, kind),
            methodValues = populated,
        )
    }

    private fun severityFor(
        key: String,
        kind: PropertyDivergenceKind,
    ): SystemPropertySeverity {
        if (isVolatileProperty(key)) {
            return SystemPropertySeverity.NEUTRAL
        }
        return if (kind == PropertyDivergenceKind.JAVA_VS_NATIVE && key.startsWith("ro.")) {
            SystemPropertySeverity.DANGER
        } else {
            SystemPropertySeverity.WARNING
        }
    }

    private fun buildFrameworkDivergences(
        buildConstants: AuditBuildConstants,
        nativeSnapshot: FullPropertyNativeSnapshot,
        getpropSnapshot: Map<String, String>,
    ): List<PropertyDivergence> {
        val findings = mutableListOf<PropertyDivergence>()

        val productFields = listOf(
            "Build.MODEL" to (buildConstants.model to "model"),
            "Build.BRAND" to (buildConstants.brand to "brand"),
            "Build.DEVICE" to (buildConstants.device to "device"),
            "Build.MANUFACTURER" to (buildConstants.manufacturer to "manufacturer"),
            "Build.PRODUCT" to (buildConstants.product to "name"),
        )
        val sourceOrder = resolvePropertySourceOrder(
            buildConstants,
            nativeSnapshot,
            getpropSnapshot,
        )
        for ((label, valueWithField) in productFields) {
            val (buildValue, field) = valueWithField
            if (buildValue.isBlank()) {
                continue
            }
            val resolved = resolveProductProperty(field, sourceOrder) { key ->
                preferredPropertyValue(key, nativeSnapshot, getpropSnapshot)
            }
            if (resolved == null || resolved.second.isBlank()) {
                continue
            }
            if (normalizeValue(buildValue) == normalizeValue(resolved.second)) {
                continue
            }
            findings += frameworkDivergence(
                label = label,
                buildValue = buildValue,
                property = resolved.first,
                propertyValue = resolved.second,
                nativeSnapshot = nativeSnapshot,
                getpropSnapshot = getpropSnapshot,
            )
        }

        val directFields = listOf(
            "Build.BOARD" to (buildConstants.board to "ro.product.board"),
            "Build.BOOTLOADER" to (buildConstants.bootloader to "ro.bootloader"),
            "Build.HARDWARE" to (buildConstants.hardware to "ro.hardware"),
            "Build.ID" to (buildConstants.id to "ro.build.id"),
            "Build.DISPLAY" to (buildConstants.display to "ro.build.display.id"),
            "Build.VERSION.RELEASE" to (buildConstants.versionRelease to "ro.build.version.release"),
            "Build.VERSION.SDK" to (buildConstants.versionSdk to "ro.build.version.sdk"),
            "Build.VERSION.INCREMENTAL" to (buildConstants.versionIncremental to "ro.build.version.incremental"),
            "Build.VERSION.SECURITY_PATCH" to (
                    buildConstants.versionSecurityPatch to "ro.build.version.security_patch"
                    ),
        )
        for ((label, valueWithProperty) in directFields) {
            val (buildValue, property) = valueWithProperty
            if (buildValue.isBlank()) {
                continue
            }
            val propertyValue = preferredPropertyValue(property, nativeSnapshot, getpropSnapshot)
            if (propertyValue.isBlank() || normalizeValue(buildValue) == normalizeValue(propertyValue)) {
                continue
            }
            findings += frameworkDivergence(
                label = label,
                buildValue = buildValue,
                property = property,
                propertyValue = propertyValue,
                nativeSnapshot = nativeSnapshot,
                getpropSnapshot = getpropSnapshot,
            )
        }

        // ro.build.date.utc is seconds while Build.TIME is milliseconds.
        val utcSeconds = preferredPropertyValue(
            "ro.build.date.utc",
            nativeSnapshot,
            getpropSnapshot,
        ).toLongOrNull()
        if (utcSeconds != null && buildConstants.buildTimeMillis > 0 &&
            kotlin.math.abs(buildConstants.buildTimeMillis - utcSeconds * 1000) > 1000
        ) {
            findings += frameworkDivergence(
                label = "Build.TIME",
                buildValue = buildConstants.buildTimeMillis.toString(),
                property = "ro.build.date.utc",
                propertyValue = utcSeconds.toString(),
                nativeSnapshot = nativeSnapshot,
                getpropSnapshot = getpropSnapshot,
            )
        }

        return findings
    }

    private fun frameworkDivergence(
        label: String,
        buildValue: String,
        property: String,
        propertyValue: String,
        nativeSnapshot: FullPropertyNativeSnapshot,
        getpropSnapshot: Map<String, String>,
    ): PropertyDivergence {
        val methodValues = LinkedHashMap<PropertyAuditMethod, String>()
        methodValues[PropertyAuditMethod.BUILD_CONSTANT] = buildValue
        methodValues[PropertyAuditMethod.NATIVE_CALLBACK] =
            sanitizeNativeValue(nativeSnapshot.callbackValue(property))
        methodValues[PropertyAuditMethod.NATIVE_LEGACY] =
            sanitizeNativeValue(nativeSnapshot.legacyValue(property))
        methodValues[PropertyAuditMethod.JVM_GETPROP] = getpropSnapshot[property].orEmpty().trim()
        methodValues[PropertyAuditMethod.JAVA_REFLECTION] = readAccess.readReflectionValue(property)
        return PropertyDivergence(
            property = "$label <> $property",
            kind = PropertyDivergenceKind.FRAMEWORK_VS_PROPERTY,
            severity = SystemPropertySeverity.WARNING,
            methodValues = methodValues.filterValues { it.isNotBlank() },
        )
    }

    private fun resolvePropertySourceOrder(
        buildConstants: AuditBuildConstants,
        nativeSnapshot: FullPropertyNativeSnapshot,
        getpropSnapshot: Map<String, String>,
    ): List<String> {
        val raw = buildConstants.propertySourceOrder
            ?: preferredPropertyValue(
                "ro.product.property_source_order",
                nativeSnapshot,
                getpropSnapshot,
            )
        val parsed = raw.split(',').mapNotNull { part ->
            part.trim().lowercase().takeIf { it.isNotEmpty() }
        }
        // Without an explicit order, framework constants come from the system
        // partition alias (the plain ro.product.* keys).
        return parsed.ifEmpty { listOf(SYSTEM_PARTITION, "") }
    }

    private fun resolveProductProperty(
        field: String,
        sourceOrder: List<String>,
        readProperty: (String) -> String,
    ): Pair<String, String>? {
        for (partition in sourceOrder) {
            val key = if (partition.isEmpty()) {
                "ro.product.$field"
            } else {
                "ro.product.$partition.$field"
            }
            val value = readProperty(key)
            if (value.isNotBlank()) {
                return key to value
            }
        }
        return null
    }

    private fun preferredPropertyValue(
        property: String,
        nativeSnapshot: FullPropertyNativeSnapshot,
        getpropSnapshot: Map<String, String>,
    ): String {
        return sanitizeNativeValue(nativeSnapshot.callbackValue(property))
            .ifBlank { sanitizeNativeValue(nativeSnapshot.legacyValue(property)) }
            .ifBlank { getpropSnapshot[property].orEmpty().trim() }
            .ifBlank { readAccess.readReflectionValue(property) }
    }

    private fun distinctNormalized(
        key: String,
        populated: Map<PropertyAuditMethod, String>,
    ): Set<String> {
        return populated.values.map { normalizeValue(it) }.toSet()
    }

    private fun isVolatileProperty(
        key: String,
    ): Boolean {
        return VOLATILE_PREFIXES.any { key.startsWith(it) }
    }

    private fun normalizeValue(
        value: String,
    ): String {
        return value.trim().lowercase()
    }

    private fun sanitizeNativeValue(
        value: String,
    ): String {
        val trimmed = value.trim()
        return if (trimmed.contains(CALLBACK_REQUIRED_MESSAGE, ignoreCase = true)) {
            ""
        } else {
            trimmed
        }
    }

    private fun severityRank(
        severity: SystemPropertySeverity,
    ): Int {
        return when (severity) {
            SystemPropertySeverity.DANGER -> 0
            SystemPropertySeverity.WARNING -> 1
            SystemPropertySeverity.NEUTRAL -> 2
            SystemPropertySeverity.SAFE -> 3
        }
    }

    private companion object {
        private const val SYSTEM_PARTITION = "system"
        private const val CALLBACK_REQUIRED_MESSAGE =
            "Must use __system_property_read_callback() to read"

        private val VOLATILE_PREFIXES = listOf(
            "init.svc.",
            "net.dns",
            "dhcp.",
            "wlan.",
            "service.",
            "debug.",
            "log.tag.",
            "persist.logd.",
        )
    }
}
