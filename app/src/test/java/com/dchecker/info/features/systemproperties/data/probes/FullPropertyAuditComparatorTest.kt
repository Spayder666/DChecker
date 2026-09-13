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

package com.dchecker.info.features.systemproperties.data.probes

import com.dchecker.info.features.systemproperties.data.native.FullPropertyNativeEntry
import com.dchecker.info.features.systemproperties.data.native.FullPropertyNativeSnapshot
import com.dchecker.info.features.systemproperties.data.utils.PropertyReadAccess
import com.dchecker.info.features.systemproperties.domain.PropertyAuditMethod
import com.dchecker.info.features.systemproperties.domain.PropertyDivergenceKind
import com.dchecker.info.features.systemproperties.domain.SystemPropertySeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FullPropertyAuditComparatorTest {

    private class FakeReadAccess(
        private val reflectionValues: Map<String, String> = emptyMap(),
        private val jvmValues: Map<String, String> = emptyMap(),
        private val inlineEntries: Map<String, FullPropertyNativeEntry> = emptyMap(),
    ) : PropertyReadAccess {
        override fun readReflectionValue(property: String): String {
            return reflectionValues[property].orEmpty()
        }

        override fun readJvmValue(property: String): String {
            return jvmValues[property].orEmpty()
        }

        override fun readInlineNativeEntry(property: String): FullPropertyNativeEntry? {
            return inlineEntries[property]
        }
    }

    private fun nativeSnapshot(
        entries: Map<String, Pair<String, String>> = emptyMap(),
        shell: Map<String, String> = emptyMap(),
    ): FullPropertyNativeSnapshot {
        return FullPropertyNativeSnapshot(
            foreachAvailable = entries.isNotEmpty(),
            totalCount = entries.size,
            shellAvailable = shell.isNotEmpty(),
            entries = entries.mapValues { (key, values) ->
                FullPropertyNativeEntry(
                    key = key,
                    callbackValue = values.first,
                    legacyValue = values.second,
                )
            },
            shellProperties = shell,
        )
    }

    private fun buildConstants(
        model: String = "PixelModel",
        propertySourceOrder: String? = null,
    ): AuditBuildConstants {
        return AuditBuildConstants(
            model = model,
            brand = "google",
            device = "pixel",
            manufacturer = "Google",
            product = "pixel_product",
            board = "board",
            bootloader = "bootloader",
            hardware = "qcom",
            fingerprint = "google/pixel/pixel:15/number/release-keys",
            id = "build-id",
            display = "display-id",
            versionRelease = "15",
            versionSdk = "35",
            versionIncremental = "incremental",
            versionSecurityPatch = "2026-09-05",
            buildTimeMillis = 1757000000000L,
            propertySourceOrder = propertySourceOrder,
        )
    }

    @Test
    fun `stable java vs native mismatch on ro property is reported as danger`() {
        val readAccess = FakeReadAccess(
            reflectionValues = mapOf("ro.build.type" to "user"),
            inlineEntries = mapOf(
                "ro.build.type" to FullPropertyNativeEntry("ro.build.type", "userdebug", "userdebug"),
            ),
        )
        val snapshot = nativeSnapshot(
            entries = mapOf("ro.build.type" to ("userdebug" to "userdebug")),
            shell = mapOf("ro.build.type" to "userdebug"),
        )

        val audit = FullPropertyAuditComparator(readAccess).audit(
            nativeSnapshot = snapshot,
            getpropSnapshot = mapOf("ro.build.type" to "userdebug"),
            buildConstants = buildConstants(),
        )

        val divergence = audit.divergences.single { it.property == "ro.build.type" }
        assertEquals(PropertyDivergenceKind.JAVA_VS_NATIVE, divergence.kind)
        assertEquals(SystemPropertySeverity.DANGER, divergence.severity)
        assertEquals("user", divergence.methodValues[PropertyAuditMethod.JAVA_REFLECTION])
        assertEquals("userdebug", divergence.methodValues[PropertyAuditMethod.NATIVE_CALLBACK])
    }

    @Test
    fun `transient mismatch disappears on second pass and is only counted`() {
        val readAccess = FakeReadAccess(
            reflectionValues = mapOf("sys.thermal.state" to "hot"),
            inlineEntries = mapOf(
                "sys.thermal.state" to FullPropertyNativeEntry("sys.thermal.state", "hot", "hot"),
            ),
        )
        val snapshot = nativeSnapshot(
            entries = mapOf("sys.thermal.state" to ("cool" to "cool")),
        )

        val audit = FullPropertyAuditComparator(readAccess).audit(
            nativeSnapshot = snapshot,
            getpropSnapshot = mapOf("sys.thermal.state" to "cool"),
            buildConstants = buildConstants(),
        )

        assertTrue(audit.divergences.none { it.property == "sys.thermal.state" })
        assertEquals(1, audit.transientCount)
    }

    @Test
    fun `volatile prefix mismatch is downgraded to neutral`() {
        val readAccess = FakeReadAccess(
            reflectionValues = mapOf("init.svc.zygote" to "restarting"),
            inlineEntries = mapOf(
                "init.svc.zygote" to FullPropertyNativeEntry("init.svc.zygote", "running", "running"),
            ),
        )
        val snapshot = nativeSnapshot(
            entries = mapOf("init.svc.zygote" to ("running" to "running")),
        )

        val audit = FullPropertyAuditComparator(readAccess).audit(
            nativeSnapshot = snapshot,
            getpropSnapshot = mapOf("init.svc.zygote" to "running"),
            buildConstants = buildConstants(),
        )

        val divergence = audit.divergences.single { it.property == "init.svc.zygote" }
        assertEquals(SystemPropertySeverity.NEUTRAL, divergence.severity)
    }

    @Test
    fun `native callback and legacy disagreement is native vs native warning`() {
        val readAccess = FakeReadAccess(
            inlineEntries = mapOf(
                "ro.kernel.qemu" to FullPropertyNativeEntry("ro.kernel.qemu", "0", "1"),
            ),
        )
        val snapshot = nativeSnapshot(
            entries = mapOf("ro.kernel.qemu" to ("0" to "1")),
        )

        val audit = FullPropertyAuditComparator(readAccess).audit(
            nativeSnapshot = snapshot,
            getpropSnapshot = emptyMap(),
            buildConstants = buildConstants(),
        )

        val divergence = audit.divergences.single { it.property == "ro.kernel.qemu" }
        assertEquals(PropertyDivergenceKind.NATIVE_VS_NATIVE, divergence.kind)
        assertEquals(SystemPropertySeverity.WARNING, divergence.severity)
    }

    @Test
    fun `agreeing values produce no divergences`() {
        val readAccess = FakeReadAccess(
            reflectionValues = mapOf("ro.secure" to "1"),
            inlineEntries = mapOf(
                "ro.secure" to FullPropertyNativeEntry("ro.secure", "1", "1"),
            ),
        )
        val snapshot = nativeSnapshot(
            entries = mapOf("ro.secure" to ("1" to "1")),
            shell = mapOf("ro.secure" to "1"),
        )

        val audit = FullPropertyAuditComparator(readAccess).audit(
            nativeSnapshot = snapshot,
            getpropSnapshot = mapOf("ro.secure" to "1"),
            buildConstants = buildConstants(),
        )

        assertTrue(audit.divergences.isEmpty())
        assertEquals(1, audit.checkedCount)
        assertEquals(0, audit.mismatchCount)
    }

    @Test
    fun `build model drift is reported against the resolved product property`() {
        val readAccess = FakeReadAccess(
            reflectionValues = mapOf("ro.product.model" to "HonestModel"),
            inlineEntries = mapOf(
                "ro.product.model" to FullPropertyNativeEntry("ro.product.model", "HonestModel", "HonestModel"),
            ),
        )
        val snapshot = nativeSnapshot(
            entries = mapOf("ro.product.model" to ("HonestModel" to "HonestModel")),
        )

        val audit = FullPropertyAuditComparator(readAccess).audit(
            nativeSnapshot = snapshot,
            getpropSnapshot = mapOf("ro.product.model" to "HonestModel"),
            buildConstants = buildConstants(model = "SpoofedModel"),
        )

        val divergence = audit.divergences.single()
        assertEquals("Build.MODEL <> ro.product.model", divergence.property)
        assertEquals(PropertyDivergenceKind.FRAMEWORK_VS_PROPERTY, divergence.kind)
        assertEquals(SystemPropertySeverity.WARNING, divergence.severity)
    }

    @Test
    fun `build model respects the property source order`() {
        val readAccess = FakeReadAccess(
            reflectionValues = mapOf(
                "ro.product.vendor.model" to "VendorModel",
                "ro.product.model" to "SystemModel",
            ),
            inlineEntries = mapOf(
                "ro.product.vendor.model" to FullPropertyNativeEntry(
                    "ro.product.vendor.model",
                    "VendorModel",
                    "VendorModel",
                ),
            ),
        )
        val snapshot = nativeSnapshot(
            entries = mapOf("ro.product.vendor.model" to ("VendorModel" to "VendorModel")),
        )

        val audit = FullPropertyAuditComparator(readAccess).audit(
            nativeSnapshot = snapshot,
            getpropSnapshot = mapOf("ro.product.vendor.model" to "VendorModel"),
            buildConstants = buildConstants(model = "VendorModel", propertySourceOrder = "vendor,system"),
        )

        assertTrue(audit.divergences.none { it.property.startsWith("Build.MODEL") })
    }

    @Test
    fun `checked count is the union of every key source`() {
        val readAccess = FakeReadAccess()
        val snapshot = nativeSnapshot(
            entries = mapOf("ro.a" to ("1" to "1")),
            shell = mapOf("shell.only" to "x"),
        )

        val audit = FullPropertyAuditComparator(readAccess).audit(
            nativeSnapshot = snapshot,
            getpropSnapshot = mapOf("getprop.only" to "y"),
            buildConstants = buildConstants(),
        )

        assertEquals(3, audit.checkedCount)
    }

    @Test
    fun `callback required message values are treated as blank`() {
        val readAccess = FakeReadAccess(
            reflectionValues = mapOf("ro.long.value" to "real"),
            inlineEntries = mapOf(
                "ro.long.value" to FullPropertyNativeEntry(
                    "ro.long.value",
                    "Must use __system_property_read_callback() to read",
                    "",
                ),
            ),
        )
        val snapshot = nativeSnapshot(
            entries = mapOf(
                "ro.long.value" to ("Must use __system_property_read_callback() to read" to ""),
            ),
        )

        val audit = FullPropertyAuditComparator(readAccess).audit(
            nativeSnapshot = snapshot,
            getpropSnapshot = mapOf("ro.long.value" to "real"),
            buildConstants = buildConstants(),
        )

        // Only one populated method remains, so there is nothing to compare.
        assertTrue(audit.divergences.none { it.property == "ro.long.value" })
    }
}
