package com.example.posapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val timestamp: Long,
    val totalAmount: Double,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val isSynced: Boolean = false
)

@Entity(
    tableName = "order_lines",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("orderId"), Index("productId")]
)
data class OrderLine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val quantity: Double,
    val unitPrice: Double,
    val lineTotal: Double
)