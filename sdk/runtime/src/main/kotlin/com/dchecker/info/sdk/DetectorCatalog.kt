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

package com.dchecker.info.sdk

import com.dchecker.info.core.detector.Detector
import com.dchecker.info.features.adbruntime.detector.AdbRuntimeDetector
import com.dchecker.info.features.bootloader.detector.BootloaderDetector
import com.dchecker.info.features.customrom.detector.CustomRomDetector
import com.dchecker.info.features.dataaudit.detector.DataAuditDetector
import com.dchecker.info.features.dangerousapps.detector.DangerousAppsDetector
import com.dchecker.info.features.heapresidue.detector.HeapResidueDetector
import com.dchecker.info.features.kernelcheck.detector.KernelCheckDetector
import com.dchecker.info.features.lsposed.detector.LSPosedDetector
import com.dchecker.info.features.memory.detector.MemoryDetector
import com.dchecker.info.features.mount.detector.MountDetector
import com.dchecker.info.features.nativeroot.detector.NativeRootDetector
import com.dchecker.info.features.playintegrityfix.detector.PlayIntegrityFixDetector
import com.dchecker.info.features.rootmanagers.detector.RootManagersDetector
import com.dchecker.info.features.selinux.detector.SelinuxDetector
import com.dchecker.info.features.su.detector.SuDetector
import com.dchecker.info.features.systemproperties.detector.SystemPropertiesDetector
import com.dchecker.info.features.tee.detector.TeeDetector
import com.dchecker.info.features.virtualization.detector.VirtualizationDetector
import com.dchecker.info.features.zygisk.detector.ZygiskDetector

/**
 * Every detector, in the order their scans start.
 *
 * This is the one list of detectors: the SDK scans in this order, and the application starts its
 * dashboard sessions in this order. Bootloader and TEE start first, as the application has always
 * started them; every other detector follows by name. Adding a detector means adding it here and
 * giving it a dashboard card.
 */
public object DetectorCatalog {
    public val all: List<Detector<*, *>> = listOf(
        BootloaderDetector,
        TeeDetector,
        AdbRuntimeDetector,
        CustomRomDetector,
        DangerousAppsDetector,
        DataAuditDetector,
        HeapResidueDetector,
        KernelCheckDetector,
        LSPosedDetector,
        MemoryDetector,
        MountDetector,
        NativeRootDetector,
        PlayIntegrityFixDetector,
        RootManagersDetector,
        SelinuxDetector,
        SuDetector,
        SystemPropertiesDetector,
        VirtualizationDetector,
        ZygiskDetector,
    )
}
