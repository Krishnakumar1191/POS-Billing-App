package com.example.posapp.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_adjustments", indices = [Index("productId")])
data class InventoryAdjustment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val quantityChange: Double,
    val reason: String,
    val timestamp: Long
)