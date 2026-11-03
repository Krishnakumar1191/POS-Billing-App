package com.example.posapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_logs")
data class StockLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val changeAmount: Double, // positive for increase, negative for decrease
    val type: String, // "SALE", "ADJUSTMENT", "INITIAL"
    val reason: String,
    val timestamp: Long
)