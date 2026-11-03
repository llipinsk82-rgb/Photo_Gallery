# Gallery Face AI Diagnostics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make losses in the on-device face-scanning pipeline measurable without altering recognition behavior.
**Architecture:** A pure Kotlin per-photo outcome accumulator is called from `FaceScanHelper`. Android logging reports only aggregate counts and a local media ID.
**Tech Stack:** Kotlin, JUnit4, Android ML Kit FaceDetection, existing GitHub CI.
**Spec:** `docs/superpowers/specs/2026-11-03-gallery-face-diagnostics-design.md`

## Global Constraints
- Keep Draft PR #6; do not build or publish a signed APK.
- Never send photo data, embeddings or names off the device.
- Preserve existing face matching, retry, and database semantics.
- CI command `./gradlew :app:testDebugUnitTest :app:assembleDebug --console=plain`.

## Review Focus
- ML Kit reports no faces: counters remain zero, no fabricated loss.
- Two people in one photo: outcomes counted separately, without overwriting each other.
- One face is rejected by pose after another was saved: both outcomes retained.
- Scan interrupted after some faces: incomplete classified count is explicit.
- Error path: existing retry/error semantics remain unchanged.

---

### Task 1: Face scan diagnostic counter and integration

**Files**
- Create `app/src/main/java/com/inferno/gallery/data/ai/FaceScanDiagnostics.kt`
- Create `app/src/test/java/com/inferno/gallery/data/ai/FaceScanDiagnosticsTest.kt`
- Modify `app/src/main/java/com/inferno/gallery/data/ai/FaceScanHelper.kt`

**Interfaces**
- `FaceScanDiagnostics(totalDetected: Int)`
- `record(FaceScanOutcome)`; `summary(mediaId: Long): String`; `categorizedCount`; `uncategorizedCount`.
- Outcomes: `TOO_SMALL`, `POSE_FILTERED`, `EXISTING`, `ALIGNMENT_FAILED`, `EMBEDDING_FAILED`, `SAVED`.

- [ ] Step 1: Add JUnit tests with literal expected counters for zero-face, group six-outcome, and incomplete scan.
- [ ] Step 2: Run CI on test-only commit. Expected: RED because diagnostic API is missing.
- [ ] Step 3: Add minimal pure Kotlin accumulator and integrate one recorded outcome per ML Kit face, preserving current processing branches. Log local summary when scan completes.
- [ ] Step 4: Run CI. Expected: complete unit suite PASS and debug compile PASS.
- [ ] Step 5: Review git diff, audit that no logging contains names/photos/embeddings, commit/push to existing Draft PR, leave follow-up issues open.
