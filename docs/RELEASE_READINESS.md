# Release readiness — Voice to Dots

Current local source/build: **1.4.8-dev / versionCode 15**; device trial used **1.4.6-dev / versionCode 13**. The identified Samsung SM-F966N passed the non-call service probes with the unchanged core service in 1.4.5-dev; the later patch refreshes the stable outcome on setup resume. The final same-signature update succeeded and all eight prepared fields plus call authorization OFF were reverified. This remains an MIT fork of Talk to Dot; stable publication is blocked by the acceptance items below. The published 1.4.0 prerelease predates the safety fixes and is not the current build.

## Completed

- Safety corrections: refreshed label/identity/window/bounds validation; one-attempt reservation; restart cancellation; missing-tree grace; resolved-label collisions; authorization revocation; structural state evidence; stable result names; ambiguous-state cancellation.
- **363 non-device assertions**: 27 call-policy, 31 baseline service, 123 safety regression, 39 compact-owner/inspection, 33 menu-only and 43 native drawer/entry and 67 actual-launcher checks. Manifest/privacy/locale/MIT checks pass. Every APK build now runs these gates before packaging, including the inherited CI build entrypoint.
- Local JDK 17 / Android 36 / build tools 36.1.0 compilation, D8, packaging, alignment and APK v2/v3 signature verification pass. Runtime minimum API 26, target 36; device Android 17 / API 37, ChatGPT 1.2026.314.
- Actual service recognizes normal-screen menu/call owners and paired input label. It opened the menu once and recognized the exact native four-child Dot row with unique Korean drawer anchors.
- Actual entry-only service test dispatched exactly one action to that verified native Dot row and observed the normal Dot input/menu/call controls. It contains no call dispatch.
- All eight observed profile/entry/label fields were verified on the device; call authorization was OFF before the announced trial. Same-signature updates preserved settings. No original app or data was removed and no device permission was enabled by the agent.
- The announced app-originated trial reached the saved confirmed-screen result. No direct ADB ChatGPT call-button tap was used. Owner audio confirmation is pending. The app's own call authorization was then saved OFF without selecting launch again.
- UI source now uses Voice to Dot, one neutral microphone icon, and collapsible advanced configuration/diagnostics. Legacy launcher component names remain available for existing routine references. Version 14 was installed over the retained local signing identity without removing data. The actual setup screen showed no editable fields or icon spinner while advanced setup was collapsed; call authorization was OFF, and the prior confirmed result remained visible.
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

1. **Owner audio confirmation remains pending for the announced app-originated trial.** The app's Save-and-call button was selected exactly once; no ADB ChatGPT call-button action occurred. The saved result reports the configured call screen detected. Microphone app-ops reported approximately 47 seconds of use and was no longer running when observed. This establishes app call-screen detection, not audible conversation or who ended the call. Never repeat without notice.
2. Authorization-OFF launch refusal passed on device through both stable LauncherActivity and legacy MainActivity; both returned to own settings with consent OFF and no new microphone session observed. Still verify authorized natural startup/return/cold navigation, missing-label timeout, repeated invocation cooldown, app departure/interruption, revocation during an attempt and locked-device refusal. Synthetic tests remain the only evidence for scenarios the real provider cannot safely reproduce, such as duplicate controls.
3. Establish physical folded/unfolded behavior with the owner. Display dimensions alone are not proof of state. Verify any persistent side-pane behavior before claiming both layouts work.
4. The installed Samsung Routines app exposes **루틴 만들기 → 이 루틴으로 실행할 동작 추가하기 → 애플리케이션 → 앱을 열거나 앱 동작 바로 실행 → Voice to Dot 열기** (confirmed in an empty unsaved draft; nothing was saved or executed). Verify a saved routine and the owner's requested “dots 음성 실행” voice trigger separately. Owner handles system settings and permission changes. The utility does not unlock the phone.
5. Verify CI for the final source revision. The simplified UI runtime [3c33a2e build](https://github.com/TurboCE/talk-to-dot/actions/runs/44351295637) also passed; its release job was skipped. Both the [2105b5c build](https://github.com/TurboCE/talk-to-dot/actions/runs/44346450085) and the latest runtime-source [1e6a502 build](https://github.com/TurboCE/talk-to-dot/actions/runs/44347446027) passed. The logs show all six regression groups, privacy/resource checks, APK compilation and signature verification; no release job ran. Do not report queued or absent CI as passed. CI development keys may differ; do not install an unrelated CI-signed APK over the retained local key.
6. Finalize and retain the release signing identity/backup and a data-preserving update plan. Do not silently uninstall the existing test app to change keys. Reverify final artifact integrity, privacy disclosures and version before stable publication.

## Publication and device boundaries

Only the review branch / draft PR is published. Main and APK releases remain unchanged. Private pairing codes, connection addresses and device serials are not needed in the public release evidence. Use the actual identified Fold7 transport for every device command; never operate on another attached phone. See FOLD7_EVIDENCE.md for the bounded observations and manual-vs-app distinction.

## Interrupted Samsung routine trial

A new manual routine named **Voice to Dot** was saved with exactly one action: Voice to Dot Open. Existing routines were not edited. No routine start was dispatched: initial connectivity failed, and the later pretrial checks found the service enabled but disconnected/in Android's Crashed services list. An old confirmed result was therefore not accepted as new success. The routine execution and voice-trigger acceptance gates remain open.

The floating grey microphone was Android's Accessibility Button shortcut assigned to this utility, not a utility-created overlay or the system microphone privacy indicator. Only its Voice to Dot button assignment was unchecked; service access remained enabled. No other shortcut targets or apps were changed. A no-call service-readiness fix is in 1.4.8-dev; actual recovery still requires verifying the Android service connection, potentially an owner-operated service off/on.
