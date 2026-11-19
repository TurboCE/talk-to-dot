# Current release status — 닷 음성 / Voice to Dot

Current release candidate: **1.4.15-dev / versionCode22**, [release notes](releases/call-preview-1.4.15.md). Source runtime commit: `4f54260`. The user explicitly requested publication of this working build. It is published as a prerelease because the remaining stable-release gates below are not all complete.

- Fold7 owner confirmed the Bixby full flow after the previous-conversation navigation fix, then confirmed success in the Gemini Korean app-name trial. See [bounded evidence](FOLD7_EVIDENCE.md).
- 468 synthetic assertions pass, plus manifest/privacy/locale/MIT gates; APK build and v2/v3 signature verification pass. These are not device proof of all safety scenarios.
- Same APK was installed with data retained. It uses the retained development signer; no key rotation, uninstall or data clearing occurred. CI-generated development APKs may use a different key.

## Remaining stable-release gates

1. Both physical Fold layouts and equivalent English provider UI acceptance.
2. Remaining device safety/lifecycle acceptance: cold process startup, repeated invocation, departure/interruption, mid-attempt revocation and lock refusal. Synthetic coverage does not replace these device tests. New live calls require advance notice.
3. Independent secure signing-key backup and coordinated data-preserving production-signing migration acceptance; see [signing plan](SIGNING_PLAN.md).

Work remains on `fix/review-safety` / PR1 targeting main. Publishing this prerelease does not imply that main is merged or all stable gates passed. No private device identifiers, raw conversation dumps or signing keys are included.
