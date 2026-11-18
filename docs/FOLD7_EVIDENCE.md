# Release readiness — Voice to Dots

Current source/build and installed device version: **1.4.6-dev / versionCode 13**. The identified Samsung SM-F966N passed the non-call service probes with the unchanged core service in 1.4.5-dev; the later patch refreshes the stable outcome on setup resume. The final same-signature update succeeded and all eight prepared fields plus call authorization OFF were reverified. This remains an MIT fork of Talk to Dot; stable publication is blocked by the acceptance items below. The published 1.4.0 prerelease predates the safety fixes and is not the current build.

## Completed

- Safety corrections: refreshed label/identity/window/bounds validation; one-attempt reservation; restart cancellation; missing-tree grace; resolved-label collisions; authorization revocation; structural state evidence; stable result names; ambiguous-state cancellation.
- **296 non-device assertions**: 27 call-policy, 31 baseline service, 123 safety regression, 39 compact-owner/inspection, 33 menu-only and 43 native drawer/entry checks. Manifest/privacy/locale/MIT checks pass. Every APK build now runs these gates before packaging, including the inherited CI build entrypoint.
- Local JDK 17 / Android 36 / build tools 36.1.0 compilation, D8, packaging, alignment and APK v2/v3 signature verification pass. Runtime minimum API 26, target 36; device Android 17 / API 37, ChatGPT 1.2026.314.
- Actual service recognizes normal-screen menu/call owners and paired input label. It opened the menu once and recognized the exact native four-child Dot row with unique Korean drawer anchors.
- Actual entry-only service test dispatched exactly one action to that verified native Dot row and observed the normal Dot input/menu/call controls. It contains no call dispatch.
- All eight observed profile/entry/label fields were verified on the device; **call authorization remains OFF**. Same-signature updates preserved settings. No original app or data was removed and no device permission was enabled by the agent.
- Earlier explicitly announced ADB call/start-end tests identified the active `종료` control and the owner reported audio. Those calls were **not** started by this app.

## Saved observed profile

| Field | Value |
| --- | --- |
| Profile name | `dot` |
| Native entry label | `Dot` |
| Dot input template | `내 {dot}에게 메시지 보내기` |
| Menu | `메뉴` |
| Native drawer marker | `Dot` |
| Optional alternate/up control | empty |
| Call | `통화` |
| End-call screen marker | `종료` |

Only the observed Korean ChatGPT structure is established. English setup resources exist; the equivalent current English provider layout is not claimed as verified. Native row acceptance requires its exact label/role structure, unique `새 채팅` / `검색` anchors and an inert optional indicator. Missing/duplicate or unrecognized evidence cannot permit a call.

## Remaining stable-release gates

1. **Announce and coordinate one app-originated live call.** Enable the app's own call authorization only for the coordinated test, invoke its launcher once and distinguish its Accessibility action from a direct ADB phone-button tap. Verify the app's stable result and owner-confirmed audio. End the test call with the owner; never schedule an unannounced retry.
2. Verify natural startup/return/cold navigation, missing-label timeout, repeated invocation, app departure/interruption, authorization revocation and locked-device refusal. Synthetic tests remain the only evidence for scenarios the real provider cannot safely reproduce, such as duplicate controls.
3. Establish physical folded/unfolded behavior with the owner. Display dimensions alone are not proof of state. Verify any persistent side-pane behavior before claiming both layouts work.
4. Verify the exact Bixby/Routines launch-app action actually available on the device. Owner handles any system permission/settings changes. The utility does not unlock the phone.
5. Verify CI for the final source revision. Both the [2105b5c build](https://github.com/TurboCE/talk-to-dot/actions/runs/44346450085) and the latest runtime-source [1e6a502 build](https://github.com/TurboCE/talk-to-dot/actions/runs/44347446027) passed. The logs show all six regression groups, privacy/resource checks, APK compilation and signature verification; no release job ran. Do not report queued or absent CI as passed. CI development keys may differ; do not install an unrelated CI-signed APK over the retained local key.
6. Finalize and retain the release signing identity/backup and a data-preserving update plan. Do not silently uninstall the existing test app to change keys. Reverify final artifact integrity, privacy disclosures and version before stable publication.

## Publication and device boundaries

Only the review branch / draft PR is published. Main and APK releases remain unchanged. Private pairing codes, connection addresses and device serials are not needed in the public release evidence. Use the actual identified Fold7 transport for every device command; never operate on another attached phone. See FOLD7_EVIDENCE.md for the bounded observations and manual-vs-app distinction.

## Final prepared runtime and CI

The latest owner-provided pairing information was used only for the specified device; pairing and a real TLS connection succeeded, and the original model/serial identity matched. No private endpoint or pairing code is needed in this public record.

The same-signature **1.4.6-dev / code 13** update succeeded without data clearing. Its only runtime change after the successful non-call probes refreshes the stable saved outcome whenever setup resumes. Device UI inspection again verified **8/8 prepared fields** and **call authorization OFF**. No live call was started in this preparation.

Mandatory non-device regression gates now run for every APK build. The [latest runtime-source Linux CI](https://github.com/TurboCE/talk-to-dot/actions/runs/44347446027), revision `1e6a502`, succeeded, as did the prior service revision's run. Local macOS build/signature checks also pass. The current source remains a draft review branch; a real app-originated call, remaining device safety/layout/routine acceptance and release signing still gate stable publication.

## Announced app-originated trial and simplified UI

One selection of the utility’s Save-and-call action produced its stable configured-call-screen-confirmed result. No direct ADB ChatGPT phone-control click was used. The provider microphone app-op later showed about 47 seconds of completed use. Audio and who ended the call are not inferred from this record; owner feedback is pending. Automatic-call authorization was subsequently saved OFF without another launch.

The retained-signature 1.4.7-dev / code14 update installed successfully with no uninstall or data reset. Actual setup rendering showed advanced edit fields and the icon chooser absent, authorization OFF, and the prior confirmed result. The app uses a neutral microphone icon and Voice to Dot label.

An empty, unsaved Samsung Routines draft exposed: 루틴 만들기 → 이 루틴으로 실행할 동작 추가하기 → 애플리케이션 → 앱을 열거나 앱 동작 바로 실행. Searching Voice showed **Voice to Dot 열기**. The action was not selected or run; no routine was saved. This establishes availability, not successful Bixby voice-trigger execution.

With authorization verified OFF, both the stable `LauncherActivity` and legacy `MainActivity` entry were invoked once. Each returned to own setup with authorization still OFF; the provider microphone app-op remained stopped with its prior completed duration unchanged. This verifies authorization-off routing on device. It does not test the authorized-launch cooldown, locked-device gate, cold provider startup, or interruption during a live attempt.

## Service recovery and 1.4.8-dev (no call)

The announced routine trial stopped before any routine Start action when preflight found no service connection despite the enabled setting. The available app crash buffer had no matching Java exception; Android update/process-termination logs were seen, without establishing the exact cause. The retained-signature code15 update installed successfully and preserved settings. Its disconnected-service warning was visible, and Cancel immediately replaced the old confirmed result with cancelled even without a connected service. Authorization was saved OFF.

The owner explicitly asked the agent to perform the recovery. Only the utility's main service control was toggled off and on in Android Accessibility → Installed apps → Voice to Dot. Actual connection was then verified in the complete **multiline Bound services section**, not by the Enabled services list or a single-line match. The setup warning disappeared. The floating Accessibility Button assignment remained absent and authorization remained OFF. Last result showed cancelled. Provider microphone app-ops retained the old completed duration; no new call or routine Start occurred. Existing non-floating hardware shortcuts and other services were preserved.

Runtime source 712da62 passed [CI 44356586143](https://github.com/TurboCE/talk-to-dot/actions/runs/44356586143); the local full suite passed 363 assertions and the APK v2/v3 signature checks. Samsung routine execution and audio feedback are still pending.

## Single saved-routine attempt and work-conversation exit

The agent selected **Voice to Dot 시작** exactly once in Samsung Routines, without directly launching the utility or clicking a ChatGPT call control. The pretrial app result was cancelled. ChatGPT became foreground; after 35 seconds the saved result was pre-call timeout, and the microphone record retained its previous completed duration. This is a failed full-flow trial, not successful voice initiation.

A subsequent no-click service probe recognized a non-Dot work input and the actionable **상위 화면으로 이동** control. No work title/input identifier is included in public evidence. The observed optional Up setting was blank. A single structurally checked ADB action on that Up control returned to a screen with one **메뉴** control; it did not start a call. The 1.4.9-dev observed preset now includes the exact Korean Up label; synthetic regression checks pass. No further live call was attempted.
