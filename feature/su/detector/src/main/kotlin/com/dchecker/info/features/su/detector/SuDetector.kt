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

package com.dchecker.info.features.su.detector

import android.content.Context
import com.dchecker.info.core.detector.Detector
import com.dchecker.info.core.detector.DetectorScanner
import com.dchecker.info.core.detector.DetectorSpecificApi
import com.dchecker.info.core.evidence.DetectorId
import com.dchecker.info.core.report.DetectorReport
import com.dchecker.info.features.su.data.repository.SuRepository
import com.dchecker.info.features.su.domain.SuReport
import com.dchecker.info.features.su.presentation.SuCardModelMapper
import com.dchecker.info.features.su.presentation.model.SuCardModel
import com.dchecker.info.features.su.presentation.toDetectorReport

/**
 * SU: looks for su binaries and root daemons on known paths and in PATH, an abnormal SELinux
 * context of this process, and suspicious processes from a native process scan.
 *
 * Collected by [SuRepository], judged by `SuReport.toDetectorStatus()` in the domain layer, and
 * described by [SuCardModelMapper].
 */
@DetectorSpecificApi
public object SuDetector : Detector<SuReport, SuCardModel> {
    override val id: DetectorId = DetectorId("su")

    override fun createScanner(context: Context): DetectorScanner<SuReport> = SuRepository()

    override fun loadingReport(): SuReport = SuReport.loading()

    override fun describe(report: SuReport): SuCardModel = SuCardModelMapper().map(report)

    override fun export(model: SuCardModel): DetectorReport = model.toDetectorReport()
}
