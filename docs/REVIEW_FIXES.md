# Local review fixes — 1.4.1-dev

The published `call-preview-1.4.0` / `8ecac1a` does not contain these source corrections. The corrected source can be published for review while physical-device validation is pending; no replacement or stable APK release is approved by synthetic checks alone. No phone installation or settings change was performed during the correction pass.

| Reviewed problem | Correction | Regression evidence |
| --- | --- | --- |
| Call control could change during `refresh()` | Require the same node identity, exact current call label, actionability, package, window and stable bounds after refresh. Re-read destination/state controls; repeat verification after synchronous click reservation, before dispatch. | Changed label/package/window/enabled/bounds, same-position replacement and post-reservation label change cannot click. |
| Recreated process/service could resume an old pre-click request after app departure | Each explicit launch stores a process nonce. A previous process request is cancelled. Persist whether ChatGPT was observed; a same-process service reconnect before a click cancels once that observation occurred. A same-process post-click reconnect can only observe, never click again. | New-process owner mismatch and observed pre-click service reconnect cancel; initial same-process service binding remains supported. Existing post-click single-attempt test remains. |
| Transient missing/stale root was treated as proven app departure | Distinguish unknown tree from a known foreign package. Pause without any clicks for up to 1 second after target observation, reset button/confirmation stability evidence, and cancel on expiry. Known foreign app still cancels immediately after target observation. Total 30-second deadline still applies. | Null and refresh-failure recovery restart the full stability interval; persistent absence cancels; known other app cancels. |
| Labels could collide after `{dot}` substitution | Validate both raw labels and fully resolved labels against the saved Dot name. | Destination/confirmation collision introduced by the Dot name is rejected; a different noncolliding name is accepted. |
| Authorization could not be revoked with incomplete fields | Revocation is persisted synchronously without validating unrelated inputs and atomically clears the pending request. Save-and-call does not launch with authorization unchecked. | Invalid/empty fields can revoke; pending state and eligibility clear; a running service cannot click after revocation. Enabling still requires a valid configuration. |
| Transcript text was interpreted as sidebar/call state | State evidence now requires one visible, enabled, directly clickable Button/ImageButton with explicit text/content-description, never a text/hint search. Editable destination matching is restricted to actual editable nodes before uniqueness checks. | Plain and clickable TextView transcript nodes containing sidebar/end-call labels are ignored. Real button markers still work. A transcript copy of the composer label does not suppress the real input. |
| Generated resource IDs were persisted across APK versions | Persist a stable result name, map it to this version’s resource ID only when displaying it, ignore unknown names and legacy integer `lastStatus`, remove the old integer key when recording a new result. | Unknown/legacy values cannot be interpreted as resource IDs; known stable names resolve to this build’s resources. |

## Additional review correction: ambiguous state markers

State matching now preserves three outcomes: zero controls (absent), one (unique), and two or more (ambiguous). Duplicate sidebar or call-screen controls stop the session with a stable `ambiguous_state` result before any navigation/call action. Both initial observation and every call dispatch revalidation enforce this distinction, including after click reservation. Duplicate markers after a click stop observation without reporting success or issuing another action.

Regression coverage includes duplicate `REAL_END` and `Scheduled` controls, zero call/menu/Dot clicks, markers introduced during both preflight reads and both post-reservation reads, and duplicates after an existing call attempt. Existing unique-marker and plain transcript tests remain in the full suite.

## Validation and limits

- The full existing policy/service suite and new targeted review regressions pass. Exact check counts are recorded in the final task report; these are assertion counts, not device test cases.
- JDK 17, Android SDK 36/build tools 36.1.0 compile and package the complete app. APK uses the same local development key as 1.4.0, version code 8 / version name 1.4.1-dev. Signature and alignment are verified locally.
- No physical device was accessed for these fixes. Actual Activity rendering, Android event delivery, process recovery and disk failures remain unverified. Test `Handler` and preferences remain deterministic fakes, not a real Android runtime.
- State markers now deliberately require Button/ImageButton accessibility semantics. A real ChatGPT control may expose a different role or only label its child; without device evidence, the app will fail closed rather than infer a location or an unsupported role. Fold7 UI identifiers and actual audio/Bixby/Routines behavior still need owner-assisted device validation.
- A re-created pre-click request is intentionally cancelled rather than resumed. After cancellation the user can explicitly launch a new request when the existing launch cooldown expires. Cancelling automation does not end an already started call.

## Service readiness and stale outcomes (1.4.8-dev)

On the Fold7, Android listed the utility as enabled but in Crashed services, with no bound service connection. No matching app Java exception remained in the available crash buffer. App-update process termination was observed, but the precise root cause is not established. An enabled setting alone is insufficient evidence of a working service.

The actual launcher now refuses and clears a request if its service is disconnected, showing a recovery message instead of opening ChatGPT. New requests persist a starting result, preventing an older success from appearing to describe a new attempt. Launch failures and lock refusal persist distinct outcomes; cancellation now records cancelled even with no live service. Setup warns about the disconnected service and explicitly says the floating accessibility shortcut is unnecessary. No code enables permissions or creates a floating microphone.

67 regression checks execute the actual MainActivity against deterministic Android fakes: disconnected-but-enabled, reconnect after refusal, exact component matching, absent permission, consent-off, lock, absent provider, progress state, cooldown, intent flags, dispatch exception, failed preference commit and service-free cancellation. These remain synthetic lifecycle tests, not Android service-binding or voice evidence.
