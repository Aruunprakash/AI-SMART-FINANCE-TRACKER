package com.example.aismartexpensetracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

val Purple = Color(0xFF5B2A8C)
val PurpleDark = Color(0xFF3E1D63)
val Ink = Color(0xFF1E1B24)
val Muted = Color(0xFF6B6577)
val LineColor = Color(0xFFE4E0EC)
val BgColor = Color(0xFFF3F1F7)
val Good = Color(0xFF2E9E6B)
val Warn = Color(0xFFD97706)
val Danger = Color(0xFFDC2626)

private val CATEGORY_EMOJI = mapOf(
    "Food" to "\uD83C\uDF54",
    "Groceries" to "\uD83D\uDED2",
    "Travel" to "\uD83D\uDE95",
    "Shopping" to "\uD83D\uDECD\uFE0F",
    "Bills" to "\uD83E\uDDFE",
    "Healthcare" to "\uD83C\uDFE5",
    "Entertainment" to "\uD83C\uDFAC",
    "Investment" to "\uD83D\uDCC8",
    "Rent" to "\uD83C\uDFE0",
    "Transfer" to "\uD83D\uDCB8",
    "Uncategorized" to "\u2753"
)

@Composable
fun DashboardScreen(viewModel: ExpenseViewModel = viewModel()) {
    val expenses by viewModel.expenses.collectAsState()
    val isCategorizing by viewModel.isCategorizing.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // ---- Real derived values, computed from actual Room data ----
    val totalSpent = expenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    val categoryTotals = expenses
        .groupBy { it.category }
        .mapValues { (_, list) -> list.sumOf { it.amount.toDoubleOrNull() ?: 0.0 } }
        .entries.sortedByDescending { it.value }
        .take(4)
    val recentTransactions = expenses.take(5)
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Purple, PurpleDark)))
                    .padding(20.dp, 18.dp, 20.dp, 22.dp)
            ) {
                Text(
                    "SMART EXPENSE TRACKER",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Hi \uD83D\uDC4B",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                DashboardCard {
                    Text("SPENT SO FAR", fontSize = 11.sp, color = Muted, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "\u20B9${"%,.0f".format(totalSpent)}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${expenses.size} transactions logged", fontSize = 12.sp, color = Muted)
                }

                Spacer(Modifier.height(12.dp))

                DashboardCard {
                    Text("SPEND BY CATEGORY", fontSize = 11.sp, color = Muted, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(10.dp))
                    if (categoryTotals.isEmpty()) {
                        Text("No expenses yet -- tap + to add one", fontSize = 12.sp, color = Muted)
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            categoryTotals.forEach { (category, amount) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(BgColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(CATEGORY_EMOJI[category] ?: "\u2753", fontSize = 16.sp)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(category, fontSize = 10.sp, color = Muted)
                                    Text(
                                        "\u20B9${"%,.0f".format(amount)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Ink
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                DashboardCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("RECENT TRANSACTIONS", fontSize = 11.sp, color = Muted, letterSpacing = 0.5.sp)
                        if (isCategorizing) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (recentTransactions.isEmpty()) {
                        Text("Nothing logged yet", fontSize = 12.sp, color = Muted)
                    } else {
                        recentTransactions.forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(tx.merchant, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "${CATEGORY_EMOJI[tx.category] ?: ""} ${tx.category}",
                                            fontSize = 10.sp,
                                            color = Muted
                                        )
                                        if (tx.isAnomaly) {
                                            Spacer(Modifier.width(6.dp))
                                            Text("UNUSUAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Danger)
                                        }
                                    }
                                    Text(dateFormat.format(tx.date), fontSize = 11.sp, color = Muted)
                                }
                                Text(
                                    "-\u20B9${tx.amount}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(80.dp)) // room for the FAB
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Purple,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add expense")
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { merchant, amount ->
                viewModel.addExpense(merchant, amount)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddExpenseDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var merchant by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val amountValue = amountText.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add expense") },
        text = {
            Column {
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant (e.g. Swiggy Order)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (\u20B9)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(merchant, amountValue ?: 0.0) },
                enabled = merchant.isNotBlank() && amountValue != null && amountValue > 0
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DashboardCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(16.dp),
        content = content
    )
}
