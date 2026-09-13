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

package com.dchecker.info.features.dashboard.ui.preview

import com.dchecker.info.features.bootloader.domain.BootloaderReport
import com.dchecker.info.features.bootloader.presentation.BootloaderCardModelMapper
import com.dchecker.info.features.customrom.domain.CustomRomReport
import com.dchecker.info.features.customrom.presentation.CustomRomCardModelMapper
import com.dchecker.info.features.deviceinfo.domain.DeviceInfoReport
import com.dchecker.info.features.deviceinfo.presentation.DeviceInfoCardModelMapper
import com.dchecker.info.features.dangerousapps.data.rules.DangerousAppsCatalog
import com.dchecker.info.features.dangerousapps.domain.DangerousAppsReport
import com.dchecker.info.features.dangerousapps.presentation.DangerousAppsCardModelMapper
import com.dchecker.info.features.dangerousapps.ui.model.DangerousAppsCardModel
import com.dchecker.info.features.dashboard.ui.model.DashboardDetectorCardEntry
import com.dchecker.info.features.dashboard.ui.model.DashboardDetectorContribution
import com.dchecker.info.features.dashboard.ui.model.DashboardUiState
import com.dchecker.info.features.dashboard.ui.model.buildDashboardFindings
import com.dchecker.info.features.dashboard.ui.model.buildDashboardOverview
import com.dchecker.info.features.dashboard.ui.model.sortDashboardDetectorCards
import com.dchecker.info.features.kernelcheck.domain.KernelCheckReport
import com.dchecker.info.features.kernelcheck.presentation.KernelCheckCardModelMapper
import com.dchecker.info.features.lsposed.domain.LSPosedReport
import com.dchecker.info.features.lsposed.presentation.LSPosedCardModelMapper
import com.dchecker.info.features.memory.domain.MemoryReport
import com.dchecker.info.features.memory.presentation.MemoryCardModelMapper
import com.dchecker.info.features.mount.domain.MountReport
import com.dchecker.info.features.mount.presentation.MountCardModelMapper
import com.dchecker.info.features.nativeroot.domain.NativeRootReport
import com.dchecker.info.features.nativeroot.presentation.NativeRootCardModelMapper
import com.dchecker.info.features.playintegrityfix.domain.PlayIntegrityFixReport
import com.dchecker.info.features.playintegrityfix.presentation.PlayIntegrityFixCardModelMapper
import com.dchecker.info.features.selinux.domain.SelinuxReport
import com.dchecker.info.features.selinux.presentation.SelinuxCardModelMapper
import com.dchecker.info.features.su.domain.SuReport
import com.dchecker.info.features.su.presentation.SuCardModelMapper
import com.dchecker.info.features.systemproperties.domain.SystemPropertiesReport
import com.dchecker.info.features.systemproperties.presentation.SystemPropertiesCardModelMapper
import com.dchecker.info.features.tee.domain.TeeReport
import com.dchecker.info.features.tee.presentation.TeeCardModelMapper
import com.dchecker.info.features.virtualization.domain.VirtualizationReport
import com.dchecker.info.features.virtualization.presentation.VirtualizationCardModelMapper
import com.dchecker.info.features.zygisk.domain.ZygiskReport
import com.dchecker.info.features.zygisk.presentation.ZygiskCardModelMapper

object DashboardPreviewData {

    private val bootloaderMapper = BootloaderCardModelMapper()
    private val teeMapper = TeeCardModelMapper()
    private val customRomMapper = CustomRomCardModelMapper()
    private val dangerousAppsMapper = DangerousAppsCardModelMapper()
    private val deviceInfoMapper = DeviceInfoCardModelMapper()
    private val kernelCheckMapper = KernelCheckCardModelMapper()
    private val lsposedMapper = LSPosedCardModelMapper()
    private val memoryMapper = MemoryCardModelMapper()
    private val mountMapper = MountCardModelMapper()
    private val nativeRootMapper = NativeRootCardModelMapper()
    private val playIntegrityFixMapper = PlayIntegrityFixCardModelMapper()
    private val selinuxMapper = SelinuxCardModelMapper()
    private val suMapper = SuCardModelMapper()
    private val systemPropertiesMapper = SystemPropertiesCardModelMapper()
    private val virtualizationMapper = VirtualizationCardModelMapper()
    private val zygiskMapper = ZygiskCardModelMapper()

    fun create(): DashboardUiState {
        val bootloaderCard = bootloaderMapper.map(BootloaderReport.loading())
        val teeCard = teeMapper.map(TeeReport.loading(), isExpanded = false)
        val customRomCard = customRomMapper.map(CustomRomReport.loading())
        val dangerousAppsCard =
            dangerousAppsMapper.map(DangerousAppsReport.loading(DangerousAppsCatalog.targets))
        val deviceInfoCard = deviceInfoMapper.map(DeviceInfoReport.loading())
        val kernelCheckCard = kernelCheckMapper.map(KernelCheckReport.loading())
        val lsposedCard = lsposedMapper.map(LSPosedReport.loading())
        val memoryCard = memoryMapper.map(MemoryReport.loading())
        val mountCard = mountMapper.map(MountReport.loading())
        val nativeRootCard = nativeRootMapper.map(NativeRootReport.loading())
        val playIntegrityFixCard = playIntegrityFixMapper.map(PlayIntegrityFixReport.loading())
        val selinuxCard = selinuxMapper.map(SelinuxReport.loading())
        val suCard = suMapper.map(SuReport.loading())
        val systemPropertiesCard = systemPropertiesMapper.map(SystemPropertiesReport.loading())
        val virtualizationCard = virtualizationMapper.map(VirtualizationReport.loading())
        val zygiskCard = zygiskMapper.map(ZygiskReport.loading())
        val contributions = listOf(
            DashboardDetectorContribution(
                id = "bootloader",
                title = bootloaderCard.title,
                status = bootloaderCard.status,
                headline = bootloaderCard.verdict,
                summary = bootloaderCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "custom_rom",
                title = customRomCard.title,
                status = customRomCard.status,
                headline = customRomCard.verdict,
                summary = customRomCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "dangerous_apps",
                title = dangerousAppsCard.title,
                status = dangerousAppsCard.status,
                headline = dangerousAppsCard.verdict,
                summary = dangerousAppsCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "kernel_check",
                title = kernelCheckCard.title,
                status = kernelCheckCard.status,
                headline = kernelCheckCard.verdict,
                summary = kernelCheckCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "lsposed",
                title = lsposedCard.title,
                status = lsposedCard.status,
                headline = lsposedCard.verdict,
                summary = lsposedCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "memory",
                title = memoryCard.title,
                status = memoryCard.status,
                headline = memoryCard.verdict,
                summary = memoryCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "mount",
                title = mountCard.title,
                status = mountCard.status,
                headline = mountCard.verdict,
                summary = mountCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "selinux",
                title = selinuxCard.title,
                status = selinuxCard.status,
                headline = selinuxCard.verdict,
                summary = selinuxCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "play_integrity_fix",
                title = playIntegrityFixCard.title,
                status = playIntegrityFixCard.status,
                headline = playIntegrityFixCard.verdict,
                summary = playIntegrityFixCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "native_root",
                title = nativeRootCard.title,
                status = nativeRootCard.status,
                headline = nativeRootCard.verdict,
                summary = nativeRootCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "su",
                title = suCard.title,
                status = suCard.status,
                headline = suCard.verdict,
                summary = suCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "system_properties",
                title = systemPropertiesCard.title,
                status = systemPropertiesCard.status,
                headline = systemPropertiesCard.verdict,
                summary = systemPropertiesCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "tee",
                title = teeCard.title,
                status = teeCard.status,
                headline = teeCard.verdict,
                summary = teeCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "virtualization",
                title = virtualizationCard.title,
                status = virtualizationCard.status,
                headline = virtualizationCard.verdict,
                summary = virtualizationCard.summary,
                ready = false,
            ),
            DashboardDetectorContribution(
                id = "zygisk",
                title = zygiskCard.title,
                status = zygiskCard.status,
                headline = zygiskCard.verdict,
                summary = zygiskCard.summary,
                ready = false,
            ),
        )
        return DashboardUiState(
            overview = buildDashboardOverview(contributions),
            topFindings = buildDashboardFindings(contributions),
            detectorCards = sortDashboardDetectorCards(
                listOf(
                    DashboardDetectorCardEntry.Bootloader(bootloaderCard),
                    DashboardDetectorCardEntry.CustomRom(customRomCard),
                    DashboardDetectorCardEntry.DangerousApps(dangerousAppsCard),
                    DashboardDetectorCardEntry.KernelCheck(kernelCheckCard),
                    DashboardDetectorCardEntry.LSPosed(lsposedCard),
                    DashboardDetectorCardEntry.Memory(memoryCard),
                    DashboardDetectorCardEntry.Mount(mountCard),
                    DashboardDetectorCardEntry.NativeRoot(nativeRootCard),
                    DashboardDetectorCardEntry.PlayIntegrityFix(playIntegrityFixCard),
                    DashboardDetectorCardEntry.Selinux(selinuxCard),
                    DashboardDetectorCardEntry.Su(suCard),
                    DashboardDetectorCardEntry.SystemProperties(systemPropertiesCard),
                    DashboardDetectorCardEntry.Tee(teeCard),
                    DashboardDetectorCardEntry.Virtualization(virtualizationCard),
                    DashboardDetectorCardEntry.Zygisk(zygiskCard),
                ),
            ),
            deviceInfoCard = deviceInfoCard,
            isLoading = true,
        )
    }

    fun dangerousAppsCard(): DangerousAppsCardModel {
        return dangerousAppsMapper.map(DangerousAppsReport.loading(DangerousAppsCatalog.targets))
    }

}
