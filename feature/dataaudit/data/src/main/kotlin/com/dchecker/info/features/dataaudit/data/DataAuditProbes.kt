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

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import com.dchecker.info.features.dataaudit.domain.AuditLens
import com.dchecker.info.features.dataaudit.domain.DataAuditGroup
import com.dchecker.info.features.dataaudit.domain.DataAuditReport
import com.dchecker.info.features.dataaudit.domain.DataPoint
import com.dchecker.info.features.dataaudit.domain.LensReading
import com.dchecker.info.features.dataaudit.domain.LensState
import java.io.File
import java.net.NetworkInterface

/**
 * The probe set of the data and identity audit: every point is read through as many vantage
 * points as the sandbox allows, and the verdict comes from comparing the lenses against each
 * other. Sealed surfaces invert the rule — the refusal is the conformant answer, and content
 * handed over is the finding.
 */
public class DataAuditProbes(
    private val context: Context,
) {

    private val telephony: TelephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    public fun collect(): List<DataPoint> {
        val points = mutableListOf<DataPoint>()
        points += identityProbes()
        points += telephonyProbes()
        points += networkProbes()
        points += advertisingProbes()
        points += packageProbes()
        points += environmentProbes()
        points += sealedProbes()
        return points
    }

    // ---------------------------------------------------------------- identity

    private fun identityProbes(): List<DataPoint> {
        val sdkSerial = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Build.getSerial()
            } else {
                @Suppress("DEPRECATION")
                Build.SERIAL
            }
        }.getOrElse { if (it is SecurityException) REFUSED_MARKER else "" }.orEmpty().trim()

        val points = mutableListOf(
            DataAuditReport.judge(
                point = "device serial",
                group = DataAuditGroup.IDENTITY,
                note = "Build.getSerial needs a privileged permission; the property path does not.",
                readings = listOf(
                    reading(AuditLens.SDK_API, sdkSerial),
                    reading(AuditLens.SHELL, shellProp("ro.serialno")),
                    reading(AuditLens.SHELL, shellProp("ro.boot.serialno")),
                ),
            ),
            DataAuditReport.judge(
                point = "android id",
                group = DataAuditGroup.IDENTITY,
                readings = listOf(
                    reading(
                        AuditLens.SDK_API,
                        runCatching {
                            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                        }.getOrNull(),
                    ),
                    reading(AuditLens.SHELL, shellSettingsGet("secure", "android_id")),
                ),
            ),
        )

        val deviceIds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                @Suppress("DEPRECATION")
                (0 until telephony.phoneCount).mapNotNull { telephony.getDeviceId(it) }
            }.getOrElse { emptyList() }
        } else {
            emptyList()
        }
        deviceIds.size.takeIf { it > 0 }?.let { count ->
            points += DataAuditReport.judge(
                point = "device id slots",
                group = DataAuditGroup.IDENTITY,
                note = "Slots the radio reports, read through the typed SDK call.",
                readings = listOf(
                    LensReading(AuditLens.SDK_API, LensState.VALUE, "$count slot(s)"),
                ),
            )
        }

        points += DataAuditReport.judge(
            point = "imei (slot 0)",
            group = DataAuditGroup.IDENTITY,
            readings = listOf(
                reading(
                    AuditLens.SDK_API,
                    sdk23Plus { @Suppress("DEPRECATION") telephony.deviceId },
                ),
                reading(
                    AuditLens.SDK_ALT,
                    sdk26Plus { telephony.getImei(0) },
                ),
            ),
        )
        points += DataAuditReport.judge(
            point = "meid",
            group = DataAuditGroup.IDENTITY,
            readings = listOf(
                reading(
                    AuditLens.SDK_ALT,
                    sdk26Plus { telephony.getMeid(0) },
                ),
            ),
        )
        points += DataAuditReport.judge(
            point = "imsi (subscriber id)",
            group = DataAuditGroup.IDENTITY,
            readings = listOf(
                reading(
                    AuditLens.SDK_API,
                    sdk29PlusRefused { telephony.subscriberId },
                ),
            ),
        )
        points += DataAuditReport.judge(
            point = "iccid (sim serial)",
            group = DataAuditGroup.IDENTITY,
            readings = listOf(
                reading(
                    AuditLens.SDK_API,
                    sdk29PlusRefused { telephony.simSerialNumber },
                ),
            ),
        )
        return points
    }

    // ---------------------------------------------------------------- telephony

    private fun telephonyProbes(): List<DataPoint> {
        return listOf(
            DataAuditReport.judge(
                point = "operator name",
                group = DataAuditGroup.TELEPHONY,
                readings = listOf(
                    reading(AuditLens.SDK_API, runCatching { telephony.networkOperatorName }.getOrNull()),
                    reading(AuditLens.SHELL, shellProp("gsm.operator.alpha")),
                ),
            ),
            DataAuditReport.judge(
                point = "operator numeric",
                group = DataAuditGroup.TELEPHONY,
                readings = listOf(
                    reading(AuditLens.SDK_API, runCatching { telephony.networkOperator }.getOrNull()),
                    reading(AuditLens.SHELL, shellProp("gsm.operator.numeric")),
                ),
            ),
            DataAuditReport.judge(
                point = "network country",
                group = DataAuditGroup.TELEPHONY,
                readings = listOf(
                    reading(AuditLens.SDK_API, runCatching { telephony.networkCountryIso }.getOrNull()),
                    reading(AuditLens.SHELL, shellProp("gsm.operator.iso-country")),
                ),
            ),
            DataAuditReport.judge(
                point = "sim state",
                group = DataAuditGroup.TELEPHONY,
                readings = listOf(
                    reading(
                        AuditLens.SDK_API,
                        runCatching {
                            when (telephony.simState) {
                                TelephonyManager.SIM_STATE_READY -> "ready"
                                TelephonyManager.SIM_STATE_ABSENT -> "absent"
                                TelephonyManager.SIM_STATE_PIN_REQUIRED -> "pin_required"
                                TelephonyManager.SIM_STATE_PUK_REQUIRED -> "puk_required"
                                TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "network_locked"
                                else -> "unknown"
                            }
                        }.getOrNull(),
                    ),
                    reading(AuditLens.SHELL, shellProp("gsm.sim.state")),
                ),
            ),
            DataAuditReport.judge(
                point = "roaming",
                group = DataAuditGroup.TELEPHONY,
                readings = listOf(
                    reading(
                        AuditLens.SDK_API,
                        runCatching {
                            if (telephony.isNetworkRoaming) "yes" else "no"
                        }.getOrNull(),
                    ),
                    reading(AuditLens.SHELL, shellProp("gsm.roaming")),
                ),
            ),
            DataAuditReport.judge(
                point = "active subscriptions",
                group = DataAuditGroup.TELEPHONY,
                readings = listOf(
                    reading(
                        AuditLens.SDK_API,
                        runCatching {
                            val sms = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
                                as SubscriptionManager
                            sms.activeSubscriptionInfoCount.toString()
                        }.getOrNull(),
                    ),
                    reading(
                        AuditLens.SHELL,
                        AuditShell.sh("dumpsys isub | grep -c 'Subscription:'"),
                    ),
                ),
            ),
        )
    }

    // ---------------------------------------------------------------- network

    private fun networkProbes(): List<DataPoint> {
        val sdkMac = runCatching {
            NetworkInterface.getNetworkInterfaces().asSequence()
                .firstOrNull { it.name == "wlan0" }
                ?.hardwareAddress
                ?.joinToString(":") { String.format("%02x", it) }
                .orEmpty()
        }.getOrNull().orEmpty()

        val ipLinkMac = AuditShell.sh("ip link show wlan0")
            ?.lineSequence()
            ?.firstOrNull { it.contains("link/ether") }
            ?.substringAfter("link/ether")
            ?.trim()
            ?.substringBefore(' ')
            .orEmpty()

        val sysfsMac = AuditShell.sh("cat /sys/class/net/wlan0/address").orEmpty()

        val points = mutableListOf(
            DataAuditReport.judge(
                point = "wlan0 mac",
                group = DataAuditGroup.NETWORK,
                note = "Three vantage points: the SDK interface, the kernel netlink state via ip, " +
                        "and the sysfs file.",
                readings = listOf(
                    reading(AuditLens.SDK_API, sdkMac.ifBlank { null }, refused = sdkMac.isBlank()),
                    reading(AuditLens.SHELL, ipLinkMac.ifBlank { null }, refused = ipLinkMac.isBlank()),
                    reading(
                        AuditLens.KERNEL_FILE,
                        sysfsMac.ifBlank { null },
                        refused = sysfsMac.isBlank(),
                    ),
                ),
            ),
            DataAuditReport.judge(
                point = "local ipv4 addresses",
                group = DataAuditGroup.NETWORK,
                readings = listOf(
                    reading(
                        AuditLens.SDK_API,
                        runCatching {
                            NetworkInterface.getNetworkInterfaces().asSequence()
                                .filter { it.isUp && !it.isLoopback }
                                .flatMap { it.inetAddresses.asSequence() }
                                .filter { it.isSiteLocalAddress }
                                .map { it.hostAddress }
                                .distinct()
                                .sorted()
                                .joinToString(", ")
                        }.getOrNull(),
                    ),
                    reading(
                        AuditLens.SHELL,
                        AuditShell.sh("ip -f inet addr | grep inet | awk '{print \$2}' | tr '\\n' ' '"),
                    ),
                ),
            ),
            DataAuditReport.judge(
                point = "bluetooth mac",
                group = DataAuditGroup.NETWORK,
                readings = listOf(
                    reading(
                        AuditLens.SDK_API,
                        runCatching {
                            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return@runCatching null
                            adapter.address
                        }.getOrNull(),
                    ),
                    reading(
                        AuditLens.SHELL,
                        shellSettingsGet("secure", "bluetooth_address"),
                    ),
                ),
            ),
        )
        return points
    }

    // ---------------------------------------------------------------- advertising

    private fun advertisingProbes(): List<DataPoint> {
        val keys = listOf(
            "advertising_id",
            "advertising_id_info",
            "oaid",
            "vaid",
            "aaid",
            "pps_oaid",
        )
        val stores = listOf(
            "secure" to { k: String -> Settings.Secure.getString(context.contentResolver, k) },
            "global" to { k: String -> Settings.Global.getString(context.contentResolver, k) },
        )
        val points = mutableListOf<DataPoint>()
        for ((storeName, readSetting) in stores) {
            for (key in keys) {
                val sdkValue = runCatching { readSetting(key) }.getOrNull()
                val shellValue = shellSettingsGet(storeName, key)
                val populated = listOfNotNull(
                    sdkValue?.trim()?.takeIf { it.isNotBlank() && it != "null" },
                    shellValue?.trim()?.takeIf { it.isNotBlank() && it != "null" },
                )
                if (populated.isEmpty()) {
                    continue
                }
                points += DataAuditReport.judge(
                    point = "ad key $storeName/$key",
                    group = DataAuditGroup.ADVERTISING,
                    readings = listOf(
                        reading(AuditLens.SDK_API, sdkValue),
                        reading(AuditLens.SHELL, shellValue),
                    ),
                )
            }
        }

        // The Google advertising id itself, through the Play Services client when it resolves.
        val gaid = runCatching {
            val clientClass = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient")
            val getMethod = clientClass.getMethod("getAdvertisingIdInfo", Context::class.java)
            val info = getMethod.invoke(null, context)
            info?.javaClass?.getMethod("getId")?.invoke(info) as? String
        }.getOrNull()
        points += DataAuditReport.judge(
            point = "google advertising id (gaid)",
            group = DataAuditGroup.ADVERTISING,
            readings = listOf(
                reading(AuditLens.SDK_API, gaid),
            ),
        )
        return points
    }

    // ---------------------------------------------------------------- packages

    private fun packageProbes(): List<DataPoint> {
        val selfPackage = context.packageName
        val sdkCount = runCatching {
            context.packageManager.getInstalledPackages(0).size
        }.getOrNull()
        val shellCount = AuditShell.sh("pm list packages | wc -l")?.trim()?.toIntOrNull()

        val installSource = runCatching {
            context.packageManager.getInstallSourceInfo(selfPackage).installingPackageName
        }.getOrNull()
        val shellInstallSource = AuditShell
            .sh("pm list packages -i | grep '^package:$selfPackage '")
            ?.substringAfter(" origin:")
            ?.trim()

        return listOf(
            DataAuditReport.judge(
                point = "installed package count",
                group = DataAuditGroup.PACKAGE,
                note = "A mismatch means an app-hiding module filters the SDK list but not the shell " +
                        "list, or the other way around.",
                readings = listOf(
                    reading(
                        AuditLens.SDK_API,
                        sdkCount?.toString(),
                        refused = sdkCount == null,
                    ),
                    reading(
                        AuditLens.SHELL,
                        shellCount?.toString(),
                        refused = shellCount == null,
                    ),
                ),
            ),
            DataAuditReport.judge(
                point = "self install source",
                group = DataAuditGroup.PACKAGE,
                readings = listOf(
                    reading(AuditLens.SDK_API, installSource),
                    reading(AuditLens.SHELL, shellInstallSource),
                ),
            ),
        )
    }

    // ---------------------------------------------------------------- system & env

    private fun environmentProbes(): List<DataPoint> {
        val sdkPath = System.getenv("PATH").orEmpty()
        val shellPath = AuditShell.sh("echo \$PATH").orEmpty()
        val sdkKernel = System.getProperty("os.version").orEmpty()
        val shellKernel = AuditShell.sh("uname -r").orEmpty()

        return listOf(
            DataAuditReport.judge(
                point = "PATH environment",
                group = DataAuditGroup.SYSTEM_ENV,
                note = "A mismatch means something injected entries into the process PATH, " +
                        "the classic magisk-tmp marker.",
                readings = listOf(
                    reading(AuditLens.SDK_API, sdkPath),
                    reading(AuditLens.SHELL, shellPath),
                ),
            ),
            DataAuditReport.judge(
                point = "kernel release",
                group = DataAuditGroup.SYSTEM_ENV,
                readings = listOf(
                    reading(AuditLens.SDK_API, sdkKernel),
                    reading(AuditLens.SHELL, shellKernel),
                ),
            ),
        )
    }

    // ---------------------------------------------------------------- sealed surfaces

    private fun sealedProbes(): List<DataPoint> {
        val sealedFiles = listOf(
            "/proc/cmdline",
            "/proc/1/cmdline",
            "/proc/1/stat",
            "/dev/kmsg",
            "/system/build.prop",
            "/sys/class/block/mmcblk0/device/cid",
            "/sys/devices/soc0/machine",
            "/sys/devices/soc0/serial",
        )
        val points = mutableListOf<DataPoint>()
        for (path in sealedFiles) {
            val direct = runCatching { File(path).readText().trim().take(96) }.getOrElse { "" }
            val shell = AuditShell.sh("cat '$path' 2>/dev/null")?.trim()?.take(96).orEmpty()
            points += DataAuditReport.judge(
                point = path,
                group = DataAuditGroup.SANDBOX,
                sealed = true,
                readings = listOf(
                    reading(AuditLens.KERNEL_FILE, direct.ifBlank { null }, refused = direct.isBlank()),
                    reading(AuditLens.SHELL, shell.ifBlank { null }, refused = shell.isBlank()),
                ),
            )
        }

        val services = listOf(
            "account",
            "clipboard",
            "device_policy",
            "diskstats",
            "location",
            "media.drm",
            "netpolicy",
            "notification",
            "telephony.registry",
            "usagestats",
        )
        for (service in services) {
            val output = AuditShell.exec(listOf("dumpsys", service))
            val denied = output == null ||
                    output.contains("Permission Denial", ignoreCase = true) ||
                    output.contains("SecurityException", ignoreCase = true) ||
                    output.contains("java.lang.SecurityException", ignoreCase = true)
            points += DataAuditReport.judge(
                point = "dumpsys $service",
                group = DataAuditGroup.SANDBOX,
                sealed = true,
                readings = listOf(
                    if (denied) {
                        LensReading(AuditLens.SHELL, LensState.REFUSED)
                    } else {
                        LensReading(AuditLens.SHELL, LensState.VALUE, output!!.lineSequence().first().take(96))
                    },
                ),
            )
        }
        return points
    }

    // ---------------------------------------------------------------- helpers

    private fun reading(
        lens: AuditLens,
        value: String?,
        refused: Boolean = false,
    ): LensReading = when {
        value == REFUSED_MARKER -> LensReading(lens, LensState.REFUSED)
        value.isNullOrBlank() && refused -> LensReading(lens, LensState.REFUSED)
        value.isNullOrBlank() -> LensReading(lens, LensState.ABSENT)
        else -> LensReading(lens, LensState.VALUE, value.trim())
    }

    private fun shellProp(key: String): String? = AuditShell.exec(listOf("getprop", key))

    private fun shellSettingsGet(namespace: String, key: String): String? =
        AuditShell.exec(listOf("settings", "get", namespace, key))
            ?.takeIf { it.isNotBlank() && it != "null" }

    private inline fun <T> sdk23Plus(crossinline block: () -> T): String? = runCatching {
        @Suppress("DEPRECATION")
        block()?.toString()
    }.getOrElse { if (it is SecurityException) REFUSED_MARKER else null }

    private inline fun <T> sdk26Plus(crossinline block: () -> T): String? =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            null
        } else {
            runCatching { block()?.toString() }
                .getOrElse { if (it is SecurityException) REFUSED_MARKER else null }
        }

    private inline fun <T> sdk29PlusRefused(crossinline block: () -> T): String? = runCatching {
        block()?.toString()
    }.getOrElse { if (it is SecurityException) REFUSED_MARKER else null }

    private companion object {
        private const val REFUSED_MARKER = "\u0000refused"
    }
}
