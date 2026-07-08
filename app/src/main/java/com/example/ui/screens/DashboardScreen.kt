package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.components.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onNavigateToQuickEntry: () -> Unit,
    onNavigateToManager: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    // Calculations
    val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
    val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
    val currentBalance = totalIncome - totalExpense
    val totalSavings = if (currentBalance > 0) currentBalance else 0.0

    // Budget Calculations
    val activeMonthBudget = budgets.find { it.categoryName == "Total" }?.amount ?: 15000.0
    val activeMonthExpense = transactions.filter { !it.isIncome && isCurrentMonth(it.date) }.sumOf { it.amount }
    val remainingBudget = activeMonthBudget - activeMonthExpense
    val budgetProgress = if (activeMonthBudget > 0.0) (activeMonthExpense / activeMonthBudget).toFloat() else 0f

    // Category Distribution for Chart
    val categoryExpenses = transactions
        .filter { !it.isIncome }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val defaultColors = listOf(
        Color(0xFFFF5722), Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF9C27B0),
        Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFF3F51B5), Color(0xFF00BCD4),
        Color(0xFF009688), Color(0xFF795548), Color(0xFF607D8B)
    )

    val chartColors = categoryExpenses.keys.mapIndexed { index, cat ->
        cat to (defaultColors.getOrNull(index % defaultColors.size) ?: Color.Gray)
    }.toMap()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Assalamu Alaikum,",
                        color = GlassTheme.glassSubTextColor(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Premium Fintech App",
                        color = GlassTheme.glassTextColor(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quick entry Floating Action Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onNavigateToQuickEntry),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onNavigateToQuickEntry) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Quick Entry",
                            tint = Color(0xFFFFC107)
                        )
                    }
                }
            }
        }

        // Main Balance Card (Glassmorphic)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CURRENT BALANCE",
                    color = GlassTheme.glassSubTextColor(),
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedCounter(
                    value = currentBalance,
                    prefix = "$currencySymbol ",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Income",
                                color = GlassTheme.glassSubTextColor(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            "$currencySymbol ${formatBDT(totalIncome)}",
                            color = GlassTheme.glassTextColor(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Expenses",
                                color = GlassTheme.glassSubTextColor(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            "$currencySymbol ${formatBDT(totalExpense)}",
                            color = GlassTheme.glassTextColor(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Monthly Budget Progress Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MONTHLY BUDGET",
                            color = GlassTheme.glassSubTextColor(),
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol ${formatBDT(activeMonthBudget)}",
                            color = GlassTheme.glassTextColor(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "REMAINING",
                            color = GlassTheme.glassSubTextColor(),
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol ${formatBDT(remainingBudget)}",
                            color = if (remainingBudget < 0) Color(0xFFEC4899) else Color(0xFF10B981),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Beautiful linear budget progress indicator
                LinearProgressIndicator(
                    progress = { budgetProgress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (budgetProgress > 0.9f) Color(0xFFEC4899) else Color(0xFF818CF8),
                    trackColor = Color(0x22FFFFFF)
                )

                if (remainingBudget < 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⚠️ Budget Limit Exceeded! Reduce spending.",
                        color = Color(0xFFF43F5E),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Quick Entry Promo / Navigation Tile
        item {
            GlassCardClickable(
                onClick = onNavigateToQuickEntry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x22FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Quick Expense Entry",
                            color = GlassTheme.glassTextColor(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Type multiple items like a grocery shopping list!",
                            color = GlassTheme.glassSubTextColor(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = GlassTheme.glassTextColor(),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Category Expense Distribution Chart
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "SPENDING BY CATEGORY",
                        color = GlassTheme.glassSubTextColor(),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "View Reports",
                        color = Color(0xFF818CF8),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.clickable(onClick = onNavigateToAnalytics)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    GlassPieChart(
                        data = categoryExpenses,
                        colors = chartColors,
                        modifier = Modifier.size(160.dp),
                        centerLabel = "This Month"
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        categoryExpenses.keys.take(4).forEach { cat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(chartColors[cat] ?: Color.Gray)
                                )
                                Text(
                                    text = cat,
                                    color = GlassTheme.glassTextColor(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT TRANSACTIONS",
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "See All",
                    color = Color(0xFF818CF8),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable(onClick = onNavigateToManager)
                )
            }
        }

        // List of recent transactions
        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = GlassTheme.glassSubTextColor(),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No Transactions Yet",
                            color = GlassTheme.glassSubTextColor(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        } else {
            items(transactions.take(5)) { tx ->
                TransactionRow(tx = tx, currencySymbol = currencySymbol)
            }
        }
    }
}

@Composable
fun TransactionRow(tx: Transaction, currencySymbol: String) {
    val categoryIcon = getCategoryIcon(tx.category)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x11FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    categoryIcon,
                    contentDescription = tx.category,
                    tint = if (tx.isIncome) Color(0xFF4CAF50) else Color(0xFFEC4899),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.name,
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${tx.category} • ${tx.paymentMethod}",
                    color = GlassTheme.glassSubTextColor(),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (tx.isIncome) "+ $currencySymbol ${formatBDT(tx.amount)}" else "- $currencySymbol ${formatBDT(tx.amount)}",
                    color = if (tx.isIncome) Color(0xFF4CAF50) else Color(0xFFEC4899),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(tx.date)),
                    color = GlassTheme.glassSubTextColor(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingBag
        "bills" -> Icons.Default.ReceiptLong
        "salary" -> Icons.Default.Payments
        "entertainment" -> Icons.Default.SportsEsports
        "education" -> Icons.Default.School
        "health" -> Icons.Default.MedicalServices
        "family" -> Icons.Default.Home
        "travel" -> Icons.Default.Flight
        "investment" -> Icons.Default.TrendingUp
        "gifts" -> Icons.Default.CardGiftcard
        else -> Icons.Default.Category
    }
}

private fun isCurrentMonth(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}
