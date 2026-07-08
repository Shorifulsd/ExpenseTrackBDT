package com.example.data.repository

import com.example.data.local.BudgetDao
import com.example.data.local.CategoryDao
import com.example.data.local.TransactionDao
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>> {
        return budgetDao.getBudgetsForMonth(monthYear)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }
}
