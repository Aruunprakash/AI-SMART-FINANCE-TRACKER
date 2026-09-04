package com.example.aismartexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aismartexpensetracker.ui.AnalyticsScreen
import com.example.aismartexpensetracker.ui.BudgetScreen
import com.example.aismartexpensetracker.ui.DashboardScreen
import com.example.aismartexpensetracker.ui.LoginScreen
import com.example.aismartexpensetracker.ui.MenuScreen
import com.example.aismartexpensetracker.ui.PredictionsScreen
import com.example.aismartexpensetracker.ui.ProfileScreen
import com.example.aismartexpensetracker.ui.RecommendationsScreen
import com.example.aismartexpensetracker.ui.TransactionsScreen
import com.example.aismartexpensetracker.ui.theme.AISMARTEXPENSETRACKERTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AISMARTEXPENSETRACKERTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost()
                }
            }
        }
    }
}

/**
 * Route names match the strings MenuScreen already navigates to.
 *
 * One ExpenseViewModel is hoisted here and shared by the screens that read
 * transactions, so the dashboard and the transactions list are guaranteed to
 * show the same data (they observe the same Room Flow either way, but sharing
 * the instance avoids duplicate collectors).
 */
@Composable
private fun AppNavHost() {
    val navController = rememberNavController()
    val expenseViewModel: ExpenseViewModel = viewModel()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(navController = navController, viewModel = expenseViewModel)
        }
        composable("menu") { MenuScreen(navController) }
        composable("transactions") { TransactionsScreen(viewModel = expenseViewModel) }
        composable("budgets") { BudgetScreen() }
        composable("analytics") { AnalyticsScreen() }
        composable("predictions") { PredictionsScreen() }
        composable("recommendations") { RecommendationsScreen() }
        composable("profile") {
            ProfileScreen(onLogout = { navController.navigate("login") })
        }
        composable("login") {
            LoginScreen(onLoginSuccess = { navController.navigate("dashboard") })
        }
    }
}
