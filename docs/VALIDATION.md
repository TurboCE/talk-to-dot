# Validation — 1.4.0-dev preview

Built on the user-designated Intel MacBook, 2026-11-18. Upstream base: `6fbb5a3bde2fc2ee6a16b0a01a3138210c7b3c4c`.

- GitHub CLI authenticated as **TurboCE** on this MacBook (macOS keyring). Windows task was confirmed stopped, with no fork/push, before fork creation. Existing fork lookup returned 404; created `TurboCE/talk-to-dot`.
- JDK: Eclipse Temurin **17.0.21+9**, macOS x86_64. Android platform **36**, build tools **36.1.0**. All downloaded build tools are isolated under the task’s `.tooling/`, outside the repository. System Java/SDK settings and other projects were not changed.
- `scripts/test.sh`: **27** call-policy checks and **31** service checks using synthetic Android nodes; manifest scope, no uses-permission, backup disabled, Korean/English resource parity and original MIT notice checks passed.
- `scripts/build.sh`: Java compilation, D8 dex conversion, APK packaging, zip alignment and signing verification passed. Development signing only; APK v2 and v3 signatures verified. Release APK checksum and signer certificate fingerprint are provided alongside the downloadable APK.
- `adb devices -l` on this MacBook returned an empty device list. No phone was installed, settings changed or actual call made.

## Not verified

Galaxy Fold7; folded/unfolded layouts; One UI/Bixby/Routines launch action; installed ChatGPT version; menu/sidebar/composer/call/end-call accessibility labels; actual audio or server connection; microphone permission flow; device UI rendering; service restart under Android process management. Call labels remain blank until verified by the user. This is an installable preview, **not an end-to-end verified phone integration**.

## Device acceptance checklist (manual, with the owner present)

1. Record ChatGPT version, UI language, One UI version and Fold7 layout; verify all exact accessibility labels and destination editability.
2. Complete ChatGPT onboarding/microphone permission manually. Configure the shortcut, review the disclosure and enable Accessibility manually.
3. Launch while unlocked and observe: correct Dot → exactly one call click → actual call screen → real audio, checked independently. End the call yourself.
4. Repeat while unfolded/folded; wrong/missing/duplicate call label should cause no click and a timeout. Confirm that a failed call-screen marker never causes a second click.
5. During a pending attempt, relaunch repeatedly and switch apps/lock the phone; verify no unexpected second click or later resumed call. Launch while already on a call; verify no click.
6. Configure the device-provided Bixby/Routines launch-app action and repeat only after manual tests pass. Record results without publishing conversation content.

The old `scripts/device-check.py` has been replaced with a refusal notice because its upstream navigation stress test launched the app and clicked assumed controls. It is not a safe call integration acceptance test.
