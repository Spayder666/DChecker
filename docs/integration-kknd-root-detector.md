# Kknd_Root_Detector — integration plan

Source: https://github.com/juanma0511/Kknd_Root_Detector (MIT). Full sources are public, so a
code port with MIT attribution headers is allowed and keeps DChecker Apache-2.0.

## What it contains

- `app/src/main/cpp/native_root_checks.cpp` (2625 lines) — ~100 named native checks and
  509 unique string literals (paths, sockets, props).
- `app/src/main/cpp/native_checks.cpp` (312 lines) — JNI table hooks, Frida descriptors,
  anonymous executable pages, PTY fds.
- Kotlin detectors: `RootDetector` (2795 lines — the Java-side matrix), `HwSecurityDetector`
  (470 — hardware security posture), `KeyAttestationChecker` (463 — per-key attestation
  verdicts), `IntegrityChecker` (201), `DetectorTrust` (208 — self-integrity of the checker),
  `AdvancedRuntimeDetector` (91), `NativeChecks` (116), `MountNamespaceDetector`,
  `OverlayFSDetector`, `ZygiskDetector`, `GetPropCatalog`, `HardcodedSignals`,
  `DirtySepolicyClient`.

## Coverage against current DChecker

Already covered by existing modules (no port needed):

- Zygisk/LSPosed/Shamiko/Riru artifacts and sockets → `feature/zygisk`, `feature/lsposed`
- KernelSU supercall, SUSFS nodes, kernel-patch latency → `feature/nativeroot`
  (`ksu_supercall_probe`, `susfs_probe`, `kernelpatch_*`)
- Magisk overlay/mirror mounts, suspicious bind mounts, mount namespace divergence →
  `feature/mount`, `MountNamespaceDriftProbe`
- SELinux permissive / dirty sepolicy → `feature/selinux`, `DirtySepolicyClient` equivalent in
  selinux probes
- su directory structure, running as root, root processes → `feature/su`, `feature/nativeroot`
  (`path_probe`, `process_probe`, `cgroup_process_probe`)
- Kernel custom builds / boot params / newer-than-system kernel → `feature/kernelcheck`
- Traced process, syscall timing hook, JNI table, Frida → `feature/memory`
  (`function_hook_detector`, `fd_detector`, `signal_detector`) and `nativeroot`
  (`kernelpatch_nr_supercall_latency_probe`, `prctl_probe`)
- Suspicious PATH / CLASSPATH env → dataaudit `PATH environment`

Genuinely new checks worth porting (not covered today):

1. Setuid-root binary scan (`Unexpected Setuid Binaries`, `Setuid Root Binary`) — walk
   PATH + /system/x|bin for mode & 04000.
2. User CA certificates installed (`User CA Certificates Installed`) —
   `KeyStore`/`CA store` user cert presence (settings + KeyChain probe).
3. SSL proxy port listening (`SSL Proxy Port Detected`) — connect to 8080/8888/9090/… loopback.
4. High file descriptor count / PTY file descriptor open — `/proc/self/fd` scan.
5. Houdini native bridge / virtual architecture — `ro.native_bridge`, `/system/lib/libhoudini*`,
   `/proc/self/maps` houdini lines.
6. SOTER group (SOTER service absent / hook in maps / bypass module) — Tencent SOTER surface.
7. Native bridge enabled property + Houdini — small property pair check.
8. `HwSecurityDetector` hardware-security posture screen — evaluate as a separate card later.

## Port order (next session)

1. Port the 8 missing checks as one native unit addition:
   `feature/nativeroot/data/src/main/cpp/nativeroot/probes/reveny_missing.cpp` — wait, the
   source is Kknd (MIT by juanma0511), keep attribution:
   `knd_missing_checks.cpp` with `/* MIT (c) juanma0511, Kknd_Root_Detector */` header.
2. Register in `capability`-style CMake include of the nativeroot unit.
3. Kotlin side: surface each as a signal row in `feature/nativeroot` report.
4. `HwSecurityDetector` port → candidate new `feature/hwsecurity` card.
5. Re-record dashboard export golden; rebuild; install; verify.
