package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.components.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseManagerScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterCategory by viewModel.filterCategory.collectAsState()
    val filterIsIncome by viewModel.filterIsIncome.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val context = LocalContext.current
    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedTransactionForAction by remember { mutableStateOf<Transaction?>(null) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

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
                    text = "Transaction Manager",
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { 
                    editingTransaction = null
                    showAddEditDialog = true 
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Custom", tint = Color(0xFF818CF8))
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    editingTransaction = null
                    showAddEditDialog = true 
                },
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp) // Leave room for floating bottom bar
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // 1. Search Bar (Frosted)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search by name, category, or note...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = GlassTheme.glassSubTextColor()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GlassTheme.glassTextColor(),
                    unfocusedTextColor = GlassTheme.glassTextColor(),
                    focusedBorderColor = Color(0xFF818CF8),
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    focusedContainerColor = Color(0x11FFFFFF),
                    unfocusedContainerColor = Color(0x05FFFFFF)
                )
            )

            // 2. Horizontal Scroll Filters Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Filter Chips: All, Income, Expense
                FilterChip(
                    selected = filterIsIncome == null,
                    onClick = { viewModel.setFilterIsIncome(null) },
                    label = { Text("All Types") }
                )
                FilterChip(
                    selected = filterIsIncome == true,
                    onClick = { viewModel.setFilterIsIncome(true) },
                    label = { Text("Income") }
                )
                FilterChip(
                    selected = filterIsIncome == false,
                    onClick = { viewModel.setFilterIsIncome(false) },
                    label = { Text("Expense") }
                )

                Spacer(modifier = Modifier.width(8.dp))
                VerticalDivider(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.width(8.dp))

                // Categories Filter Chips
                FilterChip(
                    selected = filterCategory == null,
                    onClick = { viewModel.setFilterCategory(null) },
                    label = { Text("All Categories") }
                )

                val defaultCats = listOf(
                    "Food", "Transport", "Shopping", "Bills", "Salary", "Entertainment",
                    "Education", "Health", "Family", "Travel", "Investment", "Gifts", "Others"
                )

                defaultCats.forEach { cat ->
                    FilterChip(
                        selected = filterCategory == cat,
                        onClick = { viewModel.setFilterCategory(if (filterCategory == cat) null else cat) },
                        label = { Text(cat) }
                    )
                }
            }

            // 3. Transactions List with Sticky Date Headers
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = GlassTheme.glassSubTextColor(),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No Transactions Match Filters",
                            color = GlassTheme.glassSubTextColor(),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                val groupedTransactions = filteredTransactions.groupBy {
                    SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(it.date))
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    groupedTransactions.forEach { (dateStr, txList) ->
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xCC000000))
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = dateStr.uppercase(),
                                    color = Color(0xFF06B6D4),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        items(txList, key = { it.id }) { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTransactionForAction = tx }
                            ) {
                                TransactionRow(tx = tx, currencySymbol = currencySymbol)
                            }
                        }
                    }
                }
            }
        }

        // Action Sheet / Dialog for Selected Transaction (Edit, Duplicate, Delete)
        selectedTransactionForAction?.let { tx ->
            GlassDialog(onDismissRequest = { selectedTransactionForAction = null }) {
                Text(
                    text = tx.name,
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "${tx.category} • ${if (tx.isIncome) "Income" else "Expense"}",
                    color = GlassTheme.glassSubTextColor(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider(color = Color(0x1FFFFFFF), modifier = Modifier.padding(bottom = 12.dp))

                // Action rows
                ActionRowItem(
                    icon = Icons.Default.Edit,
                    label = "Edit Transaction",
                    onClick = {
                        editingTransaction = tx
                        selectedTransactionForAction = null
                        showAddEditDialog = true
                    }
                )
                ActionRowItem(
                    icon = Icons.Default.ContentCopy,
                    label = "Duplicate Transaction",
                    onClick = {
                        viewModel.duplicateTransaction(tx)
                        selectedTransactionForAction = null
                    }
                )
                ActionRowItem(
                    icon = Icons.Default.Delete,
                    label = "Delete Transaction",
                    color = Color(0xFFF43F5E),
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        selectedTransactionForAction = null
                    }
                )
            }
        }

        // Add/Edit transaction Dialog
        if (showAddEditDialog) {
            AddEditTransactionDialog(
                transaction = editingTransaction,
                onDismiss = { showAddEditDialog = false },
                onSave = { amount, name, category, date, time, isIncome, payMethod, note ->
                    if (editingTransaction != null) {
                        viewModel.updateTransaction(
                            editingTransaction!!.copy(
                                amount = amount,
                                name = name,
                                category = category,
                                date = date,
                                time = time,
                                isIncome = isIncome,
                                paymentMethod = payMethod,
                                note = note
                            )
                        )
                    } else {
                        viewModel.addTransaction(
                            amount = amount,
                            name = name,
                            category = category,
                            date = date,
                            time = time,
                            isIncome = isIncome,
                            paymentMethod = payMethod,
                            note = note
                        )
                    }
                    showAddEditDialog = false
                }
            )
        }
    }
}

@Composable
fun ActionRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = GlassTheme.glassTextColor(),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Text(label, color = color, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun AddEditTransactionDialog(
    transaction: Transaction?,
    onDismiss: () -> Unit,
    onSave: (Double, String, String, Long, String, Boolean, String, String) -> Unit
) {
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var name by remember { mutableStateOf(transaction?.name ?: "") }
    var category by remember { mutableStateOf(transaction?.category ?: "Food") }
    var date by remember { mutableStateOf(transaction?.date ?: System.currentTimeMillis()) }
    var time by remember { mutableStateOf(transaction?.time ?: "12:00") }
    var isIncome by remember { mutableStateOf(transaction?.isIncome ?: false) }
    var paymentMethod by remember { mutableStateOf(transaction?.paymentMethod ?: "Cash") }
    var note by remember { mutableStateOf(transaction?.note ?: "") }

    val context = LocalContext.current
    var showCategorySelector by remember { mutableStateOf(false) }

    GlassDialog(onDismissRequest = onDismiss) {
        Text(
            text = if (transaction != null) "Edit Transaction" else "Add Transaction",
            color = GlassTheme.glassTextColor(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            // Type Toggle: Expense / Income
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x11FFFFFF))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isIncome) Color(0xFFEC4899) else Color.Transparent)
                            .clickable { isIncome = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Expense", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isIncome) Color(0xFF4CAF50) else Color.Transparent)
                            .clickable { isIncome = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Income", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Amount Input
            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("৳ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassTheme.glassTextColor(),
                        unfocusedTextColor = GlassTheme.glassTextColor(),
                        focusedBorderColor = Color(0xFF818CF8)
                    )
                )
            }

            // Name Input
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassTheme.glassTextColor(),
                        unfocusedTextColor = GlassTheme.glassTextColor(),
                        focusedBorderColor = Color(0xFF818CF8)
                    )
                )
            }

            // Category Picker
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                        .clickable { showCategorySelector = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(getCategoryIcon(category), contentDescription = null, tint = Color(0xFF818CF8))
                        Text(category, color = GlassTheme.glassTextColor())
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = GlassTheme.glassTextColor())
                }
            }

            // Date Picker Trigger
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = date }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    }
                                    date = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF818CF8))
                        Text(
                            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(date)),
                            color = GlassTheme.glassTextColor()
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = GlassTheme.glassTextColor())
                }
            }

            // Payment Method
            item {
                var expandedPay by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                            .clickable { expandedPay = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payment Method: $paymentMethod", color = GlassTheme.glassTextColor())
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = GlassTheme.glassTextColor())
                    }

                    DropdownMenu(
                        expanded = expandedPay,
                        onDismissRequest = { expandedPay = false }
                    ) {
                        listOf("Cash", "Card", "bKash", "Nagad", "Rocket", "Bank Transfer").forEach { pay ->
                            DropdownMenuItem(
                                text = { Text(pay) },
                                onClick = {
                                    paymentMethod = pay
                                    expandedPay = false
                                }
                            )
                        }
                    }
                }
            }

            // Note Input
            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassTheme.glassTextColor(),
                        unfocusedTextColor = GlassTheme.glassTextColor(),
                        focusedBorderColor = Color(0xFF818CF8)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save & Dismiss Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", color = GlassTheme.glassTextColor())
            }

            Button(
                onClick = {
                    val doubleAmt = amount.toDoubleOrNull()
                    if (doubleAmt != null && doubleAmt > 0 && name.isNotBlank()) {
                        onSave(doubleAmt, name.trim(), category, date, time, isIncome, paymentMethod, note)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Category dialog selector trigger inside dialog
        if (showCategorySelector) {
            GlassDialog(onDismissRequest = { showCategorySelector = false }) {
                Text(
                    "Select Category",
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val defaultCats = listOf(
                    "Food", "Transport", "Shopping", "Bills", "Salary", "Entertainment",
                    "Education", "Health", "Family", "Travel", "Investment", "Gifts", "Others"
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(defaultCats) { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    category = cat
                                    showCategorySelector = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(getCategoryIcon(cat), contentDescription = null, tint = Color(0xFF818CF8))
                            Text(cat, color = GlassTheme.glassTextColor())
                        }
                    }
                }
            }
        }
    }
}
