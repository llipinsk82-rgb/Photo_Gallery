# Gallery v9 P0 Data Safety Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Protect the only copy of photos during Vault hide/restore, trash cleanup and editor save.
**Architecture:** Add a small pure Kotlin verified-copy helper, integrate fail-closed MediaStore operations, and isolate retention metadata from original timestamps. Never delete the source until the destination has been verified.
**Tech Stack:** Android Kotlin, Room, MediaStore, WorkManager, JUnit4, existing Gradle GitHub CI.
**Spec:** `docs/superpowers/specs/2026-11-02-gallery-v9-data-safety-design.md`

## Global Constraints
- Start at `779aba964c2b91be9428d5e6400e7756ed881d0f`; branch `fix/gallery-v9-data-safety-20261102`.
- TDD RED→GREEN through the existing draft PR workflow: `:app:testDebugUnitTest :app:assembleDebug`.
- Never touch `source/blackserv-gallery`, release workflow, or installed APK during this plan.
- No additional build dependencies, no new Android permissions.
- Preserve all originals on unknown or partial writes; retain extra copies on ambiguous failures.
- No Cloud delete, People rescan, or unrelated UI work in this plan.

## Review Focus
- Null source/output stream → no original deletion (Tasks 1–3).
- Partial input/output bytes → verification failure and source kept (Tasks 1–3).
- Corruption without length change → SHA-256 read-back rejects (Tasks 1–3).
- Old photo freshly trashed → full configured retention period (Task 4).
- JPEG export to PNG or failed compress → never truncate original (Task 5).

---

### Task 1: Verified streaming copy (pure Kotlin)

**Files:**
- Create: `app/src/test/java/com/inferno/gallery/data/VaultCopyVerifierTest.kt`
- Create: `app/src/main/java/com/inferno/gallery/data/VaultCopyVerifier.kt`

**Interfaces:**
- Produces: `internal object VaultCopyVerifier` with `fun copyAndVerify(source: () -> InputStream?, destination: () -> OutputStream?, readBack: () -> InputStream?, expectedBytes: Long): Long` (throws `IOException` on null, short, mismatched or unreadable destination).
- Consumes: standard `java.io` stream types only; uses SHA-256 for read-back verification.

- [ ] **Step 1:** Create JUnit tests `fullCopyMatchesSha256`, `nullDestinationFailsClosed`, `truncatedReadBackIsRejected`, `sameSizeCorruptionIsRejected`. Assert return byte count and `IOException` for failures using `ByteArrayInputStream`/`ByteArrayOutputStream`.
- [ ] **Step 2:** Commit test only, open draft PR; CI must fail because helper is missing (RED).
- [ ] **Step 3:** Add minimal helper implementing the declared signature; close stream before verifying read-back.
- [ ] **Step 4:** Commit helper and require draft PR `test-and-debug-build` PASS (GREEN).

### Task 2: Vault restore safety

**Files:**
- Modify: `app/src/main/java/com/inferno/gallery/data/VaultRepository.kt`
- Test: `app/src/test/java/com/inferno/gallery/data/VaultCopyVerifierTest.kt` plus targeted restore-state tests if extractable.

**Interfaces:**
- Consumes Task 1 `VaultCopyVerifier.copyAndVerify`.
- Produces: `unhideMedia` preserving vault file and DAO row until verified copy, successful `IS_PENDING` publish, and no exception.

- [ ] **Step 1:** Add regression test of restore decision gate (null/short/corrupt output never invokes cleanup).
- [ ] **Step 2:** Commit tests and confirm CI RED for the missing safety behavior.
- [ ] **Step 3:** Replace nullable optional output write with verified copy; on failure best-effort delete only destination pending URI, retain vault file and DB record; check publish result.
- [ ] **Step 4:** Require CI GREEN and review fail-safe behavior.

### Task 3: Vault hide safety

**Files:**
- Modify: `app/src/main/java/com/inferno/gallery/data/VaultRepository.kt`
- Test: `app/src/test/java/com/inferno/gallery/data/VaultCopyVerifierTest.kt` and focused hide state tests.

**Interfaces:**
- Consumes Task 1 verifier.
- Produces: `hideMedia` with verified file, persisted DAO row before deleting original, and Room cleanup only for confirmed hide.

- [ ] **Step 1:** Regression tests covering copied file corruption, no source stream, and DB persistence failure not causing source deletion.
- [ ] **Step 2:** Confirm RED in draft PR CI.
- [ ] **Step 3:** Implement per-item verified copy and DAO persistence; fail closed; preserve source on any earlier failure.
- [ ] **Step 4:** Require CI GREEN.

### Task 4: Trash retention safety

**Files:**
- Modify: `app/src/main/java/com/inferno/gallery/workers/AutoCleanTrashWorker.kt`, `GalleryViewModel.kt`, MediaStore sync/DAO migration if required.
- Test: new `TrashRetentionPolicyTest.kt` and migration tests if schema changes.

**Interfaces:** Uses deletion/trashed-at metadata (not `dateModified`), preserves platform MediaStore deletion permission semantics.

- [ ] **Step 1:** Test old photo trashed today and recent photo trashed long ago, both with clock injection.
- [ ] **Step 2:** Confirm CI RED.
- [ ] **Step 3:** Implement actual trash date handling; remove direct raw-file delete; disable unsafe worker rather than guessing if Android cannot supply trustworthy data.
- [ ] **Step 4:** Require CI GREEN.

### Task 5: Editor save safety

**Files:**
- Modify: `app/src/main/java/com/inferno/gallery/ui/ImageEditorScreen.kt`
- Test: new unit tests for output decision/format policy.

**Interfaces:** Save copy checked first; overwrite only after successful staged encode and format/metadata policy.

- [ ] **Step 1:** Test failed encoder and incompatible input format leave original untouched.
- [ ] **Step 2:** Confirm CI RED.
- [ ] **Step 3:** Implement minimum non-destructive path; if safe overwrite unavailable, disable that action with clear user feedback.
- [ ] **Step 4:** Require CI GREEN and inspect complete diff.

### Final verification
- [ ] Run PR CI on final SHA; confirm unit tests and debug APK build.
- [ ] Review complete changes, file ownership, safety invariants and any Room migration.
- [ ] No release or merge without separate real-device checks using a disposable test gallery and explicit approval.
