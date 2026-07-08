package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.Calendar

@Composable
fun BudgetScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val budgets by viewModel.budgets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var selectedCategoryForBudget by remember { mutableStateOf("") }
    var inputAmountForBudget by remember { mutableStateOf("") }

    val defaultCats = listOf(
        "Total", "Food", "Transport", "Shopping", "Bills", "Entertainment",
        "Education", "Health", "Family", "Travel", "Investment", "Gifts", "Others"
    )

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
                    text = "Budgets",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "SET LIMITS FOR BALANCED LIVING",
                    color = GlassTheme.glassSubTextColor(),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(defaultCats) { category ->
                val currentBudget = budgets.find { it.categoryName == category }?.amount ?: 0.0
                
                // Calculate actual spending for this category in the current month
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)

                val actualSpending = transactions.filter { tx ->
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                    !tx.isIncome && 
                    txCal.get(Calendar.MONTH) == currentMonth && 
                    txCal.get(Calendar.YEAR) == currentYear &&
                    (category == "Total" || tx.category == category)
                }.sumOf { it.amount }

                val progress = if (currentBudget > 0.0) (actualSpending / currentBudget).toFloat() else 0f
                val remaining = currentBudget - actualSpending

                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (category == "Total") "OVERALL MONTHLY BUDGET" else category.uppercase(),
                                color = if (category == "Total") Color(0xFF818CF8) else GlassTheme.glassTextColor(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (currentBudget > 0.0) {
                                    "Limit: $currencySymbol ${formatBDT(currentBudget)} • Spent: $currencySymbol ${formatBDT(actualSpending)}"
                                } else {
                                    "No Limit Set • Spent: $currencySymbol ${formatBDT(actualSpending)}"
                                },
                                color = GlassTheme.glassSubTextColor(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedCategoryForBudget = category
                                inputAmountForBudget = if (currentBudget > 0.0) currentBudget.toInt().toString() else ""
                                showEditBudgetDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Budget", tint = Color(0xFF818CF8))
                        }
                    }

                    if (currentBudget > 0.0) {
                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = if (progress > 0.9f) Color(0xFFEC4899) else Color(0xFF10B981),
                            trackColor = Color(0x11FFFFFF)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(progress * 100).toInt()}% Used",
                                color = if (progress > 0.9f) Color(0xFFEC4899) else GlassTheme.glassSubTextColor(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = if (remaining < 0) "Over by $currencySymbol ${formatBDT(-remaining)}" else "Remaining: $currencySymbol ${formatBDT(remaining)}",
                                color = if (remaining < 0) Color(0xFFEC4899) else Color(0xFF10B981),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Edit Budget Dialog
        if (showEditBudgetDialog) {
            GlassDialog(onDismissRequest = { showEditBudgetDialog = false }) {
                Text(
                    text = "Configure Budget",
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Set monthly limit for: $selectedCategoryForBudget",
                    color = GlassTheme.glassSubTextColor(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = inputAmountForBudget,
                    onValueChange = { inputAmountForBudget = it },
                    label = { Text("Budget Limit") },
                    prefix = { Text("৳ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassTheme.glassTextColor(),
                        unfocusedTextColor = GlassTheme.glassTextColor(),
                        focusedBorderColor = Color(0xFF818CF8)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        onClick = { showEditBudgetDialog = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = GlassTheme.glassTextColor())
                    }

                    Button(
                        onClick = {
                            val limit = inputAmountForBudget.toDoubleOrNull() ?: 0.0
                            viewModel.setBudget(selectedCategoryForBudget, limit)
                            showEditBudgetDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
