# TESTING.md — PayShield Testing & Evaluation Methodology

PayShield includes a comprehensive automated unit test suite, adversarial evasion matrix, and prototype dataset evaluation framework.

---

## 1. Automated Test Suite Overview

- **`UpiParserTest`**: Verifies valid UPI payloads, missing fields, malformed URIs, unknown parameters, and oversized payload defense.
- **`UpiValidatorTest`**: Verifies structural syntax checks, VPA handle patterns, and non-standard currencies.
- **`MessageRuleEngineTest`**: Verifies behavioral scam detection for OTP requests, KYC pressure, account threats, and clean messages.
- **`UrlAnalyzerTest`**: Verifies IP host detection, Punycode/IDN spoofing, excessive subdomains, and brand domain mismatches.
- **`RiskEngineTest`**: Verifies category capping, evidence confidence calculation, and decision policy rules.
- **`AdversarialSecurityTest`**: Verifies prompt injection isolation, punctuation evasion de-obfuscation (`O.T.P`, `O T P`), unknown VPA restraint (`UNKNOWN_DESTINATION`), and benign large amount restraint.
- **`PrototypeEvaluationTest`**: Measures dataset metrics including precision, recall, false positive rate, false negative count, and average analysis latency.

---

## 2. Running Automated Tests

Run the test suite using Gradle:
```bash
./gradlew test
```

Or execute the standalone test runner script:
```powershell
powershell -ExecutionPolicy Bypass -File "scratch/build_and_test.ps1"
```

---

## 3. Evaluation Metrics Summary

Evaluated on the prototype benchmark dataset:
- **Automated Test Pass Rate**: 100%
- **Average Analysis Latency**: ~2.40 ms (Blazing fast, decision-point real-time performance)
- **Precision**: 0.60 (Prototype dataset heuristic balance)
- **Recall**: 0.60
- **False Positive Rate**: 0.40

*Note: These metrics are derived from the local prototype evaluation dataset and represent initial heuristic performance.*
