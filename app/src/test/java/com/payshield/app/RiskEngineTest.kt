package com.payshield.app

import com.payshield.app.domain.model.*
import com.payshield.app.security.engine.RiskEngine
import org.junit.Assert.*
import org.junit.Test

class RiskEngineTest {

    @Test
    fun calculateRisk_multipleSignalsSameCategory_appliesCategoryCap() {
        val signals = listOf(
            RiskSignal("S1", "Signal 1", "Exp 1", 30, Category.IDENTITY, Severity.HIGH_RISK, Reliability.HIGH),
            RiskSignal("S2", "Signal 2", "Exp 2", 30, Category.IDENTITY, Severity.HIGH_RISK, Reliability.HIGH)
        )

        val result = RiskEngine.calculateRisk(signals)

        // IDENTITY cap is 35 points max
        assertEquals(35, result.score)
    }

    @Test
    fun calculateRisk_highReliabilityMultipleCategories_resultsInHighRiskAndHighConfidence() {
        val signals = listOf(
            RiskSignal("UPI_MISSING_PA", "Missing Recipient", "Exp", 35, Category.IDENTITY, Severity.HIGH_RISK, Reliability.HIGH),
            RiskSignal("MSG_OTP_REQUEST", "OTP Request", "Exp", 30, Category.CONTENT, Severity.HIGH_RISK, Reliability.HIGH)
        )

        val result = RiskEngine.calculateRisk(signals)

        assertEquals(Severity.HIGH_RISK, result.severity)
        assertEquals(Confidence.HIGH, result.confidence)
        assertEquals(65, result.score)
    }
}
