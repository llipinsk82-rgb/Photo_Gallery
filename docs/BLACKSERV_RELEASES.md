# BlackServ Gallery releases

This is a separate update channel for the public repository
llipinsk82-rgb/Photo_Gallery. The updater only accepts tags shaped like
v3.0.0-blackserv.3 and APK assets named BlackServGallery-*.apk.

## One permanent signing identity

The Android package ID is eu.blackserv.gallery. Every release MUST use the
same private signing key. Never upload .jks, key.properties, signing
passwords, or an OAuth token to Git.

The ONE permanent RSA 3072-bit PKCS12 signing identity has already been
generated and backed up in the PRIVATE repository:

    llipinsk82-rgb/Secrets/blackserv-gallery/signing/blackserv-gallery.jks

Its credentials are in the same private repository at:

    llipinsk82-rgb/Secrets/blackserv-gallery/signing/credentials.env

Do NOT generate another key for future builds. Keep the Secrets repository
private and restrict its collaborators. Because the encrypted key and its
password are backed up in the same private repository, anyone who can read
that repository can sign APKs. Keep a second encrypted offline backup.

Set the following GitHub Actions repository secrets (not repository variables):

- RELEASE_KEYSTORE_BASE64 — base64-encoded complete blackserv-gallery.jks
- KEYSTORE_PASSWORD
- KEY_ALIAS — typically blackserv-release
- KEY_PASSWORD

For local signed builds, place blackserv-gallery.jks in the project root and
create the ignored key.properties containing the same four corresponding
properties (storeFile=blackserv-gallery.jks, storePassword, keyAlias,
keyPassword). Never commit these files.

./gradlew assembleRelease fails when key.properties is absent. CI also
fails when any required signing secret is missing. Do not use a debug key as a
release signing identity.

## First migration

Older v3.0.0-blackserv.2 experimental APKs were signed with debug keys. They
cannot be updated in place by APKs signed with the permanent release key.
Before installing the first permanently signed release, the user must first
confirm that Private Space is empty or export any photos from it, then
uninstall the old BlackServ Gallery and install the new APK once. From then
on, each update should install over the previous release without uninstalling.

## Publishing

1. Commit changes to source/blackserv-gallery.
2. Verify unit tests, the APK package name, versionCode, and signer certificate.
3. Create a tag on that branch: git tag v3.0.0-blackserv.3.
4. Push the tag. GitHub Actions checks that it is an ancestor of the
   source/blackserv-gallery branch, signs release APKs, and publishes them.
5. The app's built-in updater checks only this repository's BlackServ releases.

## Google Photos

The Cloud tab uses Android's Embedded Photo Picker (Android 14+ with SDK
extension 15+) or the ordinary system Photo Picker on older systems. The
user's existing Google Photos login is handled by the cloud provider.
BlackServ receives only explicitly selected media URIs; no Google OAuth
client secret, refresh token, or password belongs in the repository.
