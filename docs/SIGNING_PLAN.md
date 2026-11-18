# Signing candidate and upgrade boundary

The published preview and installed Fold7 still use the retained development identity:

`e96ef9f495a91c244cca47fe553d619263f2b1ad1e6cf79398b9348ee5e5b1fd`

A separate RSA3072 release key and proof-of-rotation candidate were prepared locally. Candidate public certificate SHA256:

`326c91d8ef45720b9842eafcdc0f00d1e2317188b4797eb35f1d9d91b81a46ec`

No device signer was changed. No private key, password, backup archive or rotation candidate APK was uploaded. The current local candidate was regenerated from code19 using backup-restored keys and retains the development version; it is not a published stable build.

## Verified locally

- APK v2/v3/v3.1 verification passes. API26–32 retain the previous signer; API33–37 select the new signer. This follows [Android's supported signing rotation mechanism](https://developer.android.com/tools/apksigner).
- Installed-data capability is enabled in the lineage. Rollback to newer old-key-only builds is disabled; deployment therefore needs retained new-key ownership.
- The old key was rewrapped with a strong password without changing its identity. An archive containing strong-password-encrypted old/new keys, public lineage and recovery instructions was restored in a local rehearsal. Signing from the restored keys succeeded and the resulting candidate passed API26–32 and API33–37 verification.
- The local archive is a recovery rehearsal, not proof of an independent/off-device backup. No on-device rotated-signature upgrade has been attempted, so data-preserving migration remains a device acceptance item.

## Reproducible local signing

`scripts/sign-rotation-candidate.sh ALIGNED_UNSIGNED_APK OUTPUT_APK` signs a separate local output, verifies it, and never installs/uploads it. It requires `ANDROID_SDK_ROOT`, optional `JAVA_HOME`, `LEGACY_KEYSTORE_PATH`, `RELEASE_KEYSTORE_PATH`, `SIGNING_LINEAGE_PATH`, and password environment variables `DOT_LEGACY_STOREPASS` / `DOT_RELEASE_STOREPASS`. Aliases default to `androiddebugkey` / `local`; `LEGACY_KEY_ALIAS` can select the protected archive's `legacy` alias. Never commit passwords or keys.

Use `build/aligned.apk` from the ordinary build as input. Keep the current signed preview and existing keys unchanged. The helper rejects the same input/output path and refuses debuggable APKs. APK contents/version are not upgraded by signing alone.

Before deployment, the owner needs a secure independent copy of the encrypted-key backup and separately retained password, followed by a coordinated no-call upgrade/data-retention test. Existing test APKs cannot simply be re-signed with an unrelated key and installed with `-r`. Never uninstall or clear data to bypass a mismatch. Stable publication, any CI-secret provisioning and final version/tag changes remain separate unfinished steps.
