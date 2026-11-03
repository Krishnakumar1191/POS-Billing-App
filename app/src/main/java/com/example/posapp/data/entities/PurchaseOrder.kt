package com.example.posapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_orders")
data class PurchaseOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val timestamp: Long,
    val totalAmount: Double,
    val vendorName: String
)