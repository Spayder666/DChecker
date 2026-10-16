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

package com.dchecker.info.features.mount.detector

import android.content.Context
import com.dchecker.info.core.detector.Detector
import com.dchecker.info.core.detector.DetectorScanner
import com.dchecker.info.core.detector.DetectorSpecificApi
import com.dchecker.info.core.evidence.DetectorId
import com.dchecker.info.core.report.DetectorReport
import com.dchecker.info.features.mount.data.repository.MountRepository
import com.dchecker.info.features.mount.domain.MountReport
import com.dchecker.info.features.mount.presentation.MountCardModelMapper
import com.dchecker.info.features.mount.presentation.model.MountCardModel
import com.dchecker.info.features.mount.presentation.toDetectorReport

/**
 * Mount: looks for mount namespace anomalies through native mount table, filesystem and path
 * checks, concealed shell temporary files, an isolated process's mount view and a native
 * isolated service.
 *
 * Collected by [MountRepository], judged by `MountReport.toDetectorStatus()` in the domain
 * layer, and described by [MountCardModelMapper].
 */
@DetectorSpecificApi
public object MountDetector : Detector<MountReport, MountCardModel> {
    override val id: DetectorId = DetectorId("mount")

    override fun createScanner(context: Context): DetectorScanner<MountReport> = MountRepository(context)

    override fun loadingReport(): MountReport = MountReport.loading()

    override fun describe(report: MountReport): MountCardModel = MountCardModelMapper().map(report)

    override fun export(model: MountCardModel): DetectorReport = model.toDetectorReport()
}
