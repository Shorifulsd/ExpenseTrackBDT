package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: Long, // Hex color code
    val iconName: String, // Material symbol descriptor
    val isCustom: Boolean = false
) {
    companion object {
        val defaultCategories = listOf(
            Category(name = "Food", colorHex = 0xFFFF5722, iconName = "Restaurant"),
            Category(name = "Transport", colorHex = 0xFF2196F3, iconName = "DirectionsCar"),
            Category(name = "Shopping", colorHex = 0xFFE91E63, iconName = "ShoppingBag"),
            Category(name = "Bills", colorHex = 0xFF9C27B0, iconName = "ReceiptLong"),
            Category(name = "Salary", colorHex = 0xFF4CAF50, iconName = "Payments"),
            Category(name = "Entertainment", colorHex = 0xFFFFC107, iconName = "SportsEsports"),
            Category(name = "Education", colorHex = 0xFF3F51B5, iconName = "School"),
            Category(name = "Health", colorHex = 0xFFE91E63, iconName = "MedicalServices"),
            Category(name = "Family", colorHex = 0xFF795548, iconName = "Home"),
            Category(name = "Travel", colorHex = 0xFF00BCD4, iconName = "Flight"),
            Category(name = "Investment", colorHex = 0xFF009688, iconName = "TrendingUp"),
            Category(name = "Gifts", colorHex = 0xFFFF4081, iconName = "CardGiftcard"),
            Category(name = "Others", colorHex = 0xFF607D8B, iconName = "Category")
        )
    }
}
