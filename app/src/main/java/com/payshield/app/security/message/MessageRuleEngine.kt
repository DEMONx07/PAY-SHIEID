package com.payshield.app.security.message

import com.payshield.app.domain.model.*
import com.payshield.app.security.qr.BrandEntry

object MessageRuleEngine {

    fun extractSignals(
        normalized: NormalizedText,
        knownBrands: List<BrandEntry>
    ): List<RiskSignal> {
        val signals = mutableListOf<RiskSignal>()
        val text = normalized.normalizedText

        if (text.isEmpty()) return signals

        // 1. OTP / Verification Code Request
        val otpKeywords = listOf("otp", "verification code", "one time password", "one-time password", "secret pin", "login code", "passcode")
        if (otpKeywords.any { text.contains(it) }) {
            signals.add(
                RiskSignal(
                    id = "MSG_OTP_REQUEST",
                    title = "OTP / Verification Code Request",
                    explanation = "The message requests a one-time password (OTP) or security verification code. Legitimate institutions never ask for your OTP.",
                    points = 30,
                    category = Category.CONTENT,
                    severity = Severity.HIGH_RISK,
                    reliability = Reliability.HIGH
                )
            )
        }

        // 2. Account Suspension / Deactivation Threat
        val threatKeywords = listOf("blocked", "unblock", "block", "suspended", "deactivated", "closed today", "account freeze", "electricity bill unpaid", "power cutoff", "action required immediately")
        if (threatKeywords.any { text.contains(it) }) {
            signals.add(
                RiskSignal(
                    id = "MSG_ACCOUNT_THREAT",
                    title = "Account Suspension / Coercive Threat",
                    explanation = "The message threatens imminent account closure or service interruption to force immediate action.",
                    points = 20,
                    category = Category.CONTENT,
                    severity = Severity.HIGH_RISK,
                    reliability = Reliability.MEDIUM
                )
            )
        }

        // 3. KYC / Verification Pressure
        val kycKeywords = listOf("kyc", "kyc update", "verify kyc", "pancard", "pan card", "aadhaar", "document verification")
        if (kycKeywords.any { text.contains(it) }) {
            signals.add(
                RiskSignal(
                    id = "MSG_KYC_PRESSURE",
                    title = "KYC / Verification Pressure",
                    explanation = "The message demands urgent KYC or document verification, a common vector for credential harvesting.",
                    points = 25,
                    category = Category.CONTENT,
                    severity = Severity.HIGH_RISK,
                    reliability = Reliability.HIGH
                )
            )
        }

        // 4. Unknown App / Remote Control APK Request
        val remoteAppKeywords = listOf("anydesk", "quicksupport", "rustdesk", "teamviewer", "install app", "download apk", "support.apk")
        if (remoteAppKeywords.any { text.contains(it) }) {
            signals.add(
                RiskSignal(
                    id = "MSG_REMOTE_APP_REQUEST",
                    title = "Remote Control / APK Software Request",
                    explanation = "The message asks you to download an APK or install screen-sharing software, which grants total device access.",
                    points = 30,
                    category = Category.CONTENT,
                    severity = Severity.HIGH_RISK,
                    reliability = Reliability.HIGH
                )
            )
        }

        // 5. Urgency Pressure
        val urgencyKeywords = listOf("urgent", "immediately", "within 10 minutes", "within 24 hours", "expires today", "final notice")
        if (urgencyKeywords.any { text.contains(it) }) {
            signals.add(
                RiskSignal(
                    id = "MSG_HIGH_URGENCY",
                    title = "High Urgency Pressure",
                    explanation = "The message uses artificial time pressure to discourage independent verification.",
                    points = 10,
                    category = Category.CONTEXT,
                    severity = Severity.SUSPICIOUS,
                    reliability = Reliability.MEDIUM
                )
            )
        }

        // 6. Brand Mention in Message
        for (brand in knownBrands) {
            val hasBrand = brand.keywords.any { text.contains(it.lowercase()) }
            if (hasBrand) {
                signals.add(
                    RiskSignal(
                        id = "MSG_CLAIMED_BRAND_${brand.id}",
                        title = "Claimed Organization: ${brand.name}",
                        explanation = "The message references '${brand.name}'. PayShield checks whether embedded links match official brand domains.",
                        points = 5,
                        category = Category.IDENTITY,
                        severity = Severity.SUSPICIOUS,
                        reliability = Reliability.MEDIUM
                    )
                )
                break
            }
        }

        return signals
    }
}
