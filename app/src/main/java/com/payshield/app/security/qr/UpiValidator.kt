package com.payshield.app.security.qr

import com.payshield.app.domain.model.UpiPayload
import java.util.regex.Pattern

object UpiValidator {

    // Standard VPA pattern: username@handle (letters, numbers, dots, hyphens, underscores)
    private val VPA_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$")

    data class ValidationResult(
        val isStructuralUpi: Boolean,
        val isValidVpaFormat: Boolean,
        val isValidAmountFormat: Boolean,
        val isSupportedCurrency: Boolean,
        val validationNotes: List<String>
    )

    fun validate(payload: UpiPayload): ValidationResult {
        val notes = mutableListOf<String>()

        val isUpiScheme = payload.raw.startsWith("upi://pay", ignoreCase = true) || payload.pa != null
        if (!isUpiScheme) {
            notes.add("Payload does not use standard upi://pay URI scheme")
        }

        val isValidVpa = payload.pa != null && VPA_PATTERN.matcher(payload.pa).matches()
        if (payload.pa != null && !isValidVpa) {
            notes.add("Payment address (pa) format is non-standard or malformed")
        } else if (payload.pa == null) {
            notes.add("Missing required payee Virtual Payment Address (pa)")
        }

        val isValidAmount = payload.am == null || payload.am >= 0.0
        if (payload.am != null && payload.am < 0.0) {
            notes.add("Invalid negative payment amount")
        }

        val currency = payload.currency ?: "INR"
        val isSupportedCurr = currency.equals("INR", ignoreCase = true)
        if (!isSupportedCurr) {
            notes.add("Non-standard UPI currency specified: $currency")
        }

        return ValidationResult(
            isStructuralUpi = isUpiScheme,
            isValidVpaFormat = isValidVpa,
            isValidAmountFormat = isValidAmount,
            isSupportedCurrency = isSupportedCurr,
            validationNotes = notes
        )
    }
}
