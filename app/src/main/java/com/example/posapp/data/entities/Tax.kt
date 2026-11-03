package com.example.posapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_taxes")
data class Tax(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val amountType: String = "percent", // 'fixed', 'percent', 'group'
    val typeTaxUse: String = "sale", // 'none', 'sale', 'purchase'
    val priceInclude: Boolean = false
)