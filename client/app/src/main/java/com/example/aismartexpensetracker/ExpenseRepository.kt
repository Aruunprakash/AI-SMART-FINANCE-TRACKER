package com.example.aismartexpensetracker

import android.util.Log
import com.example.aismartexpensetracker.network.ApiService
import com.example.aismartexpensetracker.network.RetrofitClient
import com.example.aismartexpensetracker.network.TransactionRequest

/**
 * Single place that knows how to save a new expense AND enrich it with the
 * server's categorize + anomaly models. Both the manual "+" entry
 * (ExpenseViewModel) and the automatic notification capture
 * (ExpenseNotificationListener) call this same function, so a transaction gets
 * identical treatment no matter how it entered the app.
 *
 * Enrichment order is deliberate:
 *   1. insert locally with an on-device keyword category  -> always works
 *   2. ask the server for a better category                -> overwrites if confident
 *   3. ask the server whether the amount is unusual        -> best effort
 *
 * Step 1 means the app is fully usable with the server down; steps 2-3 are
 * enhancements, not dependencies.
 */
object ExpenseRepository {

    private const val TAG = "ExpenseRepository"

    /**
     * Below this, we keep the on-device keyword result. A low-confidence model
     * guess is not worth overriding an exact brand match like "swiggy".
     */
    private const val MIN_SERVER_CONFIDENCE = 0.50

    private val api: ApiService = RetrofitClient.apiService

    suspend fun captureExpense(dao: ExpenseDao, merchant: String, amount: Double) {
        val localCategory = CategoryKeywords.categorize(merchant)

        val newId = dao.insertExpense(
            Expense(
                amount = amount.toString(),
                merchant = merchant,
                category = localCategory
            )
        ).toInt()
        Log.d(TAG, "Saved '$merchant' locally as $localCategory")

        try {
            val request = TransactionRequest(merchant_text = merchant, amount = amount)

            val categoryResult = api.categorize(request)
            if (categoryResult.confidence >= MIN_SERVER_CONFIDENCE) {
                dao.updateCategory(newId, categoryResult.category)
                Log.d(TAG, "Server refined -> ${categoryResult.category} (${categoryResult.confidence})")
            } else {
                Log.d(
                    TAG,
                    "Server confidence ${categoryResult.confidence} below threshold, keeping $localCategory"
                )
            }

            val anomalyResult = api.checkAnomaly(request)
            val isAnomaly = anomalyResult.status == "UNUSUAL"
            dao.updateAnomalyFlag(newId, isAnomaly)
            if (isAnomaly) Log.w(TAG, "Flagged as UNUSUAL: $merchant, $amount")
        } catch (e: Exception) {
            // Expected whenever the server isn't running. The expense is already
            // saved and categorized on-device, so there is nothing to recover.
            Log.i(TAG, "Server unreachable; keeping on-device category $localCategory (${e.message})")
        }
    }
}
