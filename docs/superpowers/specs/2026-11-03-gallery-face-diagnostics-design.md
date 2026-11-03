# Gallery Face AI scan diagnostics — design (2026-11-03)

## Intent
Lucas sees fewer recognized faces than visible in his photos, especially group pictures. Before changing ML Kit resolution or matching thresholds, record where detections are lost so future fixes are evidence-based.

## Constraints
- Android Gallery repo `llipinsk82-rgb/Photo_Gallery`; work only in isolated Draft PR #6.
- Do not upload images, face crops, embeddings, person names or identifiers to an external service.
- Logging stays on device in Android Logcat; do not persist per-photo telemetry to an analytics provider.
- No change to recognition thresholds, media deletion, cloud, or release APK in this task.
- Preserve existing face/person assignments and v9 retry rules.

## Approaches
1. **Immediately raise scan resolution and relax filtering.** Could find more group faces, but potentially multiplies RAM/CPU and causes incorrect identities; no representative on-device benchmark yet.
2. **Swap ML Kit / recognition model.** Large dependency and privacy/performance risk unrelated to verified failure.
3. **Instrument the current pipeline with local-only counters (chosen).** Low-risk insight into faces detected, filtered by size/pose, deduplicated, extraction-failed, and saved, using the same code path as actual scans.

## Behavior
For each successful ML Kit detection request, count `detected`; every returned face receives at most one terminal outcome: `tooSmall`, `poseFiltered`, `existing`, `alignmentFailed`, `embeddingFailed`, or `saved`. Log a compact summary containing media database ID, not image contents. If processing exits early or throws, do not claim every face was categorized. The existing retry mechanism remains unchanged.

## Acceptance
- Deterministic JVM tests exercise every outcome and an incomplete scan.
- Android unit tests and debug compilation pass on CI.
- Real recall improvement and deletion cleanup remain separate follow-up work requiring device sample/diagnosis; this task explicitly does not claim those are fixed.
