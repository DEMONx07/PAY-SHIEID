package com.payshield.app.security.explanation

import com.payshield.app.domain.model.Confidence
import com.payshield.app.domain.model.RiskResult
import com.payshield.app.domain.model.Severity

object ExplanationEngine {

    data class FormattedExplanation(
        val verdictBadge: String,
        val scoreText: String,
        val confidenceText: String,
        val policyReason: String,
        val whyBullets: List<String>,
        val whatToDoBullets: List<String>,
        val isAiGenerated: Boolean = false
    )

    fun generateDeterministicExplanation(result: RiskResult): FormattedExplanation {
        val verdictBadge = when (result.severity) {
            Severity.HIGH_RISK -> "HIGH RISK"
            Severity.SUSPICIOUS -> "SUSPICIOUS"
            Severity.LOW_RISK -> "LOW RISK"
        }

        val scoreText = "${result.score} / 100"
        val confidenceText = "Evidence Confidence: ${result.confidence.name}"

        val whyBullets = if (result.signals.isEmpty()) {
            listOf("No suspicious structural or behavioral signals were identified.")
        } else {
            result.signals.map { signal ->
                "${signal.title}: ${signal.explanation}"
            }
        }

        val whatToDoBullets = when (result.severity) {
            Severity.HIGH_RISK -> listOf(
                "Do NOT enter your UPI PIN, authorization code, or click embedded links.",
                "Do NOT share OTPs or install requested screen-sharing applications.",
                "Independently contact the recipient using a verified phone number."
            )
            Severity.SUSPICIOUS -> listOf(
                "Verify the payee address or sender identity through an official separate channel.",
                "Inspect any embedded web links carefully before opening.",
                "If an unknown party requested this payment, confirm their credentials first."
            )
            Severity.LOW_RISK -> listOf(
                "Standard low-risk assessment. Always double-check payee name before entering PIN.",
                "Ensure you never share OTPs or PINs with anyone."
            )
        }

        return FormattedExplanation(
            verdictBadge = verdictBadge,
            scoreText = scoreText,
            confidenceText = confidenceText,
            policyReason = result.decisionPolicyReason,
            whyBullets = whyBullets,
            whatToDoBullets = whatToDoBullets,
            isAiGenerated = false
        )
    }
}
