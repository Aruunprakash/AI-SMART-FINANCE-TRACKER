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

object ExpenseParser {

    // Matches amounts like: Rs.450, Rs 450.00, INR 1,200, ₹99
    private val amountRegex = Regex(
        """(?:Rs\.?|INR|₹)\s?([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // Matches "debited" or "credited" to figure out transaction type
    private val debitRegex = Regex("""debit(ed)?""", RegexOption.IGNORE_CASE)
    private val creditRegex = Regex("""credit(ed)?""", RegexOption.IGNORE_CASE)

    // Tries to capture merchant name after "to"/"at", stopping cleanly before
    // trailing words like "on", "via", "UPI", "A/c", or a date/number
    private val merchantRegex = Regex(
        """(?:to|at)\s+([A-Za-z&.\s]{2,30}?)(?=\s+(?:on|via|UPI|A/c|Ref|dated|\d)|$)""",
        RegexOption.IGNORE_CASE
    )

    fun parse(text: String): ParsedExpense? {
        val amountMatch = amountRegex.find(text) ?: return null
        val amountStr = amountMatch.groupValues[1].replace(",", "")
        val amount = amountStr.toDoubleOrNull() ?: return null

        val type = when {
            debitRegex.containsMatchIn(text) -> "debit"
            creditRegex.containsMatchIn(text) -> "credit"
            else -> "unknown"
        }

        val merchantMatch = merchantRegex.find(text)
        val merchant = merchantMatch?.groupValues?.get(1)?.trim() ?: "Unknown Merchant"

        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date())

        return ParsedExpense(
            amount = amount,
            merchant = merchant,
            type = type,
            rawText = text,
            date = currentDate
        )
    }
}
