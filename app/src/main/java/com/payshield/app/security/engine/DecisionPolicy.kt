package com.payshield.app.security.engine

import com.payshield.app.domain.model.*

object DecisionPolicy {

    data class Decision(
        val severity: Severity,
        val policyReason: String
    )

    fun evaluate(score: Int, signals: List<RiskSignal>, confidence: Confidence): Decision {
        val nonUnknownSignals = signals.filter { !it.isUnknown }
        val highReliabilityCount = nonUnknownSignals.count { it.reliability == Reliability.HIGH }
        val activeCategories = nonUnknownSignals.map { it.category }.distinct()

        val hasCriticalSignal = nonUnknownSignals.any { signal ->
            signal.reliability == Reliability.HIGH && (
                signal.id.startsWith("UPI_MISSING_PA") ||
                signal.id.startsWith("UPI_FLAGGED") ||
                signal.id.startsWith("UPI_BRAND_IMPERSONATION") ||
                signal.id.startsWith("MSG_OTP_REQUEST") ||
                signal.id.startsWith("MSG_REMOTE_APP") ||
                signal.id.startsWith("URL_IP_HOST") ||
                signal.id.startsWith("URL_PUNYCODE") ||
                signal.id.startsWith("URL_BRAND_MISMATCH")
            )
        }

        // Decision Policy for HIGH RISK
        return when {
            score >= 50 && (hasCriticalSignal || (highReliabilityCount >= 2 && activeCategories.size >= 2)) -> {
                Decision(
                    severity = Severity.HIGH_RISK,
                    policyReason = if (hasCriticalSignal) {
                        "Critical security indicator detected (${nonUnknownSignals.firstOrNull { it.reliability == Reliability.HIGH }?.title ?: "Critical signal"})."
                    } else {
                        "High composite risk score ($score/100) supported by multiple independent evidence categories."
                    }
                )
            }
            score >= 25 || (score >= 50 && !hasCriticalSignal) -> {
                Decision(
                    severity = Severity.SUSPICIOUS,
                    policyReason = "Multiple suspicious indicators detected ($score/100). Requires recipient verification before proceeding."
                )
            }
            else -> {
                Decision(
                    severity = Severity.LOW_RISK,
                    policyReason = "Low risk score ($score/100). No critical threat indicators detected."
                )
            }
        }
    }
}
