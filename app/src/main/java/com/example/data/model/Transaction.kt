package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val name: String,
    val category: String,
    val date: Long, // timestamp
    val time: String, // "HH:mm"
    val isIncome: Boolean,
    val paymentMethod: String, // "Cash", "Card", "bKash", "Nagad", etc.
    val note: String = "",
    val photoPath: String? = null,
    val location: String? = null
)
