# Voice to Dot — TurboCE call preview

**Latest development APK: [1.4.11-dev preview](https://github.com/TurboCE/talk-to-dot/releases/tag/call-preview-1.4.11).** The downloadable 1.4.0 preview does not contain the [review fixes](docs/REVIEW_FIXES.md).

MIT fork of [looizao/talk-to-dot](https://github.com/looizao/talk-to-dot), based on `6fbb5a3bde2fc2ee6a16b0a01a3138210c7b3c4c`. The original [MIT notice](LICENSE) remains. The app uses one neutral microphone icon. Independent project; not affiliated with OpenAI.

This Android shortcut opens the **existing ChatGPT app**, navigates to the selected Dot, attempts **one actual call-button click**, and observes a configured call-screen marker. It uses neither TTS nor an API voice session. On the identified Fold7, actual Korean control structures and app/routine call-screen initiation were observed; the owner reported the Korean Bixby invocation works. The current build simplifies setup while preserving the call/handoff policies. This remains a **development preview**: both physical layouts, the equivalent English provider UI and remaining device lifecycle/safety acceptance are not all verified. See [device evidence](docs/FOLD7_EVIDENCE.md).

## 설치 및 사용 / Install and use

1. Download `talk-to-dot.apk` and `SHA256SUMS.txt` from this fork’s [preview releases](https://github.com/TurboCE/talk-to-dot/releases). The APK is development signed, not signed with the upstream author’s key. If the original package is installed, this APK cannot update it: record its settings and manually uninstall it first if you choose to replace it. Uninstalling removes its saved settings. No uninstall is performed automatically.
2. Install the APK using Android’s installer. Android may ask you to permit this particular installer source. Review the app before permitting installation or restricted Accessibility settings. Menu names vary by Android/One UI version.
3. Open Voice to Dot. Expand **Advanced setup and diagnostics** to edit labels. Enter the exact Dot name and **device-verified** accessibility labels. English navigation defaults (`Menu`, `Scheduled`, `Navigate up`, `Message {dot}`) are inherited from upstream, not verified on your phone. Change them for your ChatGPT language/version. `{dot}` is replaced with the saved name. The optional sidebar entry label is separate from the profile name: this Fold7 shows **Dot** in navigation but the configured profile name is **dot**. Case is exact; an empty entry falls back to the profile name. The native four-child row is accepted only with its verified label/role/decorative structure and unique Korean drawer anchors (`새 채팅` and `검색`); unknown or duplicated evidence blocks calls. The destination must be a unique **editable input** labelled directly/by hint, or by its one noninteractive sibling in the observed two-child input container.
4. Call-button and call-screen-marker fields are deliberately blank. Manually verify the actual text/content-description of the call button and a distinct marker visible only on its call screen (for example, the actual end-call control’s label). **No label is guessed.** The call owner must be unique, visible, enabled and actionable. Its label must either be directly on a Button/ImageButton, or in the exact paired Compose structure described below. Icons without accessibility labels are unsupported. State markers must have a unique actionable Button/ImageButton role, directly or through a strictly validated paired Compose owner. Transcript text/hints and generic clickable text are ignored. The marker is observed, never clicked.
5. Confirm the explicit call authorization and save. Manually enable **Voice to Dot** in Android Accessibility using the settings button. This app does not enable services or change system settings for you. If ChatGPT requires microphone permission or onboarding, complete those manually first.
6. With the phone unlocked, launch Voice to Dot. After configuration, every launch authorizes a real call attempt. Repeated launches within 30 seconds are ignored. If confirmation fails, check ChatGPT manually: a call may already have started, and this utility does not retry or hang up.
7. To configure later, long-press the launcher icon → **Configure Dot call**, or use this Accessibility service’s settings. **Cancel pending attempt** stops automation; it does not end a call already placed. Disable the service or uninstall to remove access.

한국어: 기존 ChatGPT 앱의 실제 닷 통화를 한 번 시도하는 미리보기입니다. 첫 실행에서 닷 이름, 실제 기기에서 확인한 입력창·메뉴·사이드바·전화 버튼·통화 화면 라벨을 설정하고 통화 실행을 허용하세요. 전화 관련 라벨은 추측하지 않아 빈칸입니다. Android 접근성 서비스는 사용자가 직접 켜야 합니다. 잠금 해제 상태에서 먼저 수동 시험하세요. 확인 실패 시 이미 통화가 시작됐을 수 있으니 ChatGPT에서 직접 확인하세요. 앱은 다시 누르거나 통화를 종료하지 않습니다. 앱 설정 화면은 기기 언어에 따라 한국어/영어로 표시됩니다.

### Galaxy Fold7 / Bixby / Routines

After a successful manual test, choose Voice to Dot as the app to launch in whatever Bixby/Routines launch-app action your device exposes. A stable entry is `local.zipshortcut/.LauncherActivity`. A single microphone icon is used; legacy launcher component identities remain for update compatibility. The utility will not unlock the phone. Test both folded and unfolded layouts separately. The observed Korean path is Routines → Create routine → Manual start → Applications → Open an app or run an app action → Voice to Dot Open. The created routine was renamed **닷 음성 실행** to match the requested Korean phrase; speech recognition of the new name is still unverified.

Optional installation by an already authorized ADB connection:

```sh
adb install -r dist/talk-to-dot.apk
adb shell am start -n local.zipshortcut/.SettingsActivity
# After configuration and explicit authorization, this attempts a real call:
adb shell am start -n local.zipshortcut/.LauncherActivity
```

### Obtaining label evidence

Use TalkBack’s focused control announcement or Android UI inspection on your own device. Check the normal Dot screen and call screen separately, including the accessibility description and whether the call control itself is clickable. If a label or editable destination is not exposed, this preview will safely time out; a screenshot alone cannot establish the accessibility properties. Do not publish raw UI XML or screen dumps: they can contain private conversations. No inspection file or conversation content is collected by this app.

## Observed Compose ownership and read-only diagnostics

ChatGPT 1.2026.314 on the identified Fold7 exposes an actionable `android.view.View` with exactly two immediate children: a single noninteractive View chain containing the label and a nonclickable Button role leaf. The verified menu/call label is four levels below its owner. Only this bounded structure is accepted: one explicit label, one Button/ImageButton role, no other text, no nested actions, no broad/multiple branches, and the same package/window. Control identity, label binding and bounds are rechecked after refresh and after reserving the call attempt. A clickable paragraph with a matching word does not qualify.

The observed composer label is a noninteractive sibling of its editable input in a two-child immediate parent. The resolver validates that pairing instead of searching unrelated ancestors. These are structural checks, not fixed coordinate taps.

The current setup screen provides call authorization, Save, Accessibility settings and cancellation. Exact labels, the optional Bixby handoff and observed Fold7 reset are under Advanced settings. Development diagnostic buttons/reports and the immediate-call button were removed in 1.4.11-dev. Saving never launches a call; use the existing app/routine execution entry. The earlier isolated probe observations remain in [device evidence](docs/FOLD7_EVIDENCE.md), and their synthetic fixtures remain in the regression suite.

Complete verified settings can be saved while calls are off; incomplete fields can still revoke authorization without replacing the last valid settings.

The native **Dot** drawer row has a clickable View with four children: an empty three-View icon chain, a View containing the exact Dot TextView, an empty noninteractive indicator (observed both with and without its empty leaf), and one nonclickable Button role. All action/label/package/window checks remain required. Native drawer anchors must be unique; missing/duplicated anchors or an unknown row are ambiguous, never treated as absent to permit a call.

The advanced Fold7 reset saves the observed `dot` profile / `Dot` entry and labels, explicitly revokes call authorization, and clears pending automation. It is a reset operation, not a call trigger.

## Safety and privacy

- Call/navigation controls act only on the foreground `com.openai.chatgpt` window after explicit launch; the optional initial Bixby-only Back exception is described below. Stops within **30 seconds**, or **8 seconds after a click** if the call screen cannot be confirmed.
- Requires destination and unique actionable call control to remain stable for **500 ms** and rechecks the current window before dispatch and again after saving the click reservation. A refreshed control must retain its label, identity, package, window and bounds. Duplicate controls, missing labels, disabled controls and non-editable destination markers cannot trigger a call.
- Persists the click reservation synchronously **before** `ACTION_CLICK`; false dispatch cannot retry the pending request. Requests from a previous process are cancelled; a service reconnect before a click is cancelled once ChatGPT has been seen. Boot identity and monotonic time bound pending requests; a 30-second launch cooldown prevents immediate rearming.
- After the click, observes only. Confirmation needs a stable marker with the destination/sidebar absent. State markers require Button/ImageButton semantics and are not inferred from transcript text. Multiple matching sidebar/call-state controls are ambiguous and cancel the attempt; they are never treated as absent. Detection confirms a **screen**, not a server connection or audible conversation.
- No arbitrary call-button ancestor fallback, coordinates, gestures, fuzzy labels, microphone capture, message sending or automatic permission prompts. Navigation now also requires a verified labelled actionable owner and revalidates that label after refresh. The call owner must be directly actionable, with ownership proven by the label/role structure.
- Locks and Accessibility interruption cancel. Leaving ChatGPT after its window is first observed cancels; initial app opening can wait for ChatGPT until the deadline. A missing/unrefreshable tree waits without clicking for up to one second after ChatGPT was seen, resets all stability evidence, and then cancels if still unavailable.
- Uncheck call authorization and save to revoke it even if other fields are incomplete. Pending requests are cleared. Status names, rather than generated Android resource IDs, survive updates; old integer status values are ignored.
- No requested network, phone or microphone permission; no analytics, conversation logs or backup. Only settings, timing, click reservation and a stable result-name string are stored in app-private preferences. Android Accessibility itself grants broad screen access; enable it only if comfortable with that access.

## Build and test

Requires JDK 17, Python 3 and Android SDK `platforms;android-36`, `build-tools;36.1.0`. Builds on macOS/Linux without Gradle:

```sh
export JAVA_HOME=/path/to/jdk17
export ANDROID_SDK_ROOT=/path/to/android-sdk
./scripts/build.sh  # runs the non-device regression/privacy suite before packaging
```

Run `./scripts/test.sh` separately when you only need the non-device tests.

Outputs: `dist/talk-to-dot.apk` and `dist/SHA256SUMS.txt`. Default builds create/reuse `build/debug.keystore` (standard development credentials, not a production identity). Keep this local key if you want future development builds to update this APK. Keys are ignored by Git and never uploaded. A different key requires uninstall/reinstall. `RELEASE_BUILD=true` requires explicit `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, optionally `KEY_PASSWORD` and `KEY_ALIAS` (default `local`). Do not use the upstream release-signing secrets; this fork has its own identity.

The test suite exercises the pure call policy and the actual service against deterministic synthetic Android nodes. It covers duplicate/disabled controls, wrong app, locked device, non-editable destination, call ancestor rejection, single attempts, restart, timeout, navigation and screen confirmation. These tests do **not** verify Android UI rendering, system Accessibility delivery, Fold7 behavior or audio. [Build evidence](docs/VALIDATION.md) records the prepared APK checks. Historical upstream docs are retained under `docs/` and [upstream README](docs/upstream-README.md); their installation/signing/device claims describe upstream, not this fork.

If Android reports the service enabled but the app warns it is disconnected, turn the service off and back on in Accessibility → Installed apps → Voice to Dot. Leave the floating Accessibility Button shortcut off; it is not needed for routine launches. The app does not enable system permissions automatically.

### Optional initial Bixby handoff

Advanced setup has **Close Bixby once before entering ChatGPT** (off by default; the observed Fold7 preset enables it with call authorization OFF). During an explicitly armed request, before ChatGPT has been observed and before any call attempt, only the exact installed Bixby package `com.samsung.android.bixby.agent` may receive one semantic system Back action. Its active window must be stable for 500ms and revalidated before/after persisting the reservation. Request validity, opt-in, call authorization, deadline and unlocked state are checked again at dispatch. Other apps receive no Back. An overlay after entering ChatGPT still cancels the attempt.

No call verification is relaxed and no click is sent through Bixby. Failed/inconclusive Back is not retried. A keyboard may consume Back, leaving the assistant active; this safely times out. Earlier development builds had a Bixby-only probe that cleared pending requests and never armed/clicked a call; its UI is removed in 1.4.11-dev. The Fold7 keyboard-open probe timed out after one Back; the subsequent keyboard-hidden probe returned to setup with Bixby no longer active. Neither started a ChatGPT microphone session.
