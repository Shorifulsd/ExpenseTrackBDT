package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.ui.components.*
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.QuickRow
import kotlinx.coroutines.launch

@Composable
fun QuickEntryScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val rows by viewModel.quickEntryRows.collectAsState()
    val runningTotal by viewModel.quickEntryTotal.collectAsState()
    val suggestions by viewModel.historicalSuggestions.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Popular default suggestions for Bangladesh context
    val defaultSuggestions = listOf(
        Pair("Rice", "80.0"),
        Pair("Fish", "350.0"),
        Pair("Beef", "750.0"),
        Pair("Vegetables", "60.0"),
        Pair("Oil", "180.0"),
        Pair("Milk", "90.0"),
        Pair("Egg", "150.0"),
        Pair("Transport", "50.0"),
        Pair("Rickshaw", "30.0"),
        Pair("Mobile Recharge", "200.0"),
        Pair("Rent", "15000.0")
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    text = "Quick Expense Entry",
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                // Info button
                IconButton(onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Enter multiple items like a shopping list. Valid numbers save automatically!")
                    }
                }) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = GlassTheme.glassTextColor())
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // 1. Large Animated Total Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RUNNING TOTAL",
                        color = GlassTheme.glassSubTextColor(),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AnimatedCounter(
                        value = runningTotal,
                        prefix = "$currencySymbol ",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp
                        )
                    )
                }
            }

            // 2. Suggestions Pill Panel
            Text(
                "POPULAR SUGGESTIONS",
                color = GlassTheme.glassSubTextColor(),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pre-populate with defaults or historical transactions
                val activeSuggestions = if (suggestions.isNotEmpty()) {
                    suggestions.map { Pair(it.name, it.amount.toString()) }
                } else {
                    defaultSuggestions
                }

                activeSuggestions.forEach { (name, amount) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x11FFFFFF))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                            .clickable {
                                // Add or replace last empty row with this suggestion
                                val lastRowIndex = rows.size - 1
                                if (lastRowIndex >= 0 && rows[lastRowIndex].name.isBlank() && rows[lastRowIndex].amountString.isBlank()) {
                                    viewModel.updateQuickRow(
                                        lastRowIndex,
                                        QuickRow(name = name, amountString = amount, category = inferCategory(name))
                                    )
                                } else {
                                    // Add a new row
                                    viewModel.addQuickRow()
                                    viewModel.updateQuickRow(
                                        rows.size, // because a new row gets added at the end
                                        QuickRow(name = name, amountString = amount, category = inferCategory(name))
                                    )
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = name,
                            color = GlassTheme.glassTextColor(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 3. Dynamic Interactive List of Shopping Rows
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(rows, key = { _, row -> row.id }) { index, row ->
                    QuickRowItem(
                        row = row,
                        index = index,
                        currencySymbol = currencySymbol,
                        onUpdate = { updatedRow ->
                            viewModel.updateQuickRow(index, updatedRow)
                        },
                        onDelete = {
                            viewModel.removeQuickRow(index)
                        },
                        onNextRow = {
                            viewModel.addQuickRow()
                            scope.launch {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        }
                    )
                }

                // Add row button inside list
                item {
                    GlassButton(
                        onClick = { viewModel.addQuickRow() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x1A6366F1),
                            contentColor = Color(0xFF818CF8)
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Item Row", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 4. Save Button (One Tap Saving)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (runningTotal > 0.0) {
                        viewModel.saveAllQuickRows()
                        scope.launch {
                            snackbarHostState.showSnackbar("All items saved successfully!")
                        }
                        onNavigateBack()
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please add at least one valid item and amount.")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
                    .height(56.dp)
                    .border(1.dp, androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF9333EA))), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save All Expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuickRowItem(
    row: QuickRow,
    index: Int,
    currencySymbol: String,
    onUpdate: (QuickRow) -> Unit,
    onDelete: () -> Unit,
    onNextRow: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x13FFFFFF), RoundedCornerShape(16.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Name Field
        OutlinedTextField(
            value = row.name,
            onValueChange = { 
                onUpdate(row.copy(name = it, category = inferCategory(it))) 
            },
            placeholder = { Text("Item (e.g. Rice)") },
            modifier = Modifier.weight(1.5f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GlassTheme.glassTextColor(),
                unfocusedTextColor = GlassTheme.glassTextColor(),
                focusedBorderColor = Color(0xFF818CF8),
                unfocusedBorderColor = Color(0x33FFFFFF),
                focusedContainerColor = Color(0x05FFFFFF),
                unfocusedContainerColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Right)
                }
            )
        )

        // Amount Field
        OutlinedTextField(
            value = row.amountString,
            onValueChange = { input ->
                if (input.isEmpty() || input.toDoubleOrNull() != null) {
                    onUpdate(row.copy(amountString = input))
                }
            },
            placeholder = { Text("Cost") },
            prefix = { Text(currencySymbol) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GlassTheme.glassTextColor(),
                unfocusedTextColor = GlassTheme.glassTextColor(),
                focusedBorderColor = Color(0xFF818CF8),
                unfocusedBorderColor = Color(0x33FFFFFF),
                focusedContainerColor = Color(0x05FFFFFF),
                unfocusedContainerColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onNextRow()
                }
            )
        )

        // Category Selection Dialog trigger or drop-down icon button
        var showCatDialog by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x11FFFFFF))
                .clickable { showCatDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                getCategoryIcon(row.category),
                contentDescription = row.category,
                tint = Color(0xFF818CF8),
                modifier = Modifier.size(20.dp)
            )
        }

        // Delete Row Button
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "Delete Row",
                tint = Color(0xFFF43F5E),
                modifier = Modifier.size(20.dp)
            )
        }

        // Horizontal mini category picker dialog
        if (showCatDialog) {
            GlassDialog(onDismissRequest = { showCatDialog = false }) {
                Text(
                    "Select Category",
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val availableCategories = listOf(
                    "Food", "Transport", "Shopping", "Bills", "Salary", "Entertainment",
                    "Education", "Health", "Family", "Travel", "Investment", "Gifts", "Others"
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(availableCategories) { _, cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(row.copy(category = cat))
                                    showCatDialog = false
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

// Basic contextual category inference for easy auto-filling
fun inferCategory(itemName: String): String {
    return when (itemName.lowercase().trim()) {
        "rice", "fish", "beef", "chicken", "meat", "vegetables", "oil", "milk", "egg", "groceries", "food", "lunch", "dinner", "breakfast", "curry" -> "Food"
        "transport", "bus", "train", "rickshaw", "uber", "pathao", "cng", "fuel", "petrol", "bike" -> "Transport"
        "cloth", "shirt", "pant", "shoe", "dress", "shopping", "daraz", "perfume", "watch" -> "Shopping"
        "bill", "electricity", "gas", "water", "wifi", "internet", "rent", "current bill" -> "Bills"
        "salary", "bonus", "freelance" -> "Salary"
        "movie", "game", "netflix", "spotify", "ticket", "outing", "hangout" -> "Entertainment"
        "school", "college", "university", "book", "pen", "coaching", "exam fee" -> "Education"
        "medicine", "doctor", "hospital", "clinic", "pharmacy", "medical" -> "Health"
        "home", "family", "basa", "gift to mother", "abbu", "ammu" -> "Family"
        "flight", "hotel", "travel", "tour", "cox's bazar", "sajek" -> "Travel"
        "stock", "share", "crypto", "saving", "dps", "fdr" -> "Investment"
        "gift", "marriage", "birthday", "present" -> "Gifts"
        else -> "Others"
    }
}
