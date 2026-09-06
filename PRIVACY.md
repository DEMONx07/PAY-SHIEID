# PRIVACY.md — PayShield Privacy & Data Governance Model

PayShield is built on a fundamental privacy principle:

> **Data Minimization & Local-First Processing.**

---

## Core Privacy Guarantees

1. **Zero Cloud Dependencies**:
   - All risk analysis, UPI parsing, URL structural analysis, and rule matching occur 100% locally on the Android device.
   - `android.permission.INTERNET` is omitted from `AndroidManifest.xml`. No network requests are attempted.

2. **No Background Surveillance**:
   - PayShield does NOT run background services to monitor SMS, WhatsApp, Telegram, or notifications.
   - PayShield explicitly avoids `AccessibilityService` APIs to prevent invasive background screen reading.
   - All message analysis is strictly user-initiated through explicit **Paste** or **Android Share Intent**.

3. **No Raw Content Persistence**:
   - Raw message text and scanned QR image frames are analyzed transiently in memory and immediately discarded.
   - Raw inputs are never written to disk, telemetry logs, or remote servers.

4. **Zero Third-Party Analytics / Crash Reporting**:
   - PayShield contains no remote crash reporting SDKs (e.g. Firebase Crashlytics) or tracking SDKs that could capture sensitive OTPs or payment details.

---

## User Consent & Permissions

- **Camera Permission (`android.permission.CAMERA`)**: Requested strictly for scanning QR codes live via CameraX. Used only while the scanner screen is visible.
- **Android Share Intent (`Intent.ACTION_SEND`)**: Utilizes standard OS intent filters allowing users to deliberately share text to PayShield.
