package com.example.aismartexpensetracker

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ParsedExpense(
    val amount: Double,
    val merchant: String,
    val type: String,   // "debit" or "credit"
    val rawText: String,
    val date: String
)

/**
 * Turns a raw bank / UPI notification string into structured transaction data.
 *
 * Two rules keep the false-positive rate down, which matters because the
 * notification listener sees real messages on a real phone:
 *
 *  1. An amount alone is NOT enough. A chat message saying "can you send me
 *     Rs.500" contains a perfectly valid amount but is not a transaction, so
 *     parse() also requires a transaction verb (debited / credited / paid /
 *     sent / spent / received / withdrawn).
 *  2. Merchant extraction tries the UPI VPA form first, because
 *     "to VPA swiggy@ybl" is the single most common real-world shape and a
 *     naive "to (\w+)" pattern captures the literal word "VPA" from it.
 */
object ExpenseParser {

    // Rs.450 | Rs 450.00 | INR 1,200 | ₹99 | Amount: INR 37090.45
    private val amountRegex = Regex(
        """(?:Rs\.?|INR|₹)\s?([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    private val debitRegex = Regex(
        """\b(?:debited|debit|paid|payment|sent|spent|withdrawn|withdrawal|""" +
            """purchase|purchased|transferred)\b""",
        RegexOption.IGNORE_CASE
    )
    private val creditRegex = Regex(
        """\b(?:credited|credit|received|deposited|refund(?:ed)?)\b""",
        RegexOption.IGNORE_CASE
    )

    // "to swiggy@ybl", "to VPA merchant@okaxis" -> captures the handle before @
    private val vpaRegex = Regex(
        """\b(?:to|from)\s+(?:VPA\s+)?([A-Za-z0-9._\-]{2,})@[A-Za-z]{2,}""",
        RegexOption.IGNORE_CASE
    )

    // "paid to Swiggy", "at BigBasket", "transferred to John Doe on ..."
    // Stops before trailing noise: on / via / UPI / A/c / Ref / dated / digits.
    private val merchantRegex = Regex(
        """\b(?:paid to|sent to|transferred to|debited to|credited to|to|at)\s+""" +
            """([A-Za-z][A-Za-z&.'\s\-]{1,40}?)""" +
            """(?=\s*(?:\bon\b|\bvia\b|\bUPI\b|\bA/c\b|\bAc\b|\bRef\b|\bdated\b|\bfor\b|""" +
            """\bsuccessful\b|\bsuccess\b|\bcompleted\b|\bfailed\b|\bpending\b|""" +
            """[.,;:!|]|\d|${'$'}))""",
        RegexOption.IGNORE_CASE
    )

    // Words that are never a merchant, even when they follow "to"/"at".
    private val notMerchant = setOf(
        "vpa", "a/c", "ac", "account", "upi", "your", "you", "the", "ref", "bank"
    )

    /**
     * Payment-handle noise inside a VPA. "irctc.upi@sbi" is IRCTC, not
     * "Irctc Upi", so these are dropped from the end of the handle.
     */
    private val vpaNoiseSuffixes = setOf("upi", "ok", "ybl", "axl", "ibl", "paytm", "apl")

    fun parse(text: String): ParsedExpense? {
        val amountMatch = amountRegex.find(text) ?: return null
        val amount = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
        if (amount <= 0.0) return null

        // Rule 1: require a transaction verb, not just an amount.
        val type = when {
            debitRegex.containsMatchIn(text) -> "debit"
            creditRegex.containsMatchIn(text) -> "credit"
            else -> return null
        }

        return ParsedExpense(
            amount = amount,
            merchant = extractMerchant(text),
            type = type,
            rawText = text,
            date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )
    }

    /** VPA form first, then the "to <Name>" form, then give up. */
    private fun extractMerchant(text: String): String {
        vpaRegex.find(text)?.groupValues?.get(1)?.let { handle ->
            if (handle.lowercase() !in notMerchant) {
                val cleaned = handle
                    .split('.', '_', '-')
                    .filter { it.isNotBlank() }
                    .dropLastWhile { it.lowercase() in vpaNoiseSuffixes }
                if (cleaned.isNotEmpty()) return prettify(cleaned.joinToString(" "))
            }
        }

        merchantRegex.findAll(text).forEach { match ->
            val candidate = match.groupValues[1].trim().trimEnd('.', ',', '-')
            if (candidate.isNotBlank() && candidate.lowercase() !in notMerchant) {
                return prettify(candidate)
            }
        }

        return "Unknown Merchant"
    }

    /** "swiggy" -> "Swiggy", "bigbasket.retail" -> "Bigbasket Retail". */
    private fun prettify(raw: String): String =
        raw.replace('.', ' ')
            .replace('_', ' ')
            .replace('-', ' ')
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.titlecase(Locale.getDefault()) }
            }
            .take(40)
}
