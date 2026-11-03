# Group Photo Face Chips Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans, TDD RED then GREEN.

**Goal:** Preserve all stored face detections as distinct photo-detail chips.
**Architecture:** Remove query-level person grouping from `FaceDao.observePeopleForMedia`; retain the stable `faceId` list keys already used in Compose.
**Tech Stack:** SQLite, Python 3 stdlib unittest (test-only), Room, Kotlin, existing CI.
**Spec:** `docs/superpowers/specs/2026-11-03-gallery-group-face-chips-design.md`

## Global Constraints
- Draft PR #6 only; no release APK or live database writes.
- No changes to ML Kit, clustering, identities, cloud, or person metadata.
- Test SQL semantics against the query actually defined in FaceDao, not a duplicated SQL string.

## Review Focus
- Two faces with the same person ID both appear.
- Another person's face remains present.
- Hidden-person filters still exclude hidden faces.
- Null-person/unassigned face still appears.
- Real app UI keyed by unique faceId must render duplicates separately.

---

### Task 1: Query regression fixture and minimal fix

**Files**
- Create `scripts/tests/test_face_dao_query.py`
- Modify `.github/workflows/verify.yml` (Python stdlib test before Android SDK)
- Modify `app/src/main/java/com/inferno/gallery/data/db/FaceDao.kt`

- [ ] Step 1: Commit synthetic SQLite fixture verifying 3 face rows produce 3 chip rows; integrate cheap test with existing CI.
- [ ] Step 2: Run Python unittest and GitHub CI on test-only commit. Expected RED: actual query returns 2 rows.
- [ ] Step 3: Remove only `GROUP BY COALESCE(f.personId, -f.id)`.
- [ ] Step 4: Run Python unittest plus full Android CI. Expected GREEN: 3 distinct faces, all Android unit tests + debug build PASS.
- [ ] Step 5: Review diff, keep PR Draft and issue open until Android photo UI verified.
