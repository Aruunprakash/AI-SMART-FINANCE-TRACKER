package com.example.aismartexpensetracker.network

// These mirror server/app/schemas.py exactly -- keep both in sync if the
// server's request/response shape changes.

data class TransactionRequest(
    val merchant_text: String,
    val amount: Double
)

data class CategoryResponse(
    val category: String,
    val confidence: Double
)

data class AnomalyResponse(
    val amount: Double,
    val status: String // "normal" | "UNUSUAL"
)

data class PredictionRequest(
    val month: Int,
    val category: String
)

data class PredictionResponse(
    val category: String,
    val predicted_amount: Double
)
