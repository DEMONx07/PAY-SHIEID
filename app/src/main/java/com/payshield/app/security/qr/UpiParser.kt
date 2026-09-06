package com.payshield.app.security.qr

import com.payshield.app.domain.model.SecurityConstants
import com.payshield.app.domain.model.UpiPayload
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object UpiParser {

    /**
     * Safely parses raw string into UpiPayload.
     * Enforces defensive limits on input size and parameter count.
     * Never throws exceptions on malformed input.
     */
    fun parse(rawInput: String?): UpiPayload {
        if (rawInput.isNullOrBlank()) {
            return UpiPayload(null, null, null, null, null, rawInput ?: "")
        }

        // Defensive length restriction
        val truncatedInput = if (rawInput.length > SecurityConstants.MAX_QR_PAYLOAD_LENGTH) {
            rawInput.substring(0, SecurityConstants.MAX_QR_PAYLOAD_LENGTH)
        } else {
            rawInput
        }

        val cleanedInput = truncatedInput.trim()
        val queryPart = when {
            cleanedInput.startsWith("upi://pay?", ignoreCase = true) -> cleanedInput.substring(10)
            cleanedInput.contains("?") -> cleanedInput.substringAfter("?")
            else -> ""
        }

        if (queryPart.isEmpty()) {
            return UpiPayload(null, null, null, null, null, cleanedInput)
        }

        var pa: String? = null
        var pn: String? = null
        var am: Double? = null
        var tr: String? = null
        var currency: String? = null
        val unknownMap = mutableMapOf<String, String>()

        val pairs = queryPart.split("&")
        var paramCount = 0

        for (pair in pairs) {
            if (paramCount >= SecurityConstants.MAX_PARAM_COUNT) break
            if (pair.isEmpty()) continue

            val parts = pair.split("=", limit = 2)
            val key = safeUrlDecode(parts[0].trim().take(SecurityConstants.MAX_PARAM_LENGTH))
            val value = if (parts.size > 1) {
                safeUrlDecode(parts[1].trim().take(SecurityConstants.MAX_PARAM_LENGTH))
            } else ""

            if (key.isEmpty()) continue
            paramCount++

            when (key.lowercase()) {
                "pa" -> pa = value.ifEmpty { null }
                "pn" -> pn = value.ifEmpty { null }
                "am" -> am = safeParseDouble(value)
                "tr" -> tr = value.ifEmpty { null }
                "cu" -> currency = value.ifEmpty { null }
                else -> unknownMap[key] = value
            }
        }

        return UpiPayload(
            pa = pa,
            pn = pn,
            am = am,
            tr = tr,
            currency = currency,
            raw = cleanedInput,
            unknownParams = unknownMap
        )
    }

    private fun safeUrlDecode(value: String): String {
        return try {
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        } catch (_: Exception) {
            value
        }
    }

    private fun safeParseDouble(value: String): Double? {
        if (value.length > SecurityConstants.MAX_AMOUNT_DIGITS) return null
        return try {
            val parsed = value.toDouble()
            if (parsed >= 0 && !parsed.isNaN() && !parsed.isInfinite()) parsed else null
        } catch (_: Exception) {
            null
        }
    }
}
