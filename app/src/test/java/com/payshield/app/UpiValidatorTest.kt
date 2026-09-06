package com.payshield.app

import com.payshield.app.domain.model.UpiPayload
import com.payshield.app.security.qr.UpiValidator
import org.junit.Assert.*
import org.junit.Test

class UpiValidatorTest {

    @Test
    fun validate_validStandardVpa_passesValidation() {
        val payload = UpiPayload("store.merchant@sbi", "Store", 500.0, "TR1", "INR", "raw")
        val result = UpiValidator.validate(payload)

        assertTrue(result.isStructuralUpi)
        assertTrue(result.isValidVpaFormat)
        assertTrue(result.isValidAmountFormat)
        assertTrue(result.isSupportedCurrency)
        assertTrue(result.validationNotes.isEmpty())
    }

    @Test
    fun validate_malformedVpa_flagsInvalidVpaFormat() {
        val payload = UpiPayload("invalid_vpa_without_at_symbol", "Name", 100.0, "TR2", "INR", "raw")
        val result = UpiValidator.validate(payload)

        assertFalse(result.isValidVpaFormat)
        assertTrue(result.validationNotes.any { it.contains("malformed") })
    }

    @Test
    fun validate_nonStandardCurrency_flagsCurrencyNote() {
        val payload = UpiPayload("user@ybl", "Name", 100.0, "TR3", "USD", "raw")
        val result = UpiValidator.validate(payload)

        assertFalse(result.isSupportedCurrency)
        assertTrue(result.validationNotes.any { it.contains("USD") })
    }
}
