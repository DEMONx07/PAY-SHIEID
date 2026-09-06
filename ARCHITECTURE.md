# ARCHITECTURE.md — PayShield Architecture & Security Design

PayShield enforces a decoupled, deterministic security engine architecture with strict separation of concerns.

```text
                                PAYSHIELD
                                    │
         ┌──────────────────────────┴──────────────────────────┐
         ▼                                                     ▼
     QR SHIELD                                          MESSAGE SHIELD
         │                                                     │
CameraX (Scan Lock & Debounce)                           Paste / Share
         │                                                     │
Bundled ML Kit (QR code only)                          Text Normalizer (Raw + Norm)
         │                                                     │
Defensive UPI Parser                                  Rule Extractors
         │                                                     │
Defensive UPI Validator                               URL Analyzer
         │                                                     │
         └──────────────────────────┬──────────────────────────┘
                                    ▼
                            SIGNAL NORMALIZER
                                    │
                                    ▼
                            SIGNAL AGGREGATOR
                                    │
                                    ▼
                               RISK ENGINE
                                    │
                         ┌──────────┴──────────┐
                         ▼                     ▼
                     RISK SCORE            EVIDENCE
                 (Category Capped)        CONFIDENCE
                         │                     │
                         └──────────┬──────────┘
                                    ▼
                             DECISION POLICY
                                    │
                       ┌────────────┼────────────┐
                       ▼            ▼            ▼
                    LOW RISK    SUSPICIOUS   HIGH RISK
                                    │
                                    ▼
                         DETERMINISTIC EXPLANATION
                                    │
                                    ▼
                           OPTIONAL LOCAL LLM
                                    │
                                    ▼
                          LLM SCHEMA VALIDATOR
```

---

## Modular Pipeline Breakdown

1. **UpiParser & UpiValidator**:
   - `UpiParser` extracts query parameters safely into `UpiPayload`. Enforces `MAX_QR_PAYLOAD_LENGTH` (2048 chars) and `MAX_PARAM_COUNT` (32).
   - `UpiValidator` validates structural VPA syntax (`username@handle`), payment amount, and currency.

2. **TextNormalizer & MessageRuleEngine**:
   - `TextNormalizer` generates `NormalizedText` (de-obfuscating punctuation tactics while keeping `rawText`).
   - `MessageRuleEngine` extracts behavioral signals (`MSG_OTP_REQUEST`, `MSG_ACCOUNT_THREAT`, `MSG_KYC_PRESSURE`, `MSG_REMOTE_APP_REQUEST`).

3. **UrlAnalyzer**:
   - Performs offline structural analysis on extracted URLs. Checks for IP address host, Punycode/IDN encoding, excessive subdomains, and brand domain mismatches.

4. **SignalNormalizer & RiskEngine**:
   - `SignalNormalizer` deduplicates equivalent signals.
   - `RiskEngine` calculates capped scores across 4 categories:
     - `IDENTITY` (Max 35)
     - `CONTENT` (Max 30)
     - `URL` (Max 25)
     - `CONTEXT` (Max 10)
   - Computes `Evidence Confidence` (`LOW`, `MEDIUM`, `HIGH`).

5. **DecisionPolicy**:
   - Applies minimum-evidence rules before assigning `HIGH_RISK`. `HIGH_RISK` requires at least 1 critical high-reliability signal OR strong signals from >= 2 independent categories.

6. **ExplanationEngine & LLM Bridge**:
   - `ExplanationEngine` generates rich deterministic explanations (Source of Truth).
   - `LlmExplanationBridge` formats prompts with `<<<USER_CONTENT>>>` delimiters.
   - `LlmSchemaValidator` parses LLM JSON and ensures security scores are never overridden.
