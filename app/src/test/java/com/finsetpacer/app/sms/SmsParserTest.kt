package com.finsetpacer.app.sms

import com.finsetpacer.app.data.db.DEFAULT_CATEGORY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class SmsParserTest {

    private val fixedTime = LocalDateTime.of(2026, 4, 15, 12, 30, 0)

    @Test
    fun `parses HDFC debit sms`() {
        val body = "Rs.1,250.00 debited from a/c XX1234 on 15-04-26 to VPA swiggy@ybl Avl Bal Rs.5,000"
        val result = SmsParser.parse("HDFCBK", body, fixedTime)

        assertNotNull(result)
        assertEquals(1250.0, result!!.amount, 0.001)
        assertEquals("Food", result.category) // merchant contains "swiggy"
    }

    @Test
    fun `parses GPay style upi paid sms`() {
        val body = "You paid Rs.499 to Amazon Pay on GPay. UPI Ref No 123456789012."
        val result = SmsParser.parse("GPay", body, fixedTime)

        assertNotNull(result)
        assertEquals(499.0, result!!.amount, 0.001)
        assertEquals("Shopping", result.category) // merchant contains "Amazon"
    }

    @Test
    fun `parses PhonePe sent to sms`() {
        val body = "INR 850 sent to Uber India via PhonePe UPI."
        val result = SmsParser.parse("PhonePe", body, fixedTime)

        assertNotNull(result)
        assertEquals(850.0, result!!.amount, 0.001)
        assertEquals("Travel", result.category)
    }

    @Test
    fun `unmatched merchant falls back to default category`() {
        val body = "Rs.300 spent at Local Tea Stall on your Axis Bank card."
        val result = SmsParser.parse("AXISBK", body, fixedTime)

        assertNotNull(result)
        assertEquals(DEFAULT_CATEGORY, result!!.category)
    }

    @Test
    fun `non-debit sms is ignored`() {
        val body = "Your OTP for login is 482913. Do not share it with anyone."
        val result = SmsParser.parse("VM-HDFCBK", body, fixedTime)

        assertNull(result)
    }

    @Test
    fun `credit sms without debit keywords is ignored`() {
        val body = "Rs.5000 credited to your account XX1234 on 15-04-26."
        val result = SmsParser.parse("SBIINB", body, fixedTime)

        assertNull(result)
    }

    @Test
    fun `dedup hash is stable for identical inputs and differs otherwise`() {
        val body = "Rs.1,250.00 debited from a/c to VPA swiggy@ybl on 15-04-26"
        val first = SmsParser.parse("HDFCBK", body, fixedTime)
        val second = SmsParser.parse("HDFCBK", body, fixedTime)
        val differentAmount = SmsParser.parse(
            "HDFCBK",
            "Rs.999.00 debited from a/c to VPA swiggy@ybl on 15-04-26",
            fixedTime
        )

        assertNotNull(first)
        assertNotNull(second)
        assertEquals(first!!.smsHash, second!!.smsHash)
        assertNotEquals(first.smsHash, differentAmount!!.smsHash)
    }

    @Test
    fun `known sender keywords are recognized`() {
        assertTrue(SmsParser.isKnownSender("HDFCBK"))
        assertTrue(SmsParser.isKnownSender("AX-PHONEPE"))
        assertTrue(SmsParser.isKnownSender("PAYTM"))
    }
}
