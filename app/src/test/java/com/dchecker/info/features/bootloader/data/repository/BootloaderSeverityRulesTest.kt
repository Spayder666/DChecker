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

package com.dchecker.info.features.bootloader.data.repository

import com.dchecker.info.features.bootloader.domain.BootloaderFindingSeverity
import com.dchecker.info.features.tee.data.verification.certificate.CertificateTrustResult
import com.dchecker.info.features.tee.domain.TeeTrustRoot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BootloaderSeverityRulesTest {

    @Test
    fun `zero length trust chain maps to danger`() {
        val severity = BootloaderSeverityRules.trustRootSeverity(
            CertificateTrustResult(
                trustRoot = TeeTrustRoot.UNKNOWN,
                chainLength = 0,
            ),
        )

        assertEquals(BootloaderFindingSeverity.DANGER, severity)
    }

    @Test
    fun `unknown trust root maps to danger even when signatures are otherwise clean`() {
        val severity = BootloaderSeverityRules.trustRootSeverity(
            CertificateTrustResult(
                trustRoot = TeeTrustRoot.UNKNOWN,
                chainLength = 2,
                chainSignatureValid = true,
            ),
        )

        assertEquals(BootloaderFindingSeverity.DANGER, severity)
    }

    @Test
    fun `key pair generation failure is treated as critical attestation failure`() {
        assertTrue(
            BootloaderSeverityRules.isKeyPairGenerationFailure(
                "failed to generate a key pair"
            ),
        )
        assertFalse(
            BootloaderSeverityRules.isKeyPairGenerationFailure(
                "Attestation collection failed"
            ),
        )
    }
}
