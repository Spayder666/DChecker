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

package com.dchecker.info.features.virtualization.detector

import android.content.Context
import com.dchecker.info.core.detector.Detector
import com.dchecker.info.core.detector.DetectorScanner
import com.dchecker.info.core.detector.DetectorSpecificApi
import com.dchecker.info.core.evidence.DetectorId
import com.dchecker.info.core.report.DetectorReport
import com.dchecker.info.features.virtualization.data.repository.VirtualizationRepository
import com.dchecker.info.features.virtualization.domain.VirtualizationReport
import com.dchecker.info.features.virtualization.presentation.VirtualizationCardModelMapper
import com.dchecker.info.features.virtualization.presentation.model.VirtualizationCardModel
import com.dchecker.info.features.virtualization.presentation.toDetectorReport

/**
 * Virtualization: looks for emulators, virtual machines and app virtualization through build
 * and emulator properties, services, dex paths, UID identity, host apps, helper and isolated
 * processes, and native timing and trap probes.
 *
 * Collected by [VirtualizationRepository], judged by `VirtualizationReport.toDetectorStatus()`
 * in the domain layer, and described by [VirtualizationCardModelMapper].
 */
@DetectorSpecificApi
public object VirtualizationDetector : Detector<VirtualizationReport, VirtualizationCardModel> {
    override val id: DetectorId = DetectorId("virtualization")

    override fun createScanner(context: Context): DetectorScanner<VirtualizationReport> = VirtualizationRepository(context)

    override fun loadingReport(): VirtualizationReport = VirtualizationReport.loading()

    override fun describe(report: VirtualizationReport): VirtualizationCardModel = VirtualizationCardModelMapper().map(report)

    override fun export(model: VirtualizationCardModel): DetectorReport = model.toDetectorReport()
}
