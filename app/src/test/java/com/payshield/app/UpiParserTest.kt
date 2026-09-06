package com.payshield.app

import com.payshield.app.domain.model.SecurityConstants
import com.payshield.app.security.qr.UpiParser
import org.junit.Assert.*
import org.junit.Test

class UpiParserTest {

    @Test
    fun parse_validUpiPayload_extractsAllStandardFields() {
        val raw = "upi://pay?pa=merchant@sbi&pn=OfficialStore&am=250.50&tr=TXN123456&cu=INR"
        val payload = UpiParser.parse(raw)

        assertEquals("merchant@sbi", payload.pa)
        assertEquals("OfficialStore", payload.pn)
        assertEquals(250.50, payload.am!!, 0.001)
        assertEquals("TXN123456", payload.tr)
        assertEquals("INR", payload.currency)
        assertTrue(payload.unknownParams.isEmpty())
    }

    @Test
    fun parse_missingPa_handlesGracefullyWithoutCrashing() {
        val raw = "upi://pay?pn=UnknownPayee&am=100.0"
        val payload = UpiParser.parse(raw)

        assertNull(payload.pa)
        assertEquals("UnknownPayee", payload.pn)
        assertEquals(100.0, payload.am!!, 0.001)
    }

    @Test
    fun parse_malformedAmountAndCurrency_returnsNullAmountSafely() {
        val raw = "upi://pay?pa=test@okaxis&am=invalid_amount_string&cu=INVALID"
        val payload = UpiParser.parse(raw)

        assertEquals("test@okaxis", payload.pa)
        assertNull(payload.am)
        assertEquals("INVALID", payload.currency)
    }

    @Test
    fun parse_unknownExtraParameters_collectsInUnknownMap() {
        val raw = "upi://pay?pa=user@ybl&pn=User&custom_ref=999&session_token=abc"
        val payload = UpiParser.parse(raw)

        assertEquals("user@ybl", payload.pa)
        assertEquals(2, payload.unknownParams.size)
        assertEquals("999", payload.unknownParams["custom_ref"])
        assertEquals("abc", payload.unknownParams["session_token"])
    }

    @Test
    fun parse_oversizedPayload_truncatesDefensivelyWithoutOom() {
        val hugeString = "upi://pay?pa=test@bank&junk=" + "A".repeat(5000)
        val payload = UpiParser.parse(hugeString)

        assertTrue(payload.raw.length <= SecurityConstants.MAX_QR_PAYLOAD_LENGTH)
        assertEquals("test@bank", payload.pa)
    }

    @Test
    fun parse_nullOrEmptyInput_returnsEmptyPayloadWithoutCrashing() {
        val payloadNull = UpiParser.parse(null)
        assertNull(payloadNull.pa)
        assertEquals("", payloadNull.raw)

        val payloadBlank = UpiParser.parse("   ")
        assertNull(payloadBlank.pa)
        assertTrue(payloadBlank.raw.trim().isEmpty())
    }
}
