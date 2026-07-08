package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ExpenseTrackerApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { 
        ExpenseRepository(
            database.transactionDao(),
            database.categoryDao(),
            database.budgetDao()
        ) 
    }
}
