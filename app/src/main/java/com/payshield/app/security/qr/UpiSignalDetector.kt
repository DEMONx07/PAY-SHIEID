package com.payshield.app.security.qr

import com.payshield.app.domain.model.*

object UpiSignalDetector {

    fun detectSignals(
        payload: UpiPayload,
        validation: UpiValidator.ValidationResult,
        knownBrands: List<BrandEntry>,
        flaggedPatterns: List<ReputationPatternEntry>
    ): List<RiskSignal> {
        val signals = mutableListOf<RiskSignal>()

        // 1. Missing or Malformed VPA
        if (payload.pa == null) {
            signals.add(
                RiskSignal(
                    id = "UPI_MISSING_PA",
                    title = "Missing Recipient Address",
                    explanation = "The QR code does not specify a payment recipient Virtual Payment Address (pa).",
                    points = 35,
                    category = Category.IDENTITY,
                    severity = Severity.HIGH_RISK,
                    reliability = Reliability.HIGH
                )
            )
        } else if (!validation.isValidVpaFormat) {
            signals.add(
                RiskSignal(
                    id = "UPI_MALFORMED_VPA",
                    title = "Malformed Payment Address Format",
                    explanation = "The recipient address '${payload.pa}' does not match valid UPI VPA syntax.",
                    points = 25,
                    category = Category.IDENTITY,
                    severity = Severity.SUSPICIOUS,
                    reliability = Reliability.HIGH
                )
            )
        }

        // 2. Local Reputation Check: Unknown vs Flagged Test Pattern
        if (payload.pa != null) {
            val matchedFlagged = flaggedPatterns.find { 
                payload.pa.contains(it.pattern, ignoreCase = true) || payload.raw.contains(it.pattern, ignoreCase = true)
            }
            if (matchedFlagged != null) {
                signals.add(
                    RiskSignal(
                        id = "UPI_FLAGGED_LOCAL_REPUTATION",
                        title = "Known Prototype Risk Pattern",
                        explanation = "The payment address matches a prototype local test risk pattern: '${matchedFlagged.pattern}'.",
                        points = 35,
                        category = Category.IDENTITY,
                        severity = Severity.HIGH_RISK,
                        reliability = Reliability.HIGH
                    )
                )
            } else {
                // Unknown local reputation: Explicitly tagged as UNKNOWN / UNCERTAINTY (Not proof of fraud!)
                val vpaHandle = payload.pa.substringAfter("@", "").lowercase()
                val isKnownHandle = knownBrands.any { brand ->
                    brand.knownVpaDomains.any { domain -> vpaHandle.contains(domain) }
                }
                if (!isKnownHandle) {
                    signals.add(
                        RiskSignal(
                            id = "UPI_UNKNOWN_LOCAL_REPUTATION",
                            title = "Unverified Local Destination",
                            explanation = "This recipient address could not be independently verified offline against known brand handles.",
                            points = 5,
                            category = Category.IDENTITY,
                            severity = Severity.SUSPICIOUS,
                            reliability = Reliability.LOW,
                            isUnknown = true
                        )
                    )
                }
            }
        }

        // 3. Brand Impersonation Check (Name claims brand, but VPA handle doesn't match)
        if (payload.pn != null && payload.pa != null) {
            for (brand in knownBrands) {
                val nameMatch = brand.keywords.any { payload.pn.contains(it, ignoreCase = true) }
                val handleMatch = brand.knownVpaDomains.any { payload.pa.lowercase().contains(it) }

                if (nameMatch && !handleMatch) {
                    signals.add(
                        RiskSignal(
                            id = "UPI_BRAND_IMPERSONATION_MISMATCH",
                            title = "Possible Brand Impersonation",
                            explanation = "The payee claims to be '${brand.name}' ('${payload.pn}'), but the address handle '${payload.pa}' does not match official domain handles.",
                            points = 30,
                            category = Category.IDENTITY,
                            severity = Severity.HIGH_RISK,
                            reliability = Reliability.HIGH
                        )
                    )
                    break
                }
            }
        }

        // 4. Non-standard Currency
        if (!validation.isSupportedCurrency && payload.currency != null) {
            signals.add(
                RiskSignal(
                    id = "UPI_NON_STANDARD_CURRENCY",
                    title = "Non-Standard Currency",
                    explanation = "The payment QR specifies currency '${payload.currency}' instead of INR.",
                    points = 10,
                    category = Category.CONTEXT,
                    severity = Severity.SUSPICIOUS,
                    reliability = Reliability.MEDIUM
                )
            )
        }

        // 5. Excessive Unknown Parameters
        if (payload.unknownParams.size > 5) {
            signals.add(
                RiskSignal(
                    id = "UPI_EXCESSIVE_UNKNOWN_PARAMS",
                    title = "Suspicious Payment Parameters",
                    explanation = "The QR payload contains an unusually large number of unrecognized query parameters (${payload.unknownParams.size}).",
                    points = 10,
                    category = Category.CONTEXT,
                    severity = Severity.SUSPICIOUS,
                    reliability = Reliability.MEDIUM
                )
            )
        }

        return signals
    }
}

// Data models matching brands.json and reputation.json assets
data class BrandEntry(
    val id: String,
    val name: String,
    val keywords: List<String>,
    val knownVpaDomains: List<String>
)

data class ReputationPatternEntry(
    val id: String,
    val pattern: String,
    val type: String,
    val severity: String,
    val description: String
)
