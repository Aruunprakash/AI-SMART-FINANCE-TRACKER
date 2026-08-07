package com.example.aismartexpensetracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: String,
    val merchant: String,
    val date: Long = System.currentTimeMillis()
)