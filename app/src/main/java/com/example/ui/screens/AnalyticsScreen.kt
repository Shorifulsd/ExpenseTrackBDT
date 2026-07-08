package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var selectedInterval by remember { mutableStateOf("Monthly") } // "Daily", "Weekly", "Monthly", "Yearly"

    // 1. Calculations based on interval
    val expenses = transactions.filter { !it.isIncome }
    val incomes = transactions.filter { it.isIncome }

    // Slicing data for Trend Line Graph
    val lastSevenDays = remember(transactions) {
        // Mocking 7 entries for daily trends or mapping last 7 transaction values
        expenses.take(7).reversed().map { it.amount }
    }
    val lineLabels = remember(transactions) {
        expenses.take(7).reversed().map { 
            val sdf = java.text.SimpleDateFormat("dd MMM", Locale.getDefault())
            sdf.format(Date(it.date))
        }
    }

    // Weekly statistics for bar chart
    val barData = remember(transactions) {
        listOf(
            Pair("Sat", expenses.filter { getDayOfWeek(it.date) == Calendar.SATURDAY }.sumOf { it.amount }),
            Pair("Sun", expenses.filter { getDayOfWeek(it.date) == Calendar.SUNDAY }.sumOf { it.amount }),
            Pair("Mon", expenses.filter { getDayOfWeek(it.date) == Calendar.MONDAY }.sumOf { it.amount }),
            Pair("Tue", expenses.filter { getDayOfWeek(it.date) == Calendar.TUESDAY }.sumOf { it.amount }),
            Pair("Wed", expenses.filter { getDayOfWeek(it.date) == Calendar.WEDNESDAY }.sumOf { it.amount }),
            Pair("Thu", expenses.filter { getDayOfWeek(it.date) == Calendar.THURSDAY }.sumOf { it.amount }),
            Pair("Fri", expenses.filter { getDayOfWeek(it.date) == Calendar.FRIDAY }.sumOf { it.amount })
        )
    }

    // Pie Chart Data: category expenses
    val categoryExpenses = remember(transactions) {
        expenses
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
    }

    val defaultColors = listOf(
        Color(0xFFFF5722), Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF9C27B0),
        Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFF3F51B5), Color(0xFF00BCD4),
        Color(0xFF009688), Color(0xFF795548), Color(0xFF607D8B)
    )

    val chartColors = categoryExpenses.keys.mapIndexed { index, cat ->
        cat to (defaultColors.getOrNull(index % defaultColors.size) ?: Color.Gray)
    }.toMap()

    // High Level Statistics
    val totalExpenseSum = expenses.sumOf { it.amount }
    val highestExpense = expenses.maxOfOrNull { it.amount } ?: 0.0
    val averageDailyExpense = if (expenses.isNotEmpty()) totalExpenseSum / 30 else 0.0

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GlassTheme.glassTextColor())
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Analytics & Reports",
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interval selector (Daily, Weekly, Monthly, Yearly)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { interval ->
                        FilterChip(
                            selected = selectedInterval == interval,
                            onClick = { selectedInterval = interval },
                            label = { Text(interval) }
                        )
                    }
                }
            }

            // 1. Trend Line Chart (Glowmorphic)
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "SPENDING TRENDS",
                        color = GlassTheme.glassSubTextColor(),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (lastSevenDays.isNotEmpty()) {
                        GlassLineChart(
                            values = lastSevenDays,
                            labels = lineLabels,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            lineColor = Color(0xFF818CF8)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No trend records available", color = GlassTheme.glassSubTextColor())
                        }
                    }
                }
            }

            // 2. Bar Chart: Weekly Spending breakdown
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "WEEKLY DISTRIBUTION",
                        color = GlassTheme.glassSubTextColor(),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    GlassBarChart(
                        data = barData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        barColor = Color(0xFF10B981)
                    )
                }
            }

            // 3. Category Pie Chart
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "CATEGORY SPREAD",
                        color = GlassTheme.glassSubTextColor(),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        GlassPieChart(
                            data = categoryExpenses,
                            colors = chartColors,
                            modifier = Modifier.size(160.dp),
                            centerLabel = "Total Outflow"
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            categoryExpenses.entries.sortedByDescending { it.value }.take(4).forEach { entry ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(chartColors[entry.key] ?: Color.Gray)
                                    )
                                    Text(
                                        text = "${entry.key} (${((entry.value / totalExpenseSum) * 100).toInt()}%)",
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

            // 4. Detailed Stat Indicators Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left stat
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFFEC4899))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Highest Single", color = GlassTheme.glassSubTextColor(), style = MaterialTheme.typography.labelSmall)
                        Text(
                            "$currencySymbol ${formatBDT(highestExpense)}",
                            color = GlassTheme.glassTextColor(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Right stat
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Daily Avg (30d)", color = GlassTheme.glassSubTextColor(), style = MaterialTheme.typography.labelSmall)
                        Text(
                            "$currencySymbol ${formatBDT(averageDailyExpense)}",
                            color = GlassTheme.glassTextColor(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 5. Ordered Top Spending categories List
            item {
                Text(
                    text = "TOP SPENDING CATEGORIES",
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (categoryExpenses.isEmpty()) {
                item {
                    Text(
                        "No spending records to analyze.",
                        color = GlassTheme.glassSubTextColor(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                val sortedCategories = categoryExpenses.entries.sortedByDescending { it.value }
                itemsIndexed(sortedCategories) { index, entry ->
                    val percentage = (entry.value / totalExpenseSum).toFloat()

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
                            Text(
                                text = "${index + 1}",
                                color = Color(0xFF818CF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = entry.key,
                                        color = GlassTheme.glassTextColor(),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$currencySymbol ${formatBDT(entry.value)}",
                                        color = GlassTheme.glassTextColor(),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { percentage.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp),
                                    color = chartColors[entry.key] ?: Color(0xFF818CF8),
                                    trackColor = Color(0x11FFFFFF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getDayOfWeek(timestamp: Long): Int {
    return Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.DAY_OF_WEEK)
}
