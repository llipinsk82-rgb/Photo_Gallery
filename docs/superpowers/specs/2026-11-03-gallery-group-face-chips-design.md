# Group-photo face chips display — design (2026-11-03)

## Observed root cause
The Photo Gallery `FaceDao.observePeopleForMedia` query uses `GROUP BY COALESCE(f.personId,-f.id)`. Two different face rows assigned to the same person are collapsed into one chip in `ExifDetailsSheet`, despite the UI using `faceId` as the stable key. A read-only in-memory SQLite fixture reproduced 3 stored faces → 2 displayed rows.

## Goal and non-goals
- Display one chip for each distinct stored face in a photo, even if two faces have been assigned the same person/cluster. Preserve each `faceId` so Lucas can tell whether Julia and Vanessa were both detected.
- Do NOT auto-rename, merge or split people, alter embeddings, touch the Google Photos cloud, or change ML Kit thresholds.
- Preserve filters for hidden people, vault/trash count logic and existing `ExifDetailsSheet` UI.
- No signed APK release until device smoke.

## Approaches
- Tighten face clustering: does not fix the query hiding distinct faces and risks changing identities.
- Change UI to reconstruct missing faces: impossible because DAO already collapses rows.
- **Chosen:** remove SQL GROUP BY; each face row is already unique by `f.id`. Add a fixture that executes the exact DAO SQL against SQLite so behavior remains tested.

## Privacy and safety
No photo contents, person images, names or embeddings leave the app; fixture uses synthetic IDs only. Removing the grouping is read-only. It does not change persisted face/person assignments.
