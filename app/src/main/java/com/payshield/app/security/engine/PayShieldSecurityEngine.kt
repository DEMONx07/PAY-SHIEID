package com.payshield.app.security.engine

import android.content.Context
import com.payshield.app.domain.model.*
import com.payshield.app.security.message.MessageRuleEngine
import com.payshield.app.security.message.TextNormalizer
import com.payshield.app.security.qr.*
import com.payshield.app.security.url.UrlAnalyzer
import org.json.JSONObject
import java.io.InputStream

class PayShieldSecurityEngine private constructor(
    val knownBrands: List<BrandEntry>,
    val flaggedPatterns: List<ReputationPatternEntry>
) : RiskAnalyzer<String> {

    companion object {
        @Volatile
        private var INSTANCE: PayShieldSecurityEngine? = null

        fun getInstance(context: Context): PayShieldSecurityEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: loadEngineFromAssets(context).also { INSTANCE = it }
            }
        }

        fun createStandalone(
            brands: List<BrandEntry> = emptyList(),
            patterns: List<ReputationPatternEntry> = emptyList()
        ): PayShieldSecurityEngine {
            return PayShieldSecurityEngine(brands, patterns)
        }

        private fun loadEngineFromAssets(context: Context): PayShieldSecurityEngine {
            val brands = loadBrands(context)
            val patterns = loadPatterns(context)
            return PayShieldSecurityEngine(brands, patterns)
        }

        private fun loadBrands(context: Context): List<BrandEntry> {
            return try {
                val jsonString = context.assets.open("data/brands.json").bufferedReader().use { it.readText() }
                val obj = JSONObject(jsonString)
                val array = obj.getJSONArray("brands")
                val list = mutableListOf<BrandEntry>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val id = item.getString("id")
                    val name = item.getString("name")
                    val kwArray = item.getJSONArray("keywords")
                    val keywords = (0 until kwArray.length()).map { kwArray.getString(it) }
                    val vpaArray = item.getJSONArray("knownVpaDomains")
                    val vpaDomains = (0 until vpaArray.length()).map { vpaArray.getString(it) }
                    list.add(BrandEntry(id, name, keywords, vpaDomains))
                }
                list
            } catch (_: Exception) {
                defaultBrands()
            }
        }

        private fun loadPatterns(context: Context): List<ReputationPatternEntry> {
            return try {
                val jsonString = context.assets.open("data/reputation.json").bufferedReader().use { it.readText() }
                val obj = JSONObject(jsonString)
                val array = obj.getJSONArray("knownPatterns")
                val list = mutableListOf<ReputationPatternEntry>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    list.add(
                        ReputationPatternEntry(
                            id = item.getString("id"),
                            pattern = item.getString("pattern"),
                            type = item.getString("type"),
                            severity = item.getString("severity"),
                            description = item.getString("description")
                        )
                    )
                }
                list
            } catch (_: Exception) {
                defaultPatterns()
            }
        }

        private fun defaultBrands(): List<BrandEntry> = listOf(
            BrandEntry("brand-001", "State Bank of India", listOf("SBI", "State Bank", "sbi"), listOf("sbi", "sbin", "ysbi")),
            BrandEntry("brand-002", "HDFC Bank", listOf("HDFC", "hdfc"), listOf("hdfcbank", "hdfc")),
            BrandEntry("brand-003", "ICICI Bank", listOf("ICICI", "icici"), listOf("icici", "icicibank")),
            BrandEntry("brand-004", "Amazon", listOf("Amazon", "amazon"), listOf("amazon", "apl")),
            BrandEntry("brand-005", "Flipkart", listOf("Flipkart", "flipkart"), listOf("fk", "flipkart")),
            BrandEntry("brand-006", "PhonePe", listOf("PhonePe", "phonepe"), listOf("ybl", "ibl", "axl")),
            BrandEntry("brand-007", "Google Pay", listOf("GPay", "gpay"), listOf("okaxis", "okicici", "oksbi", "okhdfcbank")),
            BrandEntry("brand-008", "Paytm", listOf("Paytm", "paytm"), listOf("paytm"))
        )

        private fun defaultPatterns(): List<ReputationPatternEntry> = listOf(
            ReputationPatternEntry("rep-test-001", "scam.test@upi", "FLAGGED_TEST_PATTERN", "HIGH", "Known test risk pattern"),
            ReputationPatternEntry("rep-test-002", "lottery.winner@paytm", "FLAGGED_TEST_PATTERN", "HIGH", "Known test scam pattern")
        )
    }

    override fun analyze(input: String): RiskResult {
        return if (input.trim().startsWith("upi://", ignoreCase = true) || input.contains("pa=")) {
            analyzeQrPayload(input)
        } else {
            analyzeMessageText(input)
        }
    }

    fun analyzeQrPayload(rawQrPayload: String): RiskResult {
        val parsed = UpiParser.parse(rawQrPayload)
        val validation = UpiValidator.validate(parsed)
        val detectedSignals = UpiSignalDetector.detectSignals(parsed, validation, knownBrands, flaggedPatterns)
        return RiskEngine.calculateRisk(detectedSignals)
    }

    fun analyzeMessageText(rawText: String): RiskResult {
        val normalized = TextNormalizer.normalize(rawText)
        val messageSignals = MessageRuleEngine.extractSignals(normalized, knownBrands)
        val extractedUrls = UrlAnalyzer.extractUrls(normalized.rawText)
        val urlSignals = UrlAnalyzer.analyzeUrls(extractedUrls, knownBrands)

        val combined = messageSignals + urlSignals
        return RiskEngine.calculateRisk(combined)
    }
}
