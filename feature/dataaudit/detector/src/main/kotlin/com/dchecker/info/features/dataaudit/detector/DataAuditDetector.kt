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

package com.dchecker.info.features.dataaudit.detector

import android.content.Context
import com.dchecker.info.core.detector.Detector
import com.dchecker.info.core.detector.DetectorScanner
import com.dchecker.info.core.detector.DetectorSpecificApi
import com.dchecker.info.core.evidence.DetectorId
import com.dchecker.info.core.report.DetectorReport
import com.dchecker.info.features.dataaudit.data.DataAuditRepository
import com.dchecker.info.features.dataaudit.domain.DataAuditReport
import com.dchecker.info.features.dataaudit.presentation.DataAuditCardModel
import com.dchecker.info.features.dataaudit.presentation.toCardModel
import com.dchecker.info.features.dataaudit.presentation.toDetectorReport

/**
 * Data & Identity Audit: reads device identifiers, telephony, network surfaces, advertising keys,
 * package visibility, and the process environment through every vantage the sandbox allows, and
 * checks that surfaces the sandbox must keep closed stay closed.
 *
 * Collected by [DataAuditRepository], described by the presentation mapper in the data layer.
 */
@DetectorSpecificApi
public object DataAuditDetector : Detector<DataAuditReport, DataAuditCardModel> {
    override val id: DetectorId = DetectorId("data_audit")

    override fun createScanner(context: Context): DetectorScanner<DataAuditReport> =
        DataAuditRepository(context)

    override fun loadingReport(): DataAuditReport = DataAuditReport.loading()

    override fun describe(report: DataAuditReport): DataAuditCardModel = report.toCardModel()

    override fun export(model: DataAuditCardModel): DetectorReport = model.toDetectorReport()
}
