package com.example.aismartexpensetracker

import android.util.Log
import com.example.aismartexpensetracker.network.ApiService
import com.example.aismartexpensetracker.network.RetrofitClient
import com.example.aismartexpensetracker.network.TransactionRequest

/**
 * Single place that knows how to save a new expense AND run it through the
 * server's categorize + anomaly endpoints. Both the manual "+" entry
 * (ExpenseViewModel) and the automatic notification capture
 * (ExpenseNotificationListener) call this same function, so a transaction
 * gets the identical AI treatment no matter how it entered the app.
 */
object ExpenseRepository {

    private const val TAG = "ExpenseRepository"
    private val api: ApiService = RetrofitClient.apiService

    /**
     * Inserts the expense locally first (so nothing is ever lost if the
     * server is unreachable), then calls /categorize and /anomaly and
     * updates the row once results come back.
     */
    suspend fun captureExpense(dao: ExpenseDao, merchant: String, amount: Double) {
        val newId = dao.insertExpense(
            Expense(amount = amount.toString(), merchant = merchant)
        ).toInt()

        try {
            val categoryResult = api.categorize(
                TransactionRequest(merchant_text = merchant, amount = amount)
            )
            dao.updateCategory(newId, categoryResult.category)
            Log.d(TAG, "Categorized '$merchant' -> ${categoryResult.category} (${categoryResult.confidence})")

            val anomalyResult = api.checkAnomaly(
                TransactionRequest(merchant_text = merchant, amount = amount)
            )
            dao.updateAnomalyFlag(newId, anomalyResult.status == "UNUSUAL")
            if (anomalyResult.status == "UNUSUAL") {
                Log.w(TAG, "Flagged as UNUSUAL: $merchant, ₹$amount")
            }
        } catch (e: Exception) {
            // The expense is already saved locally -- categorization failing
            // (e.g. server not reachable) shouldn't lose the transaction.
            Log.e(TAG, "Server categorization failed, expense saved as Uncategorized", e)
        }
    }
}
