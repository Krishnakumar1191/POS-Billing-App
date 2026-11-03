package com.example.posapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_transactions")
data class CashTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "CASH_IN", "CASH_OUT", "EXPENSE"
    val amount: Double,
    val reason: String,
    val timestamp: Long
)