package com.example.aismartexpensetracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("UPDATE expenses SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: Int, category: String)

    @Query("UPDATE expenses SET isAnomaly = :isAnomaly WHERE id = :id")
    suspend fun updateAnomalyFlag(id: Int, isAnomaly: Boolean)
}