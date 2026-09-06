package com.payshield.app

import com.payshield.app.security.message.MessageRuleEngine
import com.payshield.app.security.message.TextNormalizer
import com.payshield.app.security.qr.BrandEntry
import org.junit.Assert.*
import org.junit.Test

class MessageRuleEngineTest {

    private val defaultBrands = listOf(
        BrandEntry("brand-001", "State Bank of India", listOf("SBI", "State Bank"), listOf("sbi", "sbin")),
        BrandEntry("brand-002", "HDFC Bank", listOf("HDFC"), listOf("hdfcbank"))
    )

    @Test
    fun extractSignals_otpRequest_detectsHighRiskOtpSignal() {
        val norm = TextNormalizer.normalize("SBI Alert: Please share your 6-digit OTP code to unblock your account.")
        val signals = MessageRuleEngine.extractSignals(norm, defaultBrands)

        assertTrue(signals.any { it.id == "MSG_OTP_REQUEST" })
        assertTrue(signals.any { it.id == "MSG_ACCOUNT_THREAT" })
    }

    @Test
    fun extractSignals_kycAndRemoteAppRequest_detectsKycAndRemoteAppSignals() {
        val norm = TextNormalizer.normalize("Urgent: Update your KYC today by installing AnyDesk app or your PAN card will be blocked.")
        val signals = MessageRuleEngine.extractSignals(norm, defaultBrands)

        assertTrue(signals.any { it.id == "MSG_KYC_PRESSURE" })
        assertTrue(signals.any { it.id == "MSG_REMOTE_APP_REQUEST" })
    }

    @Test
    fun extractSignals_cleanMessage_returnsNoScamSignals() {
        val norm = TextNormalizer.normalize("Hey, are we still meeting for lunch tomorrow at 1 PM?")
        val signals = MessageRuleEngine.extractSignals(norm, defaultBrands)

        assertTrue(signals.isEmpty())
    }
}
