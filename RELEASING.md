# TurboCE preview delivery

Build and test using README.md. Retain LICENSE. Never commit keystores, device UI XML or credentials.

This fork's initial downloadable APK is a development-signed prerelease. Use a tag such as `call-preview-1.4.0` rather than an upstream `v*` tag; the inherited `v*` workflow expects a production keystore in GitHub Actions secrets. Upload APK, SHA256SUMS.txt and SIGNING.txt with release notes explicitly stating unverified Fold7/control labels and development signing. Do not claim update compatibility with the upstream APK.

For future production releases, choose and securely retain a separate production key, configure this fork's own signing secrets, test the device behavior, update manifest version and release notes, and then use the inherited signed-release workflow. CI development builds can use different ephemeral debug keys and will not necessarily update the locally prepared preview.
