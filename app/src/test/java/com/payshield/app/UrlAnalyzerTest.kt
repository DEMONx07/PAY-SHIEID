package com.payshield.app

import com.payshield.app.security.qr.BrandEntry
import com.payshield.app.security.url.UrlAnalyzer
import org.junit.Assert.*
import org.junit.Test

class UrlAnalyzerTest {

    private val defaultBrands = listOf(
        BrandEntry("brand-001", "State Bank of India", listOf("SBI", "State Bank"), listOf("sbi.co.in", "sbin")),
        BrandEntry("brand-002", "HDFC Bank", listOf("HDFC"), listOf("hdfcbank.com"))
    )

    @Test
    fun extractUrls_findsMultipleHttpAndHttpsUrls() {
        val text = "Check this out http://192.168.1.1/pay and also https://sbi-security.verify-update.phishing.com/login"
        val urls = UrlAnalyzer.extractUrls(text)

        assertEquals(2, urls.size)
        assertTrue(urls.contains("http://192.168.1.1/pay"))
    }

    @Test
    fun analyzeUrls_ipHostAddress_detectsIpHostSignal() {
        val urls = listOf("http://192.168.1.50/update-kyc")
        val signals = UrlAnalyzer.analyzeUrls(urls, defaultBrands)

        assertTrue(signals.any { it.id == "URL_IP_HOST_ADDRESS" })
    }

    @Test
    fun analyzeUrls_punycodeDomain_detectsPunycodeSignal() {
        val urls = listOf("https://xn--sbi-9ja.com/login")
        val signals = UrlAnalyzer.analyzeUrls(urls, defaultBrands)

        assertTrue(signals.any { it.id == "URL_PUNYCODE_IDN" })
    }

    @Test
    fun analyzeUrls_brandImpersonationInUrl_detectsBrandMismatchSignal() {
        val urls = listOf("https://sbi-bank.kyc-verify-portal.com/update")
        val signals = UrlAnalyzer.analyzeUrls(urls, defaultBrands)

        assertTrue(signals.any { it.id.startsWith("URL_BRAND_MISMATCH") })
    }

    @Test
    fun analyzeUrls_legitimateBrandDomain_doesNotTriggerBrandMismatch() {
        val urls = listOf("https://sbin.sbi.co.in/netbanking")
        val signals = UrlAnalyzer.analyzeUrls(urls, defaultBrands)

        assertFalse(signals.any { it.id.startsWith("URL_BRAND_MISMATCH") })
    }
}
