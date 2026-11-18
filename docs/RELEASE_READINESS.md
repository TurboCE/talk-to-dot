# Release readiness — Voice to Dots

Current source/build: **1.4.6-dev / versionCode 13**. The identified Samsung SM-F966N last received **1.4.5-dev / code 12**, which passed the non-call service probes. The later patch refreshes the stable outcome on setup resume; it has not been installed because the target disconnected. This remains an MIT fork of Talk to Dot; stable publication is blocked by the acceptance items below. The published 1.4.0 prerelease predates the safety fixes and is not the current build.

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
5. Verify CI for the final source revision. The [2105b5c build and regression run](https://github.com/TurboCE/talk-to-dot/actions/runs/44346450085) passed; later UI-only source still requires its own run. Do not report queued or absent CI as passed. CI development keys may differ; do not install an unrelated CI-signed APK over the retained local key.
6. Finalize and retain the release signing identity/backup and a data-preserving update plan. Do not silently uninstall the existing test app to change keys. Reverify final artifact integrity, privacy disclosures and version before stable publication.

## Publication and device boundaries

Only the review branch / draft PR is published. Main and APK releases remain unchanged. Private pairing codes, connection addresses and device serials are not needed in the public release evidence. Use the actual identified Fold7 transport for every device command; never operate on another attached phone. See FOLD7_EVIDENCE.md for the bounded observations and manual-vs-app distinction.
