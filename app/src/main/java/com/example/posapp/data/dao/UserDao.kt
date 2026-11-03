package com.example.posapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.posapp.data.entities.User
import com.example.posapp.data.entities.BusinessProfile

@Dao
interface UserDao {
    // Added 'name' to allow logging in with the Username as well as Email/Phone
    @Query("SELECT * FROM local_users WHERE (email = :identifier OR mobileNumber = :identifier OR name = :identifier) AND passwordHash = :passwordHash")
    suspend fun loginUser(identifier: String, passwordHash: String): User?

    @Query("SELECT * FROM local_users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    @Insert
    suspend fun registerUser(user: User): Long

    @Insert
    suspend fun insertBusinessProfile(profile: BusinessProfile): Long
}