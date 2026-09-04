package com.example.aismartexpensetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).expenseDao()

    // Real data straight from Room -- nothing hardcoded. Also picks up anything
    // the notification listener captures automatically, since both write to the
    // same database.
    val expenses: StateFlow<List<Expense>> = dao.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isCategorizing = MutableStateFlow(false)
    val isCategorizing: StateFlow<Boolean> = _isCategorizing

    /**
     * Manual entry path (the + button). Uses the same pipeline as automatic
     * notification capture -- see ExpenseRepository.
     */
    fun addExpense(merchant: String, amount: Double) {
        viewModelScope.launch {
            _isCategorizing.value = true
            ExpenseRepository.captureExpense(dao, merchant, amount)
            _isCategorizing.value = false
        }
    }

    /**
     * Human-in-the-Loop correction. When the model gets a category wrong the
     * user overrides it here, and because the dashboard reads the same Room
     * Flow the correction is reflected everywhere immediately.
     */
    fun correctCategory(expenseId: Int, newCategory: String) {
        viewModelScope.launch {
            dao.updateCategory(expenseId, newCategory)
        }
    }
}
