/*
 * PAYSHIELD SECURITY PRINCIPLES:
 * 1. External content is untrusted.
 * 2. The deterministic security engine is authoritative.
 * 3. The LLM is non-authoritative.
 * 4. Unknown does not mean malicious.
 * 5. Risk does not mean certainty.
 * 6. The application warns; it does not guarantee or authorize payments.
 */

package com.payshield.app.domain.model

object SecurityConstants {
    const val MAX_QR_PAYLOAD_LENGTH = 2048
    const val MAX_PARAM_COUNT = 32
    const val MAX_PARAM_LENGTH = 256
    const val MAX_TEXT_LENGTH = 10000
    const val MAX_URL_LENGTH = 1024
    const val MAX_AMOUNT_DIGITS = 12

    // Category caps for scoring balance
    const val CAP_IDENTITY = 35
    const val CAP_CONTENT = 30
    const val CAP_URL = 25
    const val CAP_CONTEXT = 10
    const val MAX_TOTAL_SCORE = 100
}

enum class Category {
    IDENTITY,
    CONTENT,
    URL,
    CONTEXT
}

enum class Severity {
    LOW_RISK,
    SUSPICIOUS,
    HIGH_RISK
}

enum class Confidence {
    LOW,
    MEDIUM,
    HIGH
}

enum class Reliability {
    HIGH,
    MEDIUM,
    LOW
}

data class UpiPayload(
    val pa: String?,
    val pn: String?,
    val am: Double?,
    val tr: String?,
    val currency: String?,
    val raw: String,
    val unknownParams: Map<String, String> = emptyMap()
)

data class RiskSignal(
    val id: String,
    val title: String,
    val explanation: String,
    val points: Int,
    val category: Category,
    val severity: Severity,
    val reliability: Reliability = Reliability.MEDIUM,
    val isUnknown: Boolean = false
)

data class RiskResult(
    val score: Int,
    val severity: Severity,
    val confidence: Confidence,
    val signals: List<RiskSignal>,
    val recommendation: String,
    val explanation: String,
    val decisionPolicyReason: String
)

interface RiskAnalyzer<T> {
    fun analyze(input: T): RiskResult
}
