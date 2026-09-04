package com.example.aismartexpensetracker.ui

/**
 * Emoji shown next to a category. Shared by the dashboard, transactions list,
 * and analytics so a category looks the same everywhere.
 */
val CATEGORY_EMOJI: Map<String, String> = mapOf(
    "Food" to "🍔",
    "Groceries" to "🛒",
    "Travel" to "🚕",
    "Shopping" to "🛍️",
    "Bills" to "🧾",
    "Healthcare" to "🏥",
    "Entertainment" to "🎬",
    "Investment" to "📈",
    "Rent" to "🏠",
    "Transfer" to "💸",
    "Uncategorized" to "❓"
)

fun emojiFor(category: String): String = CATEGORY_EMOJI[category] ?: "❓"
