# Gallery v9 — P0 data-safety design

Date: 2026-11-02. Base: `779aba964c2b91be9428d5e6400e7756ed881d0f` on `source/blackserv-gallery`.

## Goal
No existing photo/video is deleted or overwritten until a complete, independently verified new copy is durable and discoverable. Fail closed if a provider, storage or database operation fails.

## Scope and invariants
1. Vault hide: verify copied byte count and SHA-256 read-back, insert persistent vault record, then attempt original deletion. Preserve original on any earlier failure. Never purge Room rows without successful hide.
2. Vault unhide: keep vault file and vault record intact until MediaStore write, read-back and publish succeed. Failures remove only the new pending destination.
3. Auto trash: expiration is based on deletion time, not original media modification time; worker must not bypass platform permissions with raw File.delete.
4. Editor: no destructive overwrite before staging/verifying output; correctly retain format/metadata or explicitly refuse unsupported cases.
5. No automatic cloud delete, no refactor of People, no new APK/install as part of this change.

## Design calls
- Pure JVM-testable `VaultCopyVerifier.copyAndVerify(source, destination, readBack, expectedBytes)` owns stream lifecycle and compares SHA-256.
- Existing Room/MediaStore/WorkManager are retained; no new framework.
- Android MediaStore and Room are not one transaction. On ambiguous failure, retain extra copies rather than deleting the last recoverable copy.
- Separate small RED→GREEN patches, each verified by existing GitHub PR CI: `:app:testDebugUnitTest :app:assembleDebug`.

## Release safety
Work on `fix/gallery-v9-data-safety-20261102`, draft PR only; no merge, signed release, data migration or device deploy until automated tests and real-device smoke checks pass.
