# PayShield — Think Before You Pay

**PayShield** is a privacy-first, 100% offline-capable Android cybersecurity application designed to protect users before irreversible financial transactions or security breaches occur. It performs real-time risk assessment on UPI payment QR codes, scam/phishing text messages, and embedded URLs directly on the Android device.

```text
               PAYSHIELD ARCHITECTURE AT A GLANCE
               
             UPI QR / Message Input / Android Share
                               │
                               ▼
                   Signal Extraction & Parsing
                               │
                               ▼
                      Signal Normalizer
                               │
                               ▼
            Risk Engine (Capped Category Scoring)
                               │
                               ▼
                         Decision Policy
                               │
             ┌─────────────────┼─────────────────┐
             ▼                 ▼                 ▼
          LOW RISK         SUSPICIOUS        HIGH RISK
                               │
                               ▼
                   Deterministic Explanation
                               │
                               ▼
                      Optional Local LLM
```

---

## Key Capabilities

1. **User-Initiated Real-Time Protection**:
   - **QR Shield**: Live scanning via CameraX and bundled Google ML Kit Barcode Scanning (`Barcode.FORMAT_QR_CODE`). Debounced with payload hashing and scan locks.
   - **Message Shield**: Paste text or share directly from external apps (SMS, WhatsApp, Web Browser) via `Android Share Intent` (`Intent.ACTION_SEND`).

2. **Decoupled Security Pipeline**:
   - **UPI Parser & Validator**: Safe defensive parsing enforcing strict limits (max 2048 chars payload, max 32 parameters). Extracts `pa`, `pn`, `am`, `tr`, `cu`, and `unknownParams`.
   - **Text Normalizer**: De-obfuscates evasion tactics (e.g. `O.T.P`, `O T P`) while preserving raw input text.
   - **Url Analyzer**: Structural offline analysis of numerical IP hosts, Punycode/IDN homograph spoofing, excessive subdomains, and brand name impersonation.
   - **Signal Normalizer & Risk Engine**: Grouped into `IDENTITY` (max 35), `CONTENT` (max 30), `URL` (max 25), and `CONTEXT` (max 10). Total score max 100.
   - **Decision Policy**: Evaluates minimum-evidence rules to prevent single weak signals from inflating to `HIGH RISK`.

3. **Deterministic Explanation & Evidence Confidence**:
   - Primary security analysis and recommendations are 100% deterministic (Source of Truth).
   - Local LLM acts strictly as a non-authoritative explanation generator with prompt isolation (`<<<USER_CONTENT>>>`) and schema validation.
   - Evidence Confidence (`LOW`, `MEDIUM`, `HIGH`) represents evidence quality and signal diversity.

4. **100% Offline Guarantee**:
   - Uses the bundled variant of Google ML Kit barcode scanning (`com.google.mlkit:barcode-scanning`).
   - Zero internet permission required (`android.permission.INTERNET` omitted from `AndroidManifest.xml`).

---

## Terminology & Calibrated Claims

PayShield explicitly avoids absolute security claims:

- **Used Terms**: `Risk assessment`, `Suspicious`, `High risk`, `Low risk`, `Possible impersonation`, `Could not be independently verified offline`.
- **Forbidden Terms**: `Guaranteed safe`, `Guaranteed scam`, `Merchant verified`, `Fraud probability`.

---

## How to Build and Run

### Prerequisites

- Android Studio Ladybug / Meerkat or command-line JDK 17+ & Android SDK Platform 34+.

### Build & Run App

```bash
./gradlew assembleDebug
```

### Execute Test Suite

```bash
./gradlew test
```

---

## Security Principles

```kotlin
/*
 * PAYSHIELD SECURITY PRINCIPLES:
 * 1. External content is untrusted.
 * 2. The deterministic security engine is authoritative.
 * 3. The LLM is non-authoritative.
 * 4. Unknown does not mean malicious.
 * 5. Risk does not mean certainty.
 * 6. The application warns; it does not guarantee or authorize payments.
 */
```
