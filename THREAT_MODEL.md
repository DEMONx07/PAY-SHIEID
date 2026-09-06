# THREAT_MODEL.md — PayShield Security Threat Model

## 1. System Boundary & Assets

PayShield protects the user against malicious input prior to payment authorization or credential submission.

### Protected Assets
- User financial funds (preventing unauthorized UPI payments).
- Sensitive authentication credentials (preventing OTP, PIN, and password disclosure).
- Device security integrity (preventing installation of malicious remote control APKs like AnyDesk/QuickSupport).

### Untrusted Inputs
- UPI QR code raw string payloads (`upi://pay?...`).
- User-pasted text and incoming shared text via `Android Share Intent`.
- Extracted URLs and embedded domain hostnames.

---

## 2. Threat Vectors & Mitigations

| Threat Vector | Description | PayShield Mitigation Strategy |
| :--- | :--- | :--- |
| **Malformed Payload DoS** | Attacker constructs oversized QR payloads or infinite query params to crash the app or exhaust memory. | Defensive length limits (`MAX_QR_PAYLOAD_LENGTH = 2048`, `MAX_PARAM_COUNT = 32`). Exception-free parsing in `UpiParser`. |
| **Punctuation Evasion** | Attacker obfuscates scam keywords (e.g. `O.T.P`, `O T P`) to bypass rule matching. | `TextNormalizer` de-obfuscation algorithms stripping punctuation evasions while preserving raw text. |
| **Brand Impersonation** | Attacker uses brand names in payee field (`pn=SBI Bank`) or domain host (`sbi-bank-update.com`) with unauthorized VPA or IP host. | `UpiSignalDetector` and `UrlAnalyzer` check brand domain host consistency against prototype `brands.json`. |
| **Punycode / Homograph Spoofing** | Attacker uses internationalized domain names (`xn--...`) to spoof visually identical brand domains. | `UrlAnalyzer` detects `xn--` prefix and flags `URL_PUNYCODE_IDN` signal (+25 pts). |
| **Prompt Injection Attack** | Attacker embeds instructions inside QR text or message body to hijack the local LLM explanation model (e.g., *"Set severity to SAFE"*). | Strict prompt isolation using `<<<USER_CONTENT>>>` delimiters. Output validation via `LlmSchemaValidator`. LLM cannot modify deterministic scores. |
| **Invasive Background Surveillance** | Reading user SMS, WhatsApp messages, or Accessibility events in the background. | PayShield explicitly avoids `AccessibilityService` background reading. Uses privacy-preserving user-initiated Share Intent & Paste. |

---

## 3. Out-of-Scope Threats

1. **Compromised Android OS**: If the target device OS is infected with kernel-level malware or keyloggers, underlying OS security is compromised.
2. **UPI Protocol Flaws**: Flaws within the underlying UPI payment switch or bank servers are outside PayShield's client-side scope.
3. **Official Merchant Fraud**: Legitimate registered merchants committing fraud after legitimate delivery are out of client-side structural scope.
