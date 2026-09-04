package com.example.aismartexpensetracker

/**
 * On-device keyword categorizer -- the offline fallback for the ML server.
 *
 * The server's TF-IDF model is better (it generalises to merchants not listed
 * here), but it needs the laptop to be reachable. This runs instantly on the
 * phone with no network, so a transaction always lands with a real category
 * instead of "Uncategorized". When the server IS reachable, its answer
 * overwrites this one -- see ExpenseRepository.captureExpense.
 *
 * Brand lists mirror CATEGORY_BRANDS in
 * model/training/train_categorization_model_final.py. Keep them roughly in
 * sync when categories change.
 */
object CategoryKeywords {

    const val UNCATEGORIZED = "Uncategorized"

    private val KEYWORDS: Map<String, List<String>> = mapOf(
        "Food" to listOf(
            "swiggy", "zomato", "mcdonald", "domino", "starbucks", "kfc",
            "burger king", "pizza", "cafe", "coffee", "subway", "biryani",
            "faasos", "barbeque", "chaayos", "momo", "haldiram", "restaurant",
            "eat", "food", "dhaba", "bakery"
        ),
        "Groceries" to listOf(
            "bigbasket", "dmart", "d-mart", "reliance fresh", "more supermarket",
            "nature's basket", "metro cash", "blinkit", "zepto", "star bazaar",
            "spencer", "jiomart", "grofers", "grocery", "supermarket", "kirana",
            "instamart"
        ),
        "Travel" to listOf(
            "uber", "ola", "irctc", "railway", "indigo", "rapido", "vistara",
            "spicejet", "redbus", "makemytrip", "yatra", "goair", "air india",
            "petrol", "fuel", "hpcl", "iocl", "bpcl", "metro rail", "toll",
            "fastag", "cab", "airlines"
        ),
        "Shopping" to listOf(
            "amazon", "flipkart", "myntra", "ajio", "decathlon", "croma",
            "nykaa", "tata cliq", "reliance digital", "lifestyle", "h&m",
            "snapdeal", "meesho", "shoppers stop", "westside", "store"
        ),
        "Bills" to listOf(
            "electricity", "airtel", "jio", "recharge", "act broadband",
            "water board", "tata sky", "vodafone", "bsnl", "gas", "broadband",
            "urban company", "hathway", "mtnl", "postpaid", "prepaid", "bill",
            "dth", "utility"
        ),
        "Healthcare" to listOf(
            "apollo", "practo", "medplus", "cult", "1mg", "netmeds", "fortis",
            "max healthcare", "pharmeasy", "pharmacy", "hospital", "clinic",
            "diagnostic", "lab", "medical", "doctor"
        ),
        "Entertainment" to listOf(
            "pvr", "bookmyshow", "netflix", "spotify", "inox", "hotstar",
            "prime video", "cinepolis", "sonyliv", "zee5", "youtube",
            "cinema", "movie", "gaming", "steam"
        ),
        "Investment" to listOf(
            "mutual fund", "sip", "zerodha", "lic", "groww", "icici direct",
            "upstox", "paytm money", "angel one", "ppf", "premium", "insurance",
            "nps", "trading", "smallcase"
        ),
        "Rent" to listOf(
            "rent", "landlord", "maintenance", "society", "pg ", "apartment",
            "flat rent", "hostel"
        ),
        "Transfer" to listOf(
            "phonepe", "gpay", "google pay", "paytm wallet", "upi transfer",
            "neft", "imps", "rtgs", "wallet load", "transfer", "sent to"
        )
    )

    /**
     * Keywords are matched on word boundaries, not as bare substrings.
     *
     * Substring matching looks fine until real merchant names arrive:
     * "chocolate" contains "ola" (Travel), "Vegas" contains "gas" (Bills), and
     * "theatre" contains "eat" (Food). \b removes that entire class of
     * false positive.
     *
     * Still ordered longest-first, so a specific keyword beats a generic one
     * that also matches.
     */
    private val ORDERED: List<Pair<Regex, String>> = KEYWORDS
        .flatMap { (category, words) -> words.map { it.trim() to category } }
        .sortedByDescending { it.first.length }
        .map { (keyword, category) ->
            Regex("""\b${Regex.escape(keyword)}\b""", RegexOption.IGNORE_CASE) to category
        }

    fun categorize(merchantText: String): String =
        ORDERED.firstOrNull { (pattern, _) -> pattern.containsMatchIn(merchantText) }
            ?.second
            ?: UNCATEGORIZED

    /** Category names the correction UI offers, plus the fallback. */
    val ALL_CATEGORIES: List<String> = KEYWORDS.keys.toList() + UNCATEGORIZED
}
