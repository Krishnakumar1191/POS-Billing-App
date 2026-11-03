package com.example.posapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double, // Sales Price (list_price)
    val costPrice: Double = 0.0, // Purchase Price (standard_price)
    val stock: Double, // qty_available
    val categoryId: Long? = null,
    val barcode: String? = null,
    val type: String = "consu", // 'consu' (Consumable), 'service' (Service), 'product' (Storable)
    val tracking: String = "none", // 'none', 'lot', 'serial'
    val description: String? = null,
    val availableInPos: Boolean = true,
    val canBePurchased: Boolean = true,
    val imageUri: String? = null,
    val saleTaxId: Long? = null, // Added for tax mapping
    val purchaseTaxId: Long? = null // Added for tax mapping
)