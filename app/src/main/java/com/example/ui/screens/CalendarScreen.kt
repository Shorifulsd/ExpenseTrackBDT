package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.components.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var calendarMonthOffset by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    // Generate calendar details
    val currentCal = remember(calendarMonthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, calendarMonthOffset)
        }
    }

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthYearTitle = monthYearFormat.format(currentCal.time)

    // Slicing calendar days
    val daysInMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = currentCal.run {
        val temp = clone() as Calendar
        temp.set(Calendar.DAY_OF_MONTH, 1)
        temp.get(Calendar.DAY_OF_WEEK)
    }

    // Grid details
    val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val totalGridSpaces = daysInMonth + (firstDayOfWeek - 1)

    // Transactions filtering for the selected day
    val selectedDayTransactions = remember(transactions, selectedDate) {
        transactions.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
            txCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
            txCal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
        }
    }

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
                    text = "Transaction Calendar",
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calendar Header Navigation
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { calendarMonthOffset-- }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = GlassTheme.glassTextColor())
                    }

                    Text(
                        text = monthYearTitle.uppercase(),
                        color = GlassTheme.glassTextColor(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    IconButton(onClick = { calendarMonthOffset++ }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = GlassTheme.glassTextColor())
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Days of Week Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekDays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = GlassTheme.glassSubTextColor(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grid Calendar Content
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    var currentDayIdx = 1
                    val rowsCount = (totalGridSpaces + 6) / 7

                    for (r in 0 until rowsCount) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (c in 1..7) {
                                val gridIdx = r * 7 + c
                                val dayNum = gridIdx - (firstDayOfWeek - 1)

                                if (dayNum in 1..daysInMonth) {
                                    val dayCal = (currentCal.clone() as Calendar).apply {
                                        set(Calendar.DAY_OF_MONTH, dayNum)
                                    }

                                    // Determine day status
                                    val dayTxs = transactions.filter { tx ->
                                        val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                                        txCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                        txCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
                                    }

                                    val hasIncome = dayTxs.any { it.isIncome }
                                    val hasExpense = dayTxs.any { !it.isIncome }
                                    val highSpending = dayTxs.filter { !it.isIncome }.sumOf { it.amount } >= 1000.0

                                    val isSelected = selectedDate.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                            selectedDate.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color(0xFF6366F1) 
                                                else if (highSpending) Color(0x33EC4899) 
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color.White 
                                                       else if (highSpending) Color(0x66EC4899) 
                                                       else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedDate = dayCal },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$dayNum",
                                                color = if (isSelected) Color.White else GlassTheme.glassTextColor(),
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                fontSize = 14.sp
                                            )

                                            // Draw small indicator dots
                                            if (dayTxs.isNotEmpty() && !isSelected) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    if (hasIncome) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFF4CAF50))
                                                        )
                                                    }
                                                    if (hasExpense) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFFEC4899))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Day Transactions Detail List
            Text(
                text = "TRANSACTIONS ON ${SimpleDateFormat("dd MMMM", Locale.getDefault()).format(selectedDate.time).uppercase()}",
                color = GlassTheme.glassTextColor(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (selectedDayTransactions.isEmpty()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No transactions recorded for this day.",
                            color = GlassTheme.glassSubTextColor(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(selectedDayTransactions) { tx ->
                        TransactionRow(tx = tx, currencySymbol = currencySymbol)
                    }
                }
            }
        }
    }
}
