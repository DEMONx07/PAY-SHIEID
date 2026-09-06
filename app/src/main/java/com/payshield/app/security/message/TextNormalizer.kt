package com.payshield.app.security.message

import com.payshield.app.domain.model.SecurityConstants
import java.text.Normalizer

data class NormalizedText(
    val rawText: String,
    val normalizedText: String
)

object TextNormalizer {

    /**
     * Normalizes text while preserving original raw string.
     * Enforces defensive max length boundary.
     * De-obfuscates common punctuation insertion tactics like O.T.P -> OTP, O T P -> OTP.
     */
    fun normalize(input: String?): NormalizedText {
        if (input.isNullOrBlank()) {
            return NormalizedText("", "")
        }

        // Defensive input limit
        val raw = if (input.length > SecurityConstants.MAX_TEXT_LENGTH) {
            input.substring(0, SecurityConstants.MAX_TEXT_LENGTH)
        } else {
            input
        }

        // 1. Unicode Normalization (NFKD)
        val nfkd = Normalizer.normalize(raw, Normalizer.Form.NFKD)
        
        // 2. Lowercase conversion
        var working = nfkd.lowercase()

        // 3. Remove zero-width spaces and control characters
        working = working.replace(Regex("[\\p{Cf}\\p{Cc}]"), "")

        // 4. De-obfuscate punctuation evasions in key acronyms (O.T.P -> otp, O T P -> otp, P.I.N -> pin)
        working = working.replace(Regex("o[.\\s_\\-*]+t[.\\s_\\-*]+p"), "otp")
        working = working.replace(Regex("p[.\\s_\\-*]+i[.\\s_\\-*]+n"), "pin")
        working = working.replace(Regex("k[.\\s_\\-*]+y[.\\s_\\-*]+c"), "kyc")
        working = working.replace(Regex("c[.\\s_\\-*]+v[.\\s_\\-*]+v"), "cvv")

        // 5. Normalize whitespace
        working = working.replace(Regex("\\s+"), " ").trim()

        return NormalizedText(rawText = raw, normalizedText = working)
    }
}
