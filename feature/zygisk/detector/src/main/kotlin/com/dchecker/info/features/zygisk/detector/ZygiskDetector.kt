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

package com.dchecker.info.features.zygisk.detector

import android.content.Context
import com.dchecker.info.core.detector.Detector
import com.dchecker.info.core.detector.DetectorScanner
import com.dchecker.info.core.detector.DetectorSpecificApi
import com.dchecker.info.core.evidence.DetectorId
import com.dchecker.info.core.report.DetectorReport
import com.dchecker.info.features.zygisk.data.repository.ZygiskRepository
import com.dchecker.info.features.zygisk.domain.ZygiskReport
import com.dchecker.info.features.zygisk.presentation.ZygiskCardModelMapper
import com.dchecker.info.features.zygisk.presentation.model.ZygiskCardModel
import com.dchecker.info.features.zygisk.presentation.toDetectorReport

/**
 * Zygisk: looks for Zygisk injection in this process through native linker, maps, file
 * descriptor, thread and heap probes, and a file descriptor trap in a separate
 * process.
 *
 * Collected by [ZygiskRepository], judged by `ZygiskReport.toDetectorStatus()` in the domain
 * layer, and described by [ZygiskCardModelMapper].
 */
@DetectorSpecificApi
public object ZygiskDetector : Detector<ZygiskReport, ZygiskCardModel> {
    override val id: DetectorId = DetectorId("zygisk")

    override fun createScanner(context: Context): DetectorScanner<ZygiskReport> = ZygiskRepository(context)

    override fun loadingReport(): ZygiskReport = ZygiskReport.loading()

    override fun describe(report: ZygiskReport): ZygiskCardModel = ZygiskCardModelMapper().map(report)

    override fun export(model: ZygiskCardModel): DetectorReport = model.toDetectorReport()
}
