# Gallery People Visibility Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement task-by-task with tests first.

**Goal:** Hide orphaned persons with zero visible photos from the existing Hide/Show Faces sheet without deleting identity metadata.
**Architecture:** Pure Kotlin visibility policy applied in `PeopleViewModel` to the already-computed face-count rows.
**Tech Stack:** Kotlin 2.2, JUnit4, Room Flow, Jetpack Compose.
**Spec:** `docs/superpowers/specs/2026-11-03-gallery-people-visibility-design.md`

## Global Constraints
- Existing Draft PR #6, no merge and no signed APK.
- Preserve person records, names and embeddings; UI-only filtering.
- Reuse the current PeopleViewModel and HideShowFacesSheet.

## Review Focus
- Named orphan still has coverCropPath: it must not be displayed as a ghost face.
- A hidden person with a real visible photo must remain available for unhide.
- Two live persons retain ordering, names and references.
- Trash-only / vault-only results with computed count zero must not display.
- Empty list remains empty.

---

### Task 1: Visibility policy

**Files**
- Create `app/src/main/java/com/inferno/gallery/ui/people/PeopleVisibilityPolicy.kt`
- Create `app/src/test/java/com/inferno/gallery/ui/people/PeopleVisibilityPolicyTest.kt`
- Modify `app/src/main/java/com/inferno/gallery/ui/people/PeopleViewModel.kt`

**Interfaces**
- `internal object PeopleVisibilityPolicy { fun withVisiblePhotos(people: List<PersonEntity>): List<PersonEntity> }`

- [ ] Step 1: Add JUnit tests for named orphan, hidden live person, order preservation, empty input.
- [ ] Step 2: Run tests on test-only branch commit. Expected RED (policy absent).
- [ ] Step 3: Implement `withVisiblePhotos` using `faceCount > 0` and apply it to the existing `allPersonsIncludingHidden` Flow before stateIn. No DB deletes.
- [ ] Step 4: Run complete CI unit tests plus debug compilation. Expected GREEN.
- [ ] Step 5: Read diff, confirm no destructive call, leave Draft PR unmerged pending Android UI smoke test.
