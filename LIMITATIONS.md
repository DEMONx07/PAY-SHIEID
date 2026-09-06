# LIMITATIONS.md — Explicit System Boundaries & Honest Uncertainty

PayShield operates as an offline decision-assistance security layer. To maintain security integrity and honest communication, PayShield explicitly documents its limitations.

---

## Technical & Scope Limitations

1. **Unknown Recipient Address ≠ Automatic Fraud**:
   - An unknown Virtual Payment Address (VPA) or domain handle simply means the recipient does not exist in the local prototype reputation dataset. It does NOT prove fraud. PayShield tags unknown destinations as `UNKNOWN_DESTINATION` (+5 pts uncertainty), preserving the principle: *Unknown does not equal fraud*.

2. **Payment Amount Does Not Prove Fraud**:
   - Large payment amounts alone do not indicate malicious activity. PayShield's decision policy requires independent critical or structural threat signals before raising a `HIGH_RISK` verdict.

3. **Offline Local Reputation Dataset Is Incomplete**:
   - Prototype local datasets (`brands.json`, `reputation.json`) provide heuristic signatures and cannot represent an exhaustive national fraud database.

4. **Risk Score Is Heuristic, Not a Fraud Probability**:
   - Risk scores (0–100) represent cumulative heuristic evidence density. They are not statistically calibrated probability percentages.

5. **Local LLM Explanation Runtime**:
   - Local LLM execution depends on target device hardware capabilities. If the LLM runtime fails, times out, or produces malformed JSON, PayShield seamlessly falls back to the deterministic explanation engine.

6. **Warning vs Payment Authorization Scope**:
   - PayShield provides risk assessment and safety recommendations. It does not interface with bank payment switches or block UPI authorizations directly. The user remains the ultimate decision-maker.

7. **Compromised Target Device**:
   - Security analysis assumes an uncompromised Android OS environment. Kernel-level OS compromise or root-level keyloggers are out of scope.
