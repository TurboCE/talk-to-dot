# Release readiness — Voice to Dot

Published preview: **1.4.11-dev / versionCode 18**. Current installed candidate: **1.4.12-dev / versionCode 19**, which skips redundant unchanged setup-status writes. The owner reported that the Korean Bixby invocation works with the preceding code17 runtime. Code18 simplifies setup; call/navigation/handoff policies are unchanged. This remains a development preview.

## Completed

- 452 synthetic assertions: 27 policy, 31 service, 123 safety regression, 39 compact controls, 33 menu probe, 59 native navigation, 73 optional Bixby handoff and 67 actual-launcher checks. Manifest/privacy, locale parity and MIT checks pass.
- Local JDK17/Android36 compilation, packaging, alignment and APK v2/v3 signing verification pass. Retained local signing identity permits data-preserving updates. No private key is published.
- Identified Fold7 SM-F966N, Android17/API37, ChatGPT1.2026.314. Actual structural control discovery, menu and Dot-entry probes, authorization-OFF launch refusal, service recovery and removal of the optional floating shortcut were verified.
- Announced app/routine trials reached the configured call screen and showed new microphone activity. The owner subsequently reported Bixby calling works. This is owner-reported spoken-command acceptance; no claim that every device/layout/lifecycle case was independently tested.
- Created routine **닷 음성 실행** retains manual triggering and exactly one action: Voice to Dot Open. Other routines were preserved.
- Optional Bixby handoff allows only one initially stable exact-package Back before ChatGPT is observed. Keyboard-open probe safely timed out; keyboard-hidden no-call probe returned to setup with one Back and no call.
- Code18 removes diagnostic/immediate-call UI, moves target/reset options under Advanced settings, shortens disclosures and keeps authorization, Save, Accessibility access, result/status and cancellation. No launcher identifier, core call policy, or saved-setting key changed.

## Saved observed profile

| Field | Value |
| --- | --- |
| Profile / entry | `dot` / `Dot` |
| Input | `내 {dot}에게 메시지 보내기` |
| Menu / drawer | `메뉴` / `Dot` |
| Up | `상위 화면으로 이동` |
| Call / call-screen marker | `통화` / `종료` |

## Remaining stable-release gates

1. Confirm physical folded/unfolded operation with the owner; dimensions alone are not proof. Current English provider layout is not verified.
2. Complete the remaining device safety/lifecycle acceptance: authorized cold startup, repeated invocation cooldown, departure/interruption, revocation mid-attempt and lock refusal. Existing synthetic checks are not device evidence. Never perform a new live trial without advance notice.
3. Retain/secure the release signing identity and a data-preserving upgrade/backup plan. Current APKs use the retained development identity. Never uninstall the existing app silently to change keys.
4. Confirm the final review/CI and artifact metadata before stable publication. Development CI uses its own key: its APK may not update the retained locally signed installation.

## Publication

Work is on `fix/review-safety`, draft [PR #1](https://github.com/TurboCE/talk-to-dot/pull/1) targets `main`. The default `main` was still `8ecac1ae4a9497bc3b16d8ea61a6a6b4379cc322` when checked before this cleanup. The prior published `call-preview-1.4.0` predates the fixes. The [call-preview-1.4.11 development preview](https://github.com/TurboCE/talk-to-dot/releases/tag/call-preview-1.4.11) is published from runtime commit `26ddb395cd4f0529d1a8a10d2e11252a0ce416b9`. Source [CI 44366689368](https://github.com/TurboCE/talk-to-dot/actions/runs/44366689368) passed. Uploaded, installed and local APK SHA256 all match `1f67128baa72e9662581ea51f7aa14c12d71c0cb374c22e6f1dcbb2b4908c514`. The GitHub release is explicitly prerelease, not draft. Main remains unmerged. No stable release has been published.

[FOLD7_EVIDENCE.md](FOLD7_EVIDENCE.md) retains the bounded historical observations. Private serials, pairing codes, connection endpoints and raw conversation dumps are excluded.

## Additional non-call preparation

A dedicated signing candidate, API26–37 signature verification and encrypted-key archive restoration/signing rehearsal are complete locally; see [SIGNING_PLAN.md](SIGNING_PLAN.md). Nothing changed the installed signer or published APK. Independent owner backup and a device rotation test are still pending.

Non-call device lifecycle fixtures were prepared to use a non-matching call label as an additional safeguard. They were not applied or executed: the phone changed to another user app during setup, so the agent stopped and requested a short idle/unlocked window. The original call label, authorization and working settings remain unchanged. No new call was started.

The owner reconfirmed actual spoken Bixby operation. Code19's 443 synthetic checks/build/signature verification passed and its own Advanced UI action passed after installation. The additional non-call lifecycle sequence did not run: guard setup aborted on the confirmed locked/Dozing SystemUI root before editing. Do not mark repeated-launch, departure, cold process or lock-refusal acceptance complete. A fresh unlock/short idle window is needed; no guard or new call was left active.

Code19 [CI](https://github.com/TurboCE/talk-to-dot/actions/runs/44373981636) is green. Current hardware-reported state is CLOSED with no override, but the phone is locked/Dozing. The next device step needs a fresh unlock while keeping the current closed state; physical opening will be requested separately. No extra live call has been authorized or initiated in this window.

The reported nickname **리코** has not been assumed to be a provider selector. Exact-Unicode synthetic tests pass, but the actual editable-input label must be read after unlock before deciding whether saved settings need changing. Google/Gemini package presence and the utility's public launcher were confirmed read-only; Hey Google invocation/overlay behavior remains untested and no generic dismissal was added.

The owner subsequently reported a fresh routine/direct-launch failure to start voice. Treat full-flow acceptance as **regressed/unresolved**, despite earlier owner success. Code20 only adds feedback to the previously silent cooldown branch; the underlying first-launch failure remains unverified pending a new Fold7 connection and actual control-label/result inspection. Do not promote this state to stable or assume the reported nickname equals a UI selector.
