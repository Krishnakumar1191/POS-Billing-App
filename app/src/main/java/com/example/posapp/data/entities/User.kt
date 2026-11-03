package com.example.posapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mobileNumber: String,
    val email: String,
    val name: String,
    val passwordHash: String,
    val businessName: String,
    val companyName: String,
    val createDate: Long
)

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val registeredBusinessName: String,
    val businessName: String,
    val mobileNumber: String,
    val email: String,
    val address: String
)