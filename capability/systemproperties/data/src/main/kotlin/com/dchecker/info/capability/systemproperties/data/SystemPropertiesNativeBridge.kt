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

package com.dchecker.info.capability.systemproperties.data

import com.dchecker.info.capability.systemproperties.domain.FullPropertyNativeEntry
import com.dchecker.info.capability.systemproperties.domain.FullPropertyNativeSnapshot
import com.dchecker.info.capability.systemproperties.domain.PropAreaFinding
import com.dchecker.info.capability.systemproperties.domain.SystemPropertiesNativeSnapshot
import com.dchecker.info.core.native.NativePayloadCodec
import com.dchecker.info.core.native.NativePayloadContract
import com.dchecker.info.core.native.NativeSnapshotCollector

public class SystemPropertiesNativeBridge(
    private val collector: NativeSnapshotCollector = NativeSnapshotCollector.Default,
) {

    public fun collectSnapshot(
        propertyNames: Collection<String>,
    ): SystemPropertiesNativeSnapshot {
        if (propertyNames.isEmpty()) {
            return SystemPropertiesNativeSnapshot()
        }
        return collector.collect(
            readPayload = {
                nativeCollectSnapshot(propertyNames.distinct().sorted().toTypedArray())
            },
            parse = ::parse,
            unavailable = { status -> SystemPropertiesNativeSnapshot(collection = status) },
        )
    }

    /**
     * Enumerates every property through the native foreach API and reads each key through both
     * bionic entry points plus a shell getprop pipe, for the cross-method consistency audit.
     */
    public fun collectFullSnapshot(): FullPropertyNativeSnapshot {
        return collector.collect(
            readPayload = { nativeCollectFullSnapshot() },
            parse = ::parseFullSnapshot,
            unavailable = { FullPropertyNativeSnapshot() },
        )
    }

    /** Re-reads the given keys through both bionic entry points for the audit stability pass. */
    public fun readInlineSnapshot(
        propertyNames: Collection<String>,
    ): Map<String, FullPropertyNativeEntry> {
        if (propertyNames.isEmpty()) {
            return emptyMap()
        }
        return collector.collect(
            readPayload = {
                nativeReadInlineSnapshot(propertyNames.distinct().sorted().toTypedArray())
            },
            parse = ::parseInlineSnapshot,
            unavailable = { emptyMap() },
        )
    }

    internal fun parseFullSnapshot(
        raw: String,
    ): FullPropertyNativeSnapshot {
        if (raw.isBlank()) {
            return FullPropertyNativeSnapshot()
        }

        var foreachAvailable = false
        var totalCount = 0
        var shellAvailable = false
        val entries = linkedMapOf<String, FullPropertyNativeEntry>()
        val shellProperties = linkedMapOf<String, String>()

        raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.contains('=') }
            .forEach { line ->
                val key = line.substringBefore('=')
                val value = line.substringAfter('=')
                when (key) {
                    "FOREACH_AVAILABLE" -> foreachAvailable = value != "0"
                    "TOTAL_COUNT" -> totalCount = value.toIntOrNull() ?: 0
                    "SHELL_AVAILABLE" -> shellAvailable = value != "0"
                    "ENTRY" -> {
                        val parts = value.split('|', limit = 3)
                        if (parts.size == 3 && parts[0].isNotBlank()) {
                            entries[parts[0]] = FullPropertyNativeEntry(
                                key = parts[0],
                                callbackValue = parts[1].decodeValue(),
                                legacyValue = parts[2].decodeValue(),
                            )
                        }
                    }

                    "SHELL" -> {
                        val parts = value.split('|', limit = 2)
                        if (parts.size == 2 && parts[0].isNotBlank()) {
                            shellProperties[parts[0]] = parts[1].decodeValue()
                        }
                    }
                }
            }

        return FullPropertyNativeSnapshot(
            foreachAvailable = foreachAvailable,
            totalCount = if (totalCount > 0) totalCount else entries.size,
            shellAvailable = shellAvailable,
            entries = entries,
            shellProperties = shellProperties,
        )
    }

    internal fun parseInlineSnapshot(
        raw: String,
    ): Map<String, FullPropertyNativeEntry> {
        if (raw.isBlank()) {
            return emptyMap()
        }
        val entries = linkedMapOf<String, FullPropertyNativeEntry>()
        raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.contains('=') }
            .forEach { line ->
                val key = line.substringBefore('=')
                val value = line.substringAfter('=')
                if (key != "INLINE") {
                    return@forEach
                }
                val parts = value.split('|', limit = 3)
                if (parts.size == 3 && parts[0].isNotBlank()) {
                    entries[parts[0]] = FullPropertyNativeEntry(
                        key = parts[0],
                        callbackValue = parts[1].decodeValue(),
                        legacyValue = parts[2].decodeValue(),
                    )
                }
            }
        return entries
    }

    internal fun parse(
        raw: String,
    ): SystemPropertiesNativeSnapshot {
        if (raw.isBlank()) {
            return SystemPropertiesNativeSnapshot()
        }

        NativePayloadContract.requireKeys(raw, "AVAILABLE")

        var available = false
        val libcProperties = linkedMapOf<String, String>()
        val cmdlineBootParams = linkedMapOf<String, String>()
        val bootconfigBootParams = linkedMapOf<String, String>()
        var rawCmdline = ""
        var rawBootconfig = ""
        var propAreaAvailable = false
        var propAreaContextCount = 0
        var propAreaHoleCount = 0
        val propAreaFindings = mutableListOf<PropAreaFinding>()
        var readOnlyPropertyHandleAvailable = false
        var readOnlyPropertyHandleCheckedCount = 0

        raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.contains('=') }
            .forEach { line ->
                val key = line.substringBefore('=')
                val value = line.substringAfter('=')
                when (key) {
                    "AVAILABLE" -> available = NativePayloadCodec.decodeFlag(value)
                    "PROP" -> {
                        val parts = value.split('|', limit = 2)
                        if (parts.size == 2) {
                            libcProperties[parts[0]] = parts[1].decodeValue()
                        }
                    }

                    "CMDLINE" -> {
                        val parts = value.split('|', limit = 2)
                        if (parts.size == 2) {
                            cmdlineBootParams[parts[0]] = parts[1].decodeValue()
                        }
                    }

                    "BOOTCONFIG" -> {
                        val parts = value.split('|', limit = 2)
                        if (parts.size == 2) {
                            bootconfigBootParams[parts[0]] = parts[1].decodeValue()
                        }
                    }

                    "RAW_CMDLINE" -> rawCmdline = value.decodeValue()
                    "RAW_BOOTCONFIG" -> rawBootconfig = value.decodeValue()
                    "PROP_AREA_AVAILABLE" -> propAreaAvailable = NativePayloadCodec.decodeFlag(value)
                    "PROP_AREA_CONTEXTS" -> propAreaContextCount = value.toIntOrNull() ?: 0
                    "PROP_AREA_HOLES" -> propAreaHoleCount = value.toIntOrNull() ?: 0
                    "RO_HANDLE_AVAILABLE" -> readOnlyPropertyHandleAvailable = NativePayloadCodec.decodeFlag(value)
                    "RO_HANDLE_CHECKED" -> readOnlyPropertyHandleCheckedCount = value.toIntOrNull() ?: 0
                    "PROP_AREA_FINDING" -> {
                        val parts = value.split('|', limit = 3)
                        val holeCount = parts.getOrNull(1)?.toIntOrNull()
                        val context = parts.getOrNull(0).orEmpty()
                        val detail = parts.getOrNull(2)?.decodeValue().orEmpty()
                        if (parts.size == 3 && context.isNotBlank() && holeCount != null) {
                            propAreaFindings += PropAreaFinding(
                                context = context,
                                holeCount = holeCount,
                                detail = detail,
                            )
                        }
                    }
                }
            }

        return SystemPropertiesNativeSnapshot(
            available = available,
            libcProperties = libcProperties,
            cmdlineBootParams = cmdlineBootParams,
            bootconfigBootParams = bootconfigBootParams,
            rawCmdline = rawCmdline,
            rawBootconfig = rawBootconfig,
            propAreaAvailable = propAreaAvailable,
            propAreaContextCount = propAreaContextCount,
            propAreaHoleCount = propAreaHoleCount,
            propAreaFindings = propAreaFindings,
            readOnlyPropertyHandleAvailable = readOnlyPropertyHandleAvailable,
            readOnlyPropertyHandleCheckedCount = readOnlyPropertyHandleCheckedCount,
        )
    }

    private fun String.decodeValue(): String = NativePayloadCodec.decodeValue(this)

    private external fun nativeCollectSnapshot(propertyNames: Array<String>): String

    private external fun nativeCollectFullSnapshot(): String

    private external fun nativeReadInlineSnapshot(propertyNames: Array<String>): String
}
