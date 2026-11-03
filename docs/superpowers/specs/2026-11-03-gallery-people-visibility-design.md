# Gallery People visibility after photo deletion (2026-11-03)

## Intent
Lucas reports that faces remain after deleting photos. The Room `faces` table cascades on `core_media` deletion, and the primary People list already excludes people with no accessible media. However, `observeAllPersonsIncludingHidden()` feeds the Hide/Show Faces sheet with **all** person rows even when the computed visible photo count is zero, and those rows retain an old portrait cache path.

## Decision and alternatives
- Hard-delete empty people: would destroy names, favorites, manual merge identity and user metadata. **Rejected**.
- Clear all face indexes on deletion: heavy, destructive and mis-scopes the issue. **Rejected**.
- Filter only zero-visible-photo people from the Hide/Show Faces sheet while retaining underlying person metadata. **Chosen**.

## Scope and constraints
- Use existing Room-computed `faceCount` from the `observeAllPersonsIncludingHidden` query (counts non-Trash, non-vault photos).
- Keep visible or hidden people with at least one visible photo; keep list order.
- Never delete faces, persons, crops, media, or identities in this task. Existing vault and trash behavior stays unchanged.
- No new UI styling/components; filter the existing ViewModel flow and reuse the existing sheet.
- Complete with JVM unit test, CI debug compile, and on-device smoke eventually; no APK publishing.
