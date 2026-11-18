# Fold7 evidence — scope and current limits

Observed on the designated MacBook with every device command explicitly selecting the Fold7. The unrelated dream2lte recovery device was not operated on. No raw conversation tree, message text, audio or screen recording is retained in this repository.

## Observed / owner reported

- Model: Samsung **SM-F966N**, ADB-authorized. Android **17**, SDK **37**. ChatGPT **1.2026.314** (versionCode 2631431).
- `local.zipshortcut` was absent. Development APK **1.4.1-dev / code 8** installed successfully, and its Korean setup UI displayed. The same-signature **1.4.2-dev / code 9** then installed successfully with `adb install -r --no-incremental`, without clearing data; package inspection confirmed code 9. The service was still disabled on the final read-only check. No existing package was uninstalled and no data cleared.
- The owner identified the target name as **dot** and opened that conversation.
- Normal-screen descriptions observed: menu **메뉴**, call **통화**, composer label **내 dot에게 메시지 보내기**, assistant options **어시스턴트 옵션 열기**, dictation **받아쓰기**. These are device evidence, not general defaults or assumptions for another version/language.
- Menu/call labels are nonclickable `android.view.View` descendants four levels below a clickable `android.view.View` owner. That owner has two immediate children: a noninteractive label branch and one nonclickable `android.widget.Button` role leaf.
- The composer description is in a noninteractive View sibling of the `android.widget.EditText`, under their immediate two-child parent; the input itself had no exposed text/description.
- One manually initiated ADB control tap opened the real ChatGPT call screen. The owner confirmed hearing audio. **Voice to Dot did not initiate this call:** its service was not enabled/configured. A later attempted inspection tap failed its target-header guard and did not execute a second call click.
- The UI subsequently returned to the conversation. **통화 평가** was observed there; it is a post-call rating control and must not be mistaken for an active-call confirmation marker.
- The end-call control's exact label/ownership was not captured before the screen returned. Do not invent or configure it as verified.

## Source work derived from evidence

1.4.2-dev supports only the constrained paired label/Button ownership and paired input-label structure observed above. It retains unique-control and ambiguous-state cancellation, refresh/identity/bounds revalidation, one-attempt persistence and deadlines. No arbitrary ancestor or coordinate fallback is added to the app. Synthetic fixtures reproduce only control labels and structure, with no conversation content.

Read-only diagnostics can inspect the actual AccessibilityService view once the owner manually enables access. It performs no clicks, does not arm a call, excludes editable values and transcript text, and holds its report in process memory only. Structural behavior remains subject to actual service-tree validation.

## Still required

- Owner manually enabling Talk to Dot Accessibility; the agent has only opened the system settings screen, never toggled the permission.
- Actual service diagnostics, confirmed sidebar/Dot selection structure, active end-call marker, automated app-originated call, manual audio confirmation of that flow, negative/safety cases and Bixby/Routines.
- Folded/unfolded device state has not been independently established; display dimensions alone are not claimed as proof.
- Before another live call, announce the intended test to the owner through the coordinating task and wait for readiness. Do not unexpectedly trigger another call.
- No stable release or verified end-to-end claim until these gates pass.
