package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.repository.ExpenseRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class QuickRow(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val amountString: String = "",
    val category: String = "Others"
)

class ExpenseViewModel(
    application: Application,
    private val repository: ExpenseRepository
) : AndroidViewModel(application) {

    private val sharedPrefs: SharedPreferences =
        application.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // 1. Transactions State
    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Categories State
    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 3. Budgets & Selected Month State
    private val _selectedMonthYear = MutableStateFlow(getCurrentMonthYearString())
    val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

    val budgets: StateFlow<List<Budget>> = _selectedMonthYear
        .flatMapLatest { monthYear -> repository.getBudgetsForMonth(monthYear) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 4. Filters State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory = _filterCategory.asStateFlow()

    private val _filterIsIncome = MutableStateFlow<Boolean?>(null)
    val filterIsIncome = _filterIsIncome.asStateFlow()

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        transactions, _searchQuery, _filterCategory, _filterIsIncome
    ) { txs, query, cat, isInc ->
        txs.filter { tx ->
            val matchesQuery = query.isEmpty() || 
                    tx.name.contains(query, ignoreCase = true) || 
                    tx.note.contains(query, ignoreCase = true) ||
                    tx.category.contains(query, ignoreCase = true)
            val matchesCat = cat == null || tx.category == cat
            val matchesIncome = isInc == null || tx.isIncome == isInc
            matchesQuery && matchesCat && matchesIncome
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 5. Quick Entry State (Shopping list style)
    private val _quickEntryRows = MutableStateFlow<List<QuickRow>>(listOf(QuickRow()))
    val quickEntryRows = _quickEntryRows.asStateFlow()

    val quickEntryTotal: StateFlow<Double> = _quickEntryRows
        .map { rows ->
            rows.sumOf { row -> row.amountString.toDoubleOrNull() ?: 0.0 }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Autocomplete/Suggestions based on historical transactions
    val historicalSuggestions: StateFlow<List<Transaction>> = transactions
        .map { list -> list.distinctBy { it.name.lowercase().trim() }.take(20) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 6. Settings States
    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode_v2", true)) // default dark mode for visual glassmorphism pop
    val isDarkMode = _isDarkMode.asStateFlow()

    private val _currencySymbol = MutableStateFlow(sharedPrefs.getString("currency_symbol", "৳") ?: "৳")
    val currencySymbol = _currencySymbol.asStateFlow()

    init {
        loadQuickEntryDraft()
    }

    // --- Action Methods ---

    fun toggleDarkMode() {
        val nextMode = !_isDarkMode.value
        _isDarkMode.value = nextMode
        sharedPrefs.edit().putBoolean("dark_mode_v2", nextMode).apply()
    }

    fun setCurrency(symbol: String) {
        _currencySymbol.value = symbol
        sharedPrefs.edit().putString("currency_symbol", symbol).apply()
    }

    fun setSelectedMonth(monthYear: String) {
        _selectedMonthYear.value = monthYear
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterCategory(category: String?) {
        _filterCategory.value = category
    }

    fun setFilterIsIncome(isIncome: Boolean?) {
        _filterIsIncome.value = isIncome
    }

    // --- Database Operations ---

    fun addTransaction(
        amount: Double,
        name: String,
        category: String,
        date: Long,
        time: String,
        isIncome: Boolean,
        paymentMethod: String,
        note: String = "",
        photoPath: String? = null,
        location: String? = null
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    amount = amount,
                    name = name,
                    category = category,
                    date = date,
                    time = time,
                    isIncome = isIncome,
                    paymentMethod = paymentMethod,
                    note = note,
                    photoPath = photoPath,
                    location = location
                )
            )
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun duplicateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction.copy(id = 0, date = System.currentTimeMillis()))
        }
    }

    fun addCategory(name: String, colorHex: Long, iconName: String) {
        viewModelScope.launch {
            repository.insertCategory(
                Category(name = name, colorHex = colorHex, iconName = iconName, isCustom = true)
            )
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun setBudget(categoryName: String, amount: Double) {
        viewModelScope.launch {
            val existing = budgets.value.find { it.categoryName == categoryName }
            val budget = existing?.copy(amount = amount) ?: Budget(
                categoryName = categoryName,
                amount = amount,
                monthYear = _selectedMonthYear.value
            )
            repository.insertBudget(budget)
        }
    }

    // --- Quick Entry Row Manipulation ---

    fun updateQuickRow(index: Int, updated: QuickRow) {
        val current = _quickEntryRows.value.toMutableList()
        if (index in current.indices) {
            current[index] = updated
            _quickEntryRows.value = current
            saveQuickEntryDraft(current)
        }
    }

    fun addQuickRow() {
        val current = _quickEntryRows.value.toMutableList()
        current.add(QuickRow())
        _quickEntryRows.value = current
        saveQuickEntryDraft(current)
    }

    fun removeQuickRow(index: Int) {
        val current = _quickEntryRows.value.toMutableList()
        if (current.size > 1 && index in current.indices) {
            current.removeAt(index)
        } else if (current.size == 1 && index == 0) {
            current[0] = QuickRow()
        }
        _quickEntryRows.value = current
        saveQuickEntryDraft(current)
    }

    fun saveAllQuickRows() {
        viewModelScope.launch {
            val currentDate = System.currentTimeMillis()
            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTimeStr = sdfTime.format(Date())

            _quickEntryRows.value.forEach { row ->
                val amount = row.amountString.toDoubleOrNull()
                if (amount != null && amount > 0.0 && row.name.isNotBlank()) {
                    repository.insertTransaction(
                        Transaction(
                            amount = amount,
                            name = row.name.trim(),
                            category = row.category,
                            date = currentDate,
                            time = currentTimeStr,
                            isIncome = false,
                            paymentMethod = "Cash",
                            note = "Quick Entry Transaction"
                        )
                    )
                }
            }

            // Reset rows after success saving
            val freshList = listOf(QuickRow())
            _quickEntryRows.value = freshList
            saveQuickEntryDraft(freshList)
        }
    }

    // --- Persistence of Drafts using Moshi JSON adapter ---

    private fun saveQuickEntryDraft(rows: List<QuickRow>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val type = Types.newParameterizedType(List::class.java, QuickRow::class.java)
                val adapter = moshi.adapter<List<QuickRow>>(type)
                val json = adapter.toJson(rows)
                sharedPrefs.edit().putString("quick_entry_draft_v2", json).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadQuickEntryDraft() {
        val json = sharedPrefs.getString("quick_entry_draft_v2", null)
        if (json != null) {
            try {
                val type = Types.newParameterizedType(List::class.java, QuickRow::class.java)
                val adapter = moshi.adapter<List<QuickRow>>(type)
                val loaded = adapter.fromJson(json)
                if (!loaded.isNullOrEmpty()) {
                    _quickEntryRows.value = loaded
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Import / Export of Backup Data ---

    fun exportBackup(context: Context, onComplete: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val txsList = transactions.value
                val catsList = categories.value
                val bgsList = budgets.value

                val backupData = mapOf(
                    "transactions" to txsList,
                    "categories" to catsList,
                    "budgets" to bgsList
                )

                val type = Types.newParameterizedType(
                    Map::class.java,
                    String::class.java,
                    Any::class.java
                )
                // A simplified Moshi export logic
                val adapter = moshi.adapter<Map<String, Any>>(type)
                val jsonString = adapter.toJson(backupData)

                withContext(Dispatchers.Main) {
                    onComplete(jsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete("")
                }
            }
        }
    }

    fun importBackup(jsonString: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Parsing manually from JSON to maintain stability
                val type = Types.newParameterizedType(
                    Map::class.java,
                    String::class.java,
                    Any::class.java
                )
                val adapter = moshi.adapter<Map<String, Any>>(type)
                val data = adapter.fromJson(jsonString)

                if (data != null) {
                    // Safe parsing
                    val txs = data["transactions"] as? List<*>
                    val cats = data["categories"] as? List<*>
                    val bgs = data["budgets"] as? List<*>

                    // Overwrite database or merge. Let's merge nicely.
                    // To keep implementation simple and robust, we can clear and import,
                    // or just write individual records. Let's add them to the database.
                    txs?.forEach { item ->
                        val itemJson = moshi.adapter(Transaction::class.java).fromJsonValue(item)
                        if (itemJson != null) repository.insertTransaction(itemJson)
                    }
                    cats?.forEach { item ->
                        val itemJson = moshi.adapter(Category::class.java).fromJsonValue(item)
                        if (itemJson != null) repository.insertCategory(itemJson)
                    }
                    bgs?.forEach { item ->
                        val itemJson = moshi.adapter(Budget::class.java).fromJsonValue(item)
                        if (itemJson != null) repository.insertBudget(itemJson)
                    }

                    withContext(Dispatchers.Main) {
                        onComplete(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    // --- Helper functions ---

    private fun getCurrentMonthYearString(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }
}

class ExpenseViewModelFactory(
    private val application: Application,
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
