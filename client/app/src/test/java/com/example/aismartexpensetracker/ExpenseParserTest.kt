package com.example.aismartexpensetracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Regex extraction accuracy tests (implementation plan §7, Phase 8).
 *
 * Every string here is a real-world Indian bank / UPI notification shape.
 * ExpenseParser is a pure function with no Android dependencies, so these run
 * on the JVM via `./gradlew test` -- no device or emulator needed.
 */
class ExpenseParserTest {

    // ---------- amount extraction ----------

    @Test
    fun `parses rupee symbol amount`() {
        val result = ExpenseParser.parse("You paid ₹450.00 to Swiggy")
        assertNotNull(result)
        assertEquals(450.0, result!!.amount, 0.001)
    }

    @Test
    fun `parses Rs prefix with decimals`() {
        val result = ExpenseParser.parse("Rs.450.00 debited from a/c XX1234")
        assertEquals(450.0, result!!.amount, 0.001)
    }

    @Test
    fun `parses INR with thousands separator`() {
        val result = ExpenseParser.parse("INR 1,200 debited")
        assertEquals(1200.0, result!!.amount, 0.001)
    }

    @Test
    fun `parses amount with comma and paise`() {
        val result = ExpenseParser.parse("Rs 2,500.50 debited for an order")
        assertEquals(2500.50, result!!.amount, 0.001)
    }

    // ---------- transaction type ----------

    @Test
    fun `classifies debit`() {
        assertEquals("debit", ExpenseParser.parse("Rs.180 paid to BigBasket Order")!!.type)
    }

    @Test
    fun `classifies credit`() {
        val result = ExpenseParser.parse("Rs.45000 credited to your a/c XX1234 on 01-09-25")
        assertEquals("credit", result!!.type)
    }

    @Test
    fun `treats payment as a debit verb`() {
        // PhonePe's wording: "Payment of ..." rather than "paid".
        assertEquals("debit", ExpenseParser.parse("Payment of Rs.320 to Zomato successful")!!.type)
    }

    // ---------- merchant extraction ----------

    @Test
    fun `extracts merchant after to`() {
        assertEquals("Swiggy", ExpenseParser.parse("You paid ₹450.00 to Swiggy")!!.merchant)
    }

    @Test
    fun `extracts merchant after at`() {
        assertEquals("Starbucks", ExpenseParser.parse("₹99 spent at Starbucks")!!.merchant)
    }

    @Test
    fun `extracts merchant from UPI VPA rather than the literal word VPA`() {
        // The most common real shape, and the one a naive "to (\w+)" gets wrong.
        val result = ExpenseParser.parse(
            "Rs.450.00 debited from a/c XX1234 to VPA swiggy@ybl on 04-09-25"
        )
        assertEquals("Swiggy", result!!.merchant)
    }

    @Test
    fun `strips payment handle noise from a VPA`() {
        // irctc.upi@sbi is IRCTC, not "Irctc Upi".
        val result = ExpenseParser.parse(
            "Dear Customer, Rs.75 debited from A/c XX9012 to VPA irctc.upi@sbi Ref 4432"
        )
        assertEquals("Irctc", result!!.merchant)
    }

    @Test
    fun `stops merchant before a trailing status word`() {
        assertEquals("Zomato", ExpenseParser.parse("Payment of Rs.320 to Zomato successful")!!.merchant)
    }

    @Test
    fun `stops merchant before a date`() {
        val result = ExpenseParser.parse("₹1250 transferred to John Doe on 12-08-2025")
        assertEquals("John Doe", result!!.merchant)
    }

    @Test
    fun `does not treat your a slash c as a merchant`() {
        val result = ExpenseParser.parse("Rs.45000 credited to your a/c XX1234 on 01-09-25")
        assertEquals("Unknown Merchant", result!!.merchant)
    }

    @Test
    fun `falls back to unknown merchant when none is present`() {
        assertEquals("Unknown Merchant", ExpenseParser.parse("INR 1,200 debited")!!.merchant)
    }

    // ---------- rejection: the false-positive guard ----------

    @Test
    fun `rejects a chat message that merely mentions an amount`() {
        // The reason the parser requires a transaction verb: messaging apps are
        // on the notification allowlist because bank SMS arrive through them.
        assertNull(ExpenseParser.parse("Hey can you send me ₹500 for dinner"))
    }

    @Test
    fun `rejects text with no amount`() {
        assertNull(ExpenseParser.parse("Your OTP is 123456. Do not share it."))
    }

    @Test
    fun `rejects an amount with no transaction verb`() {
        assertNull(ExpenseParser.parse("Offer: get ₹200 off on your next order"))
    }

    @Test
    fun `rejects empty text`() {
        assertNull(ExpenseParser.parse(""))
    }
}
