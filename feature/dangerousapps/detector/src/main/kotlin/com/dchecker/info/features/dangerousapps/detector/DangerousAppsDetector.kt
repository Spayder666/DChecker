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

package com.dchecker.info.features.dangerousapps.detector

import android.content.Context
import com.dchecker.info.core.detector.Detector
import com.dchecker.info.core.detector.DetectorScanner
import com.dchecker.info.core.detector.DetectorSpecificApi
import com.dchecker.info.core.evidence.DetectorId
import com.dchecker.info.core.report.DetectorReport
import com.dchecker.info.features.dangerousapps.data.repository.DangerousAppsRepository
import com.dchecker.info.features.dangerousapps.domain.DangerousAppsCatalog
import com.dchecker.info.features.dangerousapps.domain.DangerousAppsReport
import com.dchecker.info.features.dangerousapps.presentation.DangerousAppsCardModelMapper
import com.dchecker.info.features.dangerousapps.presentation.model.DangerousAppsCardModel
import com.dchecker.info.features.dangerousapps.presentation.toDetectorReport

/**
 * Dangerous Apps: checks whether known root, hooking and tampering apps are installed, using
 * the package inventory and side channels that reveal packages the inventory cannot see.
 *
 * Collected by [DangerousAppsRepository], judged by `DangerousAppsReport.toDetectorStatus()` in
 * the domain layer, and described by [DangerousAppsCardModelMapper].
 */
@DetectorSpecificApi
public object DangerousAppsDetector : Detector<DangerousAppsReport, DangerousAppsCardModel> {
    override val id: DetectorId = DetectorId("dangerous_apps")

    override fun createScanner(context: Context): DetectorScanner<DangerousAppsReport> = DangerousAppsRepository(context)

    override fun loadingReport(): DangerousAppsReport = DangerousAppsReport.loading(DangerousAppsCatalog.targets)

    override fun describe(report: DangerousAppsReport): DangerousAppsCardModel = DangerousAppsCardModelMapper().map(report)

    override fun export(model: DangerousAppsCardModel): DetectorReport = model.toDetectorReport()
}
