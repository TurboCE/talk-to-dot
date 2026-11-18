# Release readiness — Voice to Dots

Current source: 1.4.5-dev, versionCode 12, built and installed on the identified Fold7. This is still an independent MIT fork of Talk to Dot. The published 1.4.0 prerelease predates safety review fixes and must not be presented as the fixed build.

## Completed

- Fixes for refreshed control identity/label, process/reconnect cancellation, missing-tree grace, resolved-label collisions, authorization revocation, structural state evidence, stable persisted outcomes and ambiguous duplicate state controls.
- 27 policy, 31 baseline service, 123 safety regression, 39 observed-layout/inspection, 33 menu-only and 43 native drawer/entry assertions pass; manifest permission/backup/scope and locale parity checks pass.
- Complete app compilation and signed APK build on the designated MacBook with JDK 17 / Android 36 / build tools 36.1.0. Zip alignment and APK v2/v3 signature verified. Current build is development signed.
- No phone data erased, package removed, Accessibility service enabled or unrelated Android device operated on.

## Required device evidence before stable release

1. Connect the actual Fold7 to this MacBook with a data-capable USB cable, unlock it, enable USB debugging and accept its computer authorization prompt. These are owner actions. ADB must show the Fold7 as `device`, not `unauthorized`, `offline`, or `recovery`. Any other attached phone must be excluded by explicit serial selection for every device command.
2. Inspect installed package and signing certificate first. The current package is `local.zipshortcut`. If an upstream installation has a different certificate, do not uninstall or clear its data automatically. Preserve it; decide with the owner whether a separate application identity or another migration is appropriate.
3. Inspect the installed ChatGPT version and owner-visible accessibility controls without publishing raw UI trees or conversations. Verify exact input/menu/sidebar/call/end-call labels, role, clickability and window structure for Korean and English where available. Do not infer labels from screenshots or assume that an icon is accessible.
4. Install only on the identified Fold7 after compatibility checks. Owner manually enables this app's Accessibility service and completes ChatGPT permissions/onboarding. No automated device setting changes.
5. With the owner present, verify correct Dot selection, exactly one real call start, actual call screen, audible conversation and manual call end. The utility itself never ends calls. Record folded/unfolded results and actual app/OS versions.
6. Verify wrong/missing/duplicate labels, repeated launches, already-on-call, lost/stale trees, app switching, lock, interrupted/recreated service, revocation and timeout without unwanted calls or resumed requests. Synthetic tests do not replace these device acceptance tests.
7. Configure and verify the specific Bixby/Routines launch-app action actually available on this device. Verify again while unlocked in both Fold layouts. Do not promise background unlock or unsupported Samsung actions.
8. Before stable distribution, select and retain the release signing identity and backup, ensure future updates use it, finalize package/version and disclosures, rerun full checks, and publish only results supported by the physical tests. Existing upstream-signing compatibility is not claimed. GitHub Actions currently has no confirmed run and must not be reported as passed.

## Current gate

The actual Fold7 is now identified as Samsung SM-F966N / Android 17 (API 37). It had no existing `local.zipshortcut`, and the 1.4.1-dev test APK installed without removing any app or data. A manually initiated ADB click opened ChatGPT's actual call screen; the owner reports audible Dot output. This was **not** a Voice to Dot automated call. The 1.4.2-dev source now supports the observed normal-screen Compose structure and includes read-only service diagnostics, but the updated 1.4.2-dev APK has installed successfully without data clearing, and its real AccessibilityService read-only diagnostic recognized the normal-screen menu/call owners and composer label after the owner enabled access. A later pre-announced single direct ADB test confirmed the active end-control label **종료**, its paired role structure, composer absence during the call, and normal-composer restoration after one verified end tap. Actual active-call detection through the service, sidebar markers, automated single-call flow, both Fold layouts, safety acceptance and Bixby/Routines remain gates. Next live call must be announced to the owner and coordinated before execution. See FOLD7_EVIDENCE.md.

## Latest non-call preparation gate

No calls were made during the subsequent menu/setup preparation. A menu-only diagnostic build (1.4.3-dev) installed without clearing data. The actual native navigation screen exposes **Dot** (capital D), not the profile name **dot**. The row is a clickable View with three View children and one Button-role child; the Dot TextView is two levels beneath the owner. This differs from the two-child compact call/menu binding. The complete decorative/label branch structure still needs device inspection before accepting that row. The latest source separates the entry label from the profile name and does not silently loosen the selector.

The menu probe was requested and a native MainActivity navigation screen was observed, but its temporary final attempt/result report could not be read from the scrolled setup UI. Do not count semantic menu execution as fully validated on that evidence alone. 1.4.4-dev improves report focus and unknown-ownership metadata for the next inspection.

Fold7 then disappeared from both USB and mDNS inventory. Reconnection to the last confirmed endpoint the device's then-current private connection endpoint was refused. Only the unrelated recovery device remains visible, and it was not operated on. Obtain the current **Wireless debugging IP address and connection port** (not the pairing-code port), then reconnect with the existing pairing identity if still authorized. No further live call is authorized without a new advance readiness announcement.

## Reconnected target and successful non-call navigation

Existing pairing automatically reconnected the same SM-F966N / matching serial at the device's then-current private connection endpoint; the newly supplied pairing code was not used. The complete four-child native Dot row was inspected, including an empty trailing decoration that can have zero or one empty leaf. The selector now requires that exact role/label/icon structure and unique observed `새 채팅` / `검색` drawer anchors. Unknown or duplicated evidence fails closed.

The final 1.4.5-dev build installed without data clearing. The observed profile preset was applied; all eight configuration fields matched expectations and call authorization was checked OFF. A real menu-only service probe reported **Menu attempts=1**, **recognizedTargetControls=1**, **verifiedRole=true**. A separate fixed-entry-only probe reported **entry attempts=1; no call dispatch**, then the normal Dot input plus menu/call controls. This validates semantic menu opening and native entry selection through the actual service, not an integrated live call.

Next gate: announce and coordinate exactly one app-originated live-call test, explicitly enable the app's call authorization, invoke its launcher once, and verify the app's stable outcome plus user-confirmed audio. No such call was started during this preparation. Routine/Bixby, both physical Fold layouts and remaining device safety acceptance still gate stable publication. Main and the published 1.4.0 preview have not been replaced.

## Complete native row and actual non-call service proofs

- Existing TLS pairing reconnected automatically to the same SM-F966N / original serial. mDNS identified the currently advertised connection service; the newly supplied pairing code was unnecessary and unused.
- Full native **Dot** row: clickable View with four children. Child 0 is an empty nonclickable View chain of length 3; child 1 is an empty View containing the exact nonclickable Dot TextView; child 2 is an empty nonclickable indicator View observed both with one empty leaf and with no children; child 3 is one nonclickable empty Button role. All belong to the same window/package. The actual service confirmed unique `새 채팅` and `검색` drawer anchors.
- An initially rejected row was diagnosed without relaxing action/label guards. Only the observed optional empty decoration was accommodated; clickable/labelled/branched decorations, missing/duplicate anchors and duplicate Dot entries still fail closed.
- 1.4.5-dev / code 12 installed as a same-signature update without data clearing. The observed Korean label preset stored profile **dot**, entry **Dot**, composer **내 {dot}에게 메시지 보내기**, menu **메뉴**, sidebar marker **Dot**, blank optional up control, call **통화**, end marker **종료**. UI inspection verified all 8 fields and the call-authorization checkbox **false**.
- **Actual fixed-entry-only service report:**

```text
Dot-entry-only; entry attempts=1; no call dispatch.
controls=3, inputLabels=1
Button: 통화 평가 | owner=android.view.View
Button: 메뉴 | owner=android.view.View
Button: 통화 | owner=android.view.View
Input label: 내 dot에게 메시지 보내기
```

The rating button is a residual post-call action, not an active-call marker. The fixed-entry probe can invoke only the verified native Dot row; it has no call dispatch. The normal-screen report verifies the transition after one accepted entry action.

- **Actual menu-only service report:** `Menu attempts=1`, `Target: Dot`, `recognizedTargetControls=1`, `verifiedRole=true`, `nativeExact=true`, `newChatAnchors=1`, `searchAnchors=1`. This proves one semantic menu action and the recognized native entry, without selecting or calling from that probe.
- Final preparation status: all observed configuration fields verified, call authorization OFF, no live call during this preparation. Phone left in the setup task; the underlying ChatGPT task is on its native drawer after the last menu probe. Another live call requires a new coordinating-task announcement/readiness confirmation before launcher invocation.
