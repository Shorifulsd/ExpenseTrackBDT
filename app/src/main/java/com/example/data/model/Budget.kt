package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryName: String, // "Total" for overall, or specific category name
    val amount: Double,
    val monthYear: String // "yyyy-MM" format, e.g. "2026-07"
)
