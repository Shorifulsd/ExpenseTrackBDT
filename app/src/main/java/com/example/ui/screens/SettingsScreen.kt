package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.viewmodel.ExpenseViewModel

@Composable
fun SettingsScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showExportDialog by remember { mutableStateOf(false) }
    var exportString by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importInputString by remember { mutableStateOf("") }

    // Placeholders
    var pinLockEnabled by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

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
                    text = "Settings & Backups",
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
            // General Title
            item {
                Text(
                    text = "PREFERENCES",
                    color = GlassTheme.glassSubTextColor(),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp
                )
            }

            // Theme Setting
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = Color(0xFF818CF8)
                            )
                            Column {
                                Text("Aesthetic Glass Theme", color = GlassTheme.glassTextColor(), fontWeight = FontWeight.Bold)
                                Text("Toggle Light vs Dark Modes", color = GlassTheme.glassSubTextColor(), style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF6366F1)
                            )
                        )
                    }
                }
            }

            // Currency selection
            item {
                var expandedCurrency by remember { mutableStateOf(false) }

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedCurrency = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = Color(0xFF818CF8)
                            )
                            Column {
                                Text("Default Currency", color = GlassTheme.glassTextColor(), fontWeight = FontWeight.Bold)
                                Text("Active currency: $currencySymbol", color = GlassTheme.glassSubTextColor(), style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Box {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(currencySymbol, color = Color(0xFF818CF8), fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = GlassTheme.glassTextColor())
                            }

                            DropdownMenu(
                                expanded = expandedCurrency,
                                onDismissRequest = { expandedCurrency = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Bangladeshi Taka (৳)") },
                                    onClick = {
                                        viewModel.setCurrency("৳")
                                        expandedCurrency = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("US Dollar ($)") },
                                    onClick = {
                                        viewModel.setCurrency("$")
                                        expandedCurrency = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Euro (€)") },
                                    onClick = {
                                        viewModel.setCurrency("€")
                                        expandedCurrency = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Security Section
            item {
                Text(
                    text = "SECURITY & NOTIFICATIONS",
                    color = GlassTheme.glassSubTextColor(),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // PIN Lock Placeholder
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF818CF8))
                            Column {
                                Text("PIN Code Lock", color = GlassTheme.glassTextColor(), fontWeight = FontWeight.Bold)
                                Text("Secure access on launch (Demo)", color = GlassTheme.glassSubTextColor(), style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Switch(
                            checked = pinLockEnabled,
                            onCheckedChange = { pinLockEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF6366F1)
                            )
                        )
                    }
                }
            }

            // Biometric Lock Placeholder
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF818CF8))
                            Column {
                                Text("Biometric Lock", color = GlassTheme.glassTextColor(), fontWeight = FontWeight.Bold)
                                Text("Use fingerprint sensor (Demo)", color = GlassTheme.glassSubTextColor(), style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { biometricEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF6366F1)
                            )
                        )
                    }
                }
            }

            // Notification toggle Placeholder
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF818CF8))
                            Column {
                                Text("Daily Reminder", color = GlassTheme.glassTextColor(), fontWeight = FontWeight.Bold)
                                Text("Receive daily notifications at 8 PM", color = GlassTheme.glassSubTextColor(), style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF6366F1)
                            )
                        )
                    }
                }
            }

            // Backup & Restore
            item {
                Text(
                    text = "BACKUP & RESTORE DATA",
                    color = GlassTheme.glassSubTextColor(),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Backup actions card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.exportBackup(context) { jsonString ->
                                        if (jsonString.isNotBlank()) {
                                            exportString = jsonString
                                            showExportDialog = true
                                        } else {
                                            Toast.makeText(context, "Failed to generate backup", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF818CF8))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Export Data Backup", color = GlassTheme.glassTextColor(), fontWeight = FontWeight.Bold)
                                Text("Copy or share database JSON string", color = GlassTheme.glassSubTextColor(), style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GlassTheme.glassTextColor())
                        }

                        HorizontalDivider(color = Color(0x11FFFFFF))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showImportDialog = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF10B981))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Import Data Backup", color = GlassTheme.glassTextColor(), fontWeight = FontWeight.Bold)
                                Text("Paste database JSON string to restore", color = GlassTheme.glassSubTextColor(), style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GlassTheme.glassTextColor())
                        }
                    }
                }
            }

            // App details
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Taka Expense Tracker",
                        color = GlassTheme.glassSubTextColor(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Version 1.0.0 (Offline First)",
                        color = GlassTheme.glassSubTextColor(),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Export Dialog
        if (showExportDialog) {
            GlassDialog(onDismissRequest = { showExportDialog = false }) {
                Text(
                    "Export Backup Successful!",
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    "Copy the backup string below and save it somewhere secure. You can use it later to restore all transactions, budgets, and settings.",
                    color = GlassTheme.glassSubTextColor(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Scrollable container for JSON
                OutlinedTextField(
                    value = exportString,
                    onValueChange = {},
                    readOnly = true,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassTheme.glassTextColor(),
                        unfocusedTextColor = GlassTheme.glassTextColor()
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        onClick = { showExportDialog = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = GlassTheme.glassTextColor())
                    }

                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(exportString))
                            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            showExportDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy String", color = Color.White)
                    }
                }
            }
        }

        // Import Dialog
        if (showImportDialog) {
            GlassDialog(onDismissRequest = { showImportDialog = false }) {
                Text(
                    "Import Backup",
                    color = GlassTheme.glassTextColor(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    "Paste your previously exported database JSON backup string below and click Restore to load your data. This merges with current records.",
                    color = GlassTheme.glassSubTextColor(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = importInputString,
                    onValueChange = { importInputString = it },
                    placeholder = { Text("Paste JSON string here...") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassTheme.glassTextColor(),
                        unfocusedTextColor = GlassTheme.glassTextColor(),
                        focusedBorderColor = Color(0xFF818CF8)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        onClick = { showImportDialog = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = GlassTheme.glassTextColor())
                    }

                    Button(
                        onClick = {
                            if (importInputString.isNotBlank()) {
                                viewModel.importBackup(importInputString) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Data Restored Successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Invalid Backup String Format", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                showImportDialog = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Restore Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
