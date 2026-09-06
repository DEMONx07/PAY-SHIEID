package com.payshield.app.security.engine

import com.payshield.app.domain.model.*

object RiskEngine {

    fun calculateRisk(rawSignals: List<RiskSignal>): RiskResult {
        val signals = SignalNormalizer.normalizeSignals(rawSignals)

        // 1. Group points by category and apply category caps
        var identityPoints = 0
        var contentPoints = 0
        var urlPoints = 0
        var contextPoints = 0

        for (signal in signals) {
            when (signal.category) {
                Category.IDENTITY -> identityPoints += signal.points
                Category.CONTENT -> contentPoints += signal.points
                Category.URL -> urlPoints += signal.points
                Category.CONTEXT -> contextPoints += signal.points
            }
        }

        val cappedIdentity = identityPoints.coerceAtMost(SecurityConstants.CAP_IDENTITY)
        val cappedContent = contentPoints.coerceAtMost(SecurityConstants.CAP_CONTENT)
        val cappedUrl = urlPoints.coerceAtMost(SecurityConstants.CAP_URL)
        val cappedContext = contextPoints.coerceAtMost(SecurityConstants.CAP_CONTEXT)

        val totalScore = (cappedIdentity + cappedContent + cappedUrl + cappedContext)
            .coerceAtMost(SecurityConstants.MAX_TOTAL_SCORE)

        // 2. Evidence Confidence Calculation (Degree of supporting evidence quality)
        val nonUnknownSignals = signals.filter { !it.isUnknown }
        val highReliabilityCount = nonUnknownSignals.count { it.reliability == Reliability.HIGH }
        val activeCategories = nonUnknownSignals.map { it.category }.distinct()

        val confidence = when {
            highReliabilityCount >= 2 || (highReliabilityCount >= 1 && activeCategories.size >= 2) -> Confidence.HIGH
            highReliabilityCount == 1 || nonUnknownSignals.size >= 2 -> Confidence.MEDIUM
            else -> Confidence.LOW
        }

        // 3. Decision Policy Evaluation
        val decision = DecisionPolicy.evaluate(totalScore, signals, confidence)

        // 4. Recommendation Generation
        val recommendation = when (decision.severity) {
            Severity.HIGH_RISK -> "DO NOT PROCEED. High risk of fraud or credential theft detected. Independently verify recipient."
            Severity.SUSPICIOUS -> "EXERCISE CAUTION. Suspicious indicators found. Verify recipient through a trusted separate channel."
            Severity.LOW_RISK -> "NO HIGH RISK DETECTED. Proceed with standard verification."
        }

        // 5. Deterministic Explanation Summary
        val explanationSummary = buildString {
            append("Risk Assessment: ${decision.severity.name.replace("_", " ")} ($totalScore/100). ")
            append("Evidence Confidence: ${confidence.name}. ")
            if (signals.isEmpty()) {
                append("No risk signals detected.")
            } else {
                val signalTitles = signals.take(3).joinToString(", ") { it.title }
                append("Primary signals: $signalTitles.")
            }
        }

        return RiskResult(
            score = totalScore,
            severity = decision.severity,
            confidence = confidence,
            signals = signals,
            recommendation = recommendation,
            explanation = explanationSummary,
            decisionPolicyReason = decision.policyReason
        )
    }
}
