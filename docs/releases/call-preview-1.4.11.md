# Voice to Dot 1.4.11 development preview

Compact setup for the existing ChatGPT Dot call launcher. The owner reported Korean Bixby invocation works on the tested Fold7 with the preceding runtime. This UI cleanup removes diagnostic and immediate-call buttons; target/reset options remain under Advanced settings. Existing call settings, package and launcher identifiers are preserved.

- A launch attempts one actual ChatGPT call after exact destination/control checks; no TTS or API call.
- Optional initial Bixby handoff sends Back at most once to the exact Bixby window. It cannot dismiss other apps; a keyboard may consume Back and cause a safe timeout.
- 443 synthetic checks plus manifest/privacy/locale/MIT checks and local APK v2/v3 signature verification pass. These are not proof of every real-device case.
- Retained development signature supports updates to the locally installed test build. It differs from upstream and ephemeral CI keys. Do not uninstall or clear data to bypass a signature conflict.

Still unverified: both physical Fold layouts, the equivalent English provider UI and remaining device lifecycle/safety cases. Production signing/backup and stable-release review are pending. No new call was started for this UI-only update.

Install with Android's installer or `adb -s <verified-Fold7> install -r talk-to-dot.apk`. Review `SHA256SUMS.txt` and `SIGNING.txt`. Enable Accessibility manually if needed; keep the floating shortcut off. Existing configured owners can use their saved routine **닷 음성 실행** after unlocking. Saving settings never starts a call.
