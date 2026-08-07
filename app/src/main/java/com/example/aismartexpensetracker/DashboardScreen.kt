package com.example.aismartexpensetracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Purple = Color(0xFF5B2A8C)
val PurpleDark = Color(0xFF3E1D63)
val Ink = Color(0xFF1E1B24)
val Muted = Color(0xFF6B6577)
val LineColor = Color(0xFFE4E0EC)
val BgColor = Color(0xFFF3F1F7)
val Good = Color(0xFF2E9E6B)
val Warn = Color(0xFFD97706)

data class CategorySpend(val emoji: String, val name: String, val amount: String)
data class Transaction(val name: String, val time: String, val amount: String)

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(Purple, PurpleDark))
                )
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
                "Hi, Vyshnavi \uD83D\uDC4B",
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
                Text("SPENT THIS MONTH", fontSize = 11.sp, color = Muted, letterSpacing = 0.5.sp)
                Spacer(Modifier.height(6.dp))
                Text("₹18,240", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.height(4.dp))
                Text("↓ 12% vs last month", fontSize = 12.sp, color = Good)
            }

            Spacer(Modifier.height(12.dp))

            DashboardCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Monthly budget used", fontSize = 12.sp, color = Muted)
                    Text("68%", fontSize = 12.sp, color = Muted)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress =  0.68f ,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = Purple,
                    trackColor = LineColor
                )
            }

            Spacer(Modifier.height(12.dp))

            DashboardCard {
                Text("TOP CATEGORIES", fontSize = 11.sp, color = Muted, letterSpacing = 0.5.sp)
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(
                        CategorySpend("\uD83C\uDF54", "Food", "₹5,200"),
                        CategorySpend("\uD83D\uDE95", "Travel", "₹3,100"),
                        CategorySpend("\uD83E\uDDFE", "Bills", "₹6,400"),
                        CategorySpend("\uD83D\uDECD\uFE0F", "Shopping", "₹3,540")
                    ).forEach { cat ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cat.emoji, fontSize = 16.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(cat.name, fontSize = 10.sp, color = Muted)
                            Text(cat.amount, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            DashboardCard {
                Text("RECENT TRANSACTIONS", fontSize = 11.sp, color = Muted, letterSpacing = 0.5.sp)
                Spacer(Modifier.height(8.dp))
                listOf(
                    Transaction("Swiggy", "Today, 1:20 PM", "-₹420"),
                    Transaction("Uber", "Today, 9:05 AM", "-₹185"),
                    Transaction("Electricity Bill", "Yesterday", "-₹1,240")
                ).forEach { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(tx.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                            Text(tx.time, fontSize = 11.sp, color = Muted)
                        }
                        Text(tx.amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                    }
                }
                Text(
                    "View all →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Purple,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFF4E5))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚠️ You're close to your Food budget limit this month.", fontSize = 12.sp, color = Warn)
            }
        }
    }
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