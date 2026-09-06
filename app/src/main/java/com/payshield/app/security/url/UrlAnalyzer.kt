package com.payshield.app.security.url

import com.payshield.app.domain.model.*
import com.payshield.app.security.qr.BrandEntry
import java.net.URI
import java.util.regex.Pattern

object UrlAnalyzer {

    private val URL_REGEX = Pattern.compile(
        "(https?://[\\w-]+(\\.[\\w-]+)+[\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])",
        Pattern.CASE_INSENSITIVE
    )

    private val IP_HOST_REGEX = Pattern.compile(
        "^https?://(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d+)?(?:/.*)?$",
        Pattern.CASE_INSENSITIVE
    )

    fun extractUrls(text: String): List<String> {
        val urls = mutableListOf<String>()
        val matcher = URL_REGEX.matcher(text)
        while (matcher.find()) {
            val url = matcher.group(1)
            if (url != null && url.length <= SecurityConstants.MAX_URL_LENGTH) {
                urls.add(url)
            }
        }
        return urls
    }

    fun analyzeUrls(urls: List<String>, knownBrands: List<BrandEntry>): List<RiskSignal> {
        val signals = mutableListOf<RiskSignal>()

        for (urlStr in urls) {
            val uri = try {
                URI.create(urlStr)
            } catch (_: Exception) {
                signals.add(
                    RiskSignal(
                        id = "URL_MALFORMED_STRUCTURE",
                        title = "Malformed URL Structure",
                        explanation = "The link structure could not be standardly parsed offline.",
                        points = 15,
                        category = Category.URL,
                        severity = Severity.SUSPICIOUS,
                        reliability = Reliability.HIGH
                    )
                )
                continue
            }

            val host = uri.host ?: ""
            val rawUrl = urlStr.lowercase()

            // 1. IP Address Host check
            if (IP_HOST_REGEX.matcher(urlStr).matches() || host.matches(Regex("^(?:\\d{1,3}\\.){3}\\d{1,3}$"))) {
                signals.add(
                    RiskSignal(
                        id = "URL_IP_HOST_ADDRESS",
                        title = "Direct IP Address Host",
                        explanation = "This link points directly to a raw numerical IP address ($host) rather than a registered domain name.",
                        points = 25,
                        category = Category.URL,
                        severity = Severity.HIGH_RISK,
                        reliability = Reliability.HIGH
                    )
                )
            }

            // 2. Punycode / IDN check
            if (host.startsWith("xn--", ignoreCase = true) || host.contains(".xn--")) {
                signals.add(
                    RiskSignal(
                        id = "URL_PUNYCODE_IDN",
                        title = "Punycode / Homograph Character Encoding",
                        explanation = "This domain uses Punycode encoding ('$host'), which is commonly used to spoof visually identical brand names.",
                        points = 25,
                        category = Category.URL,
                        severity = Severity.HIGH_RISK,
                        reliability = Reliability.HIGH
                    )
                )
            }

            // 3. Excessive Subdomains
            val subdomains = host.split(".")
            if (subdomains.size >= 4 && !host.matches(Regex("^(?:\\d{1,3}\\.){3}\\d{1,3}$"))) {
                signals.add(
                    RiskSignal(
                        id = "URL_EXCESSIVE_SUBDOMAINS",
                        title = "Excessive Subdomains",
                        explanation = "The link host '$host' contains ${subdomains.size} domain levels, often used to conceal the actual destination.",
                        points = 15,
                        category = Category.URL,
                        severity = Severity.SUSPICIOUS,
                        reliability = Reliability.MEDIUM
                    )
                )
            }

            // 4. Userinfo component
            if (uri.userInfo != null) {
                signals.add(
                    RiskSignal(
                        id = "URL_USERINFO_COMPONENT",
                        title = "Embedded User Credentials in Link",
                        explanation = "The link contains user credentials in the authority section, which can obscure the real destination.",
                        points = 20,
                        category = Category.URL,
                        severity = Severity.HIGH_RISK,
                        reliability = Reliability.HIGH
                    )
                )
            }

            // 5. Brand Name Embedded in Non-Brand Host
            for (brand in knownBrands) {
                val matchesBrandKeyword = brand.keywords.any { rawUrl.contains(it.lowercase()) }
                if (matchesBrandKeyword) {
                    val isOfficialDomain = brand.knownVpaDomains.any { domain ->
                        host.lowercase().endsWith(domain) || host.lowercase().contains(".$domain.")
                    }
                    if (!isOfficialDomain) {
                        signals.add(
                            RiskSignal(
                                id = "URL_BRAND_MISMATCH_${brand.id}",
                                title = "Possible Brand Impersonation in Link",
                                explanation = "The link contains '${brand.name}', but the destination host '$host' does not match official domain records.",
                                points = 25,
                                category = Category.URL,
                                severity = Severity.HIGH_RISK,
                                reliability = Reliability.HIGH
                            )
                        )
                        break
                    }
                }
            }

            // 6. Suspicious Path/Query Keywords
            val suspiciousKeywords = listOf("login", "verify", "kyc", "update", "bank", "account", "secure", "signin", "payment")
            val pathQueryStr = (uri.path ?: "") + (uri.query ?: "")
            if (suspiciousKeywords.any { pathQueryStr.lowercase().contains(it) }) {
                signals.add(
                    RiskSignal(
                        id = "URL_SUSPICIOUS_PATH_KEYWORDS",
                        title = "Suspicious Path / Query Characteristics",
                        explanation = "The link path contains sensitive keywords commonly associated with credential collection.",
                        points = 10,
                        category = Category.URL,
                        severity = Severity.SUSPICIOUS,
                        reliability = Reliability.MEDIUM
                    )
                )
            }

            // 7. Unusual URL Length
            if (urlStr.length > 100) {
                signals.add(
                    RiskSignal(
                        id = "URL_UNUSUAL_LENGTH",
                        title = "Unusual Link Length",
                        explanation = "The link string is unusually long (${urlStr.length} characters).",
                        points = 5,
                        category = Category.URL,
                        severity = Severity.SUSPICIOUS,
                        reliability = Reliability.LOW
                    )
                )
            }
        }

        return signals
    }
}
