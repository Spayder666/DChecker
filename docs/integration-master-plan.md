# DChecker — master integration plan (all-layers checker)

Goal: absorb every open-source detection technique available, centralize cross-validation
between detectors, and unify presentation. Executed incrementally per source; this file is
the working map.

## Sources and status

| Source | License | Status |
|---|---|---|
| VD-Infos (identifiers/telephony/network/sealed) | AGPL | DONE — reimplemented natively (`feature/dataaudit`), 40+ points |
| Kknd_Root_Detector (~100 native checks) | MIT | MAPPED — port the 8 missing checks next (setuid scan, user CA certs, SSL proxy ports, fd/PTY scans, Houdini bridge, SOTER group, native-bridge props, hwsecurity card) |
| reveny/Android-Native-Root-Detector | MIT, native closed | Techniques covered; nothing to copy |
| RootBeer (Java) | Apache-2.0 | BACKLOG — port missing: `su` binary exec probe, busybox presence, dangerous props table, test-signature detection |
| MomoDetector / NeoDetector-style repos | mixed | BACKLOG — maps needle set (injection_maps_needles equivalent), lsposed manager packages, apatch extras |
| TrickyStore / PlayIntegrityFix public docs | mixed | covered by `feature/playintegrityfix`, `feature/tee` |

## Cross-validation engine (next major piece)

New `core/correlate` module:

- Input: every detector's report (typed) after a scan.
- Rules (first batch):
  1. KSU supercall positive (nativeroot) + KernelSU package (rootmanagers) ⇒ escalate to
     DANGER with "confirmed by two independent layers".
  2. dataaudit PATH mismatch + nativeroot magisk-tmp artifact ⇒ "magisk-tmp confirmed".
  3. dataaudit sealed /proc/cmdline SEALED_VIOLATED + systemproperties boot-param
     contradiction ⇒ "boot state rewritten".
  4. Package count mismatch (dataaudit) + dangerous-apps hit ⇒ "app-hiding module active".
  5. Full property audit mismatch on ro.boot.* + bootloader unlocked signal ⇒ "boot chain
     spoof suspected".
- Output: `CorrelationReport` — list of confirmed/escalated/contradicted findings with the
  contributing detector ids; dashboard gains a "Correlations" section; export gains the same.

## UI unification plan

1. Every card: consistent sections (group rows → scan summary → impact → methods → open
   detail dialog), the systemproperties/dataaudit shape is the reference.
2. Detail dialogs: search + filters + per-lens/per-method values, red divergent values.
3. Empty/error/loading states standardized: Pending / Error / Unavailable rows (exists —
   apply to new cards).
4. New "Correlations" block on dashboard above Top findings.

## Execution order

1. Port 8 Kknd missing checks (native + nativeroot report rows).
2. `core/correlate` engine + dashboard section + export section.
3. RootBeer backlog probes.
4. HwSecurity card.
5. Golden re-record; full test suite; install; on-device verification of every new card.
