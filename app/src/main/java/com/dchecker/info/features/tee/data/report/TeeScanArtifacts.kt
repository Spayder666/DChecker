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

package com.dchecker.info.features.tee.data.report

import com.dchecker.info.features.tee.data.attestation.AttestationSnapshot
import com.dchecker.info.features.tee.data.native.NativeTeeSnapshot
import com.dchecker.info.features.tee.data.verification.boot.BootConsistencyResult
import com.dchecker.info.features.tee.data.verification.certificate.ChainStructureResult
import com.dchecker.info.features.tee.data.verification.certificate.CertificateTrustResult
import com.dchecker.info.features.tee.data.verification.certificate.DualAlgorithmChainResult
import com.dchecker.info.features.tee.data.verification.crl.CrlStatusResult
import com.dchecker.info.features.tee.data.verification.keystore.IdAttestationResult
import com.dchecker.info.features.tee.data.verification.keystore.AesGcmRoundTripResult
import com.dchecker.info.features.tee.data.verification.keystore.BinderChainConsistencyResult
import com.dchecker.info.features.tee.data.verification.keystore.BinderHookBootstrapResult
import com.dchecker.info.features.tee.data.verification.keystore.BinderPatchModeResult
import com.dchecker.info.features.tee.data.verification.keystore.BiometricTeeIntegrationResult
import com.dchecker.info.features.tee.data.verification.keystore.ImportKeyRetainedAttestationNarrativeResult
import com.dchecker.info.features.tee.data.verification.keystore.KeyboxImportResult
import com.dchecker.info.features.tee.data.verification.keystore.Keystore2GenerateModeParcelFingerprintResult
import com.dchecker.info.features.tee.data.verification.keystore.Keystore2HookResult
import com.dchecker.info.features.tee.data.verification.keystore.Keystore2PostProcessingResult
import com.dchecker.info.features.tee.data.verification.keystore.KeyLifecycleResult
import com.dchecker.info.features.tee.data.verification.keystore.KeyMintCapabilityResult
import com.dchecker.info.features.tee.data.verification.keystore.GrantDomainFullChainSplitResult
import com.dchecker.info.features.tee.data.verification.keystore.GrantSelfDomainFullChainSplitResult
import com.dchecker.info.features.tee.data.verification.keystore.SyntheticGrantGetKeyEntryAccessVectorBlindnessResult
import com.dchecker.info.features.tee.data.verification.keystore.SyntheticGrantGranteeBlindReadbackResult
import com.dchecker.info.features.tee.data.verification.keystore.LegacyKeystorePathResult
import com.dchecker.info.features.tee.data.verification.keystore.ListEntriesBatchedResult
import com.dchecker.info.features.tee.data.verification.keystore.ListEntriesConsistencyResult
import com.dchecker.info.features.tee.data.verification.keystore.KeyMetadataSemanticsResult
import com.dchecker.info.features.tee.data.verification.keystore.KeyMetadataShapeResult
import com.dchecker.info.features.tee.data.verification.keystore.KeyPairConsistencyResult
import com.dchecker.info.features.tee.data.verification.keystore.OperationErrorPathResult
import com.dchecker.info.features.tee.data.verification.keystore.OperationPruningResult
import com.dchecker.info.features.tee.data.verification.keystore.OversizedChallengeResult
import com.dchecker.info.features.tee.data.verification.keystore.PureCertificateResult
import com.dchecker.info.features.tee.data.verification.keystore.PureCertificateSecurityLevelResult
import com.dchecker.info.features.tee.data.verification.keystore.SupplementaryAttestationInfoResult
import com.dchecker.info.features.tee.data.verification.keystore.TimingAnomalyResult
import com.dchecker.info.features.tee.data.verification.keystore.TimingSideChannelResult
import com.dchecker.info.features.tee.data.verification.keystore.UpdateSubcomponentResult
import com.dchecker.info.features.tee.data.verification.keystore.UpdateSubcomponentStaleResponsePersistenceResult
import com.dchecker.info.features.tee.data.verification.keystore.VintfKeyMintVersionResult
import com.dchecker.info.features.tee.data.verification.strongbox.StrongBoxBehaviorResult
import com.dchecker.info.features.tee.data.verification.rkp.RkpProvisionedManufacturerResult
import com.dchecker.info.features.tee.domain.TeeRkpState
import com.dchecker.info.features.tee.domain.TeeSoterState

data class TeeScanArtifacts(
    val snapshot: AttestationSnapshot,
    val trust: CertificateTrustResult,
    val chainStructure: ChainStructureResult,
    val rkp: TeeRkpState,
    val crl: CrlStatusResult,
    val pairConsistency: KeyPairConsistencyResult,
    val aesGcm: AesGcmRoundTripResult,
    val lifecycle: KeyLifecycleResult,
    val keyMintCapability: KeyMintCapabilityResult,
    val timing: TimingAnomalyResult,
    val timingSideChannel: TimingSideChannelResult,
    val oversizedChallenge: OversizedChallengeResult,
    val keyboxImport: KeyboxImportResult,
    val importKeyRetainedAttestationNarrative: ImportKeyRetainedAttestationNarrativeResult,
    val supplementaryAttestationInfo: SupplementaryAttestationInfoResult,
    val vintfKeyMintVersion: VintfKeyMintVersionResult,
    val keystore2Hook: Keystore2HookResult,
    val generateModeParcelFingerprint: Keystore2GenerateModeParcelFingerprintResult,
    val postProcessing: Keystore2PostProcessingResult,
    val rkpProvisionedManufacturer: RkpProvisionedManufacturerResult,
    val grantDomainFullChainSplit: GrantDomainFullChainSplitResult,
    val syntheticGrantGranteeBlindReadback: SyntheticGrantGranteeBlindReadbackResult,
    val syntheticGrantGetKeyEntryAccessVectorBlindness: SyntheticGrantGetKeyEntryAccessVectorBlindnessResult,
    val grantSelfDomainFullChainSplit: GrantSelfDomainFullChainSplitResult,
    val legacyKeystorePath: LegacyKeystorePathResult,
    val listEntriesConsistency: ListEntriesConsistencyResult,
    val listEntriesBatched: ListEntriesBatchedResult,
    val keyMetadataSemantics: KeyMetadataSemanticsResult,
    val keyMetadataShape: KeyMetadataShapeResult,
    val pureCertificate: PureCertificateResult,
    val pureCertificateSecurityLevel: PureCertificateSecurityLevelResult,
    val operationErrorPath: OperationErrorPathResult,
    val biometricIntegration: BiometricTeeIntegrationResult,
    val binderHookBootstrap: BinderHookBootstrapResult,
    val binderPatchMode: BinderPatchModeResult,
    val binderChainConsistency: BinderChainConsistencyResult,
    val updateSubcomponent: UpdateSubcomponentResult,
    val updateSubcomponentStaleResponsePersistence: UpdateSubcomponentStaleResponsePersistenceResult,
    val pruning: OperationPruningResult,
    val dualAlgorithm: DualAlgorithmChainResult,
    val idAttestation: IdAttestationResult,
    val strongBox: StrongBoxBehaviorResult,
    val native: NativeTeeSnapshot,
    val soter: TeeSoterState,
    val bootConsistency: BootConsistencyResult,
)
