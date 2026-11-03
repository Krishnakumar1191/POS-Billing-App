package com.example.posapp.data.dao

import androidx.room.*
import com.example.posapp.data.entities.Tax
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxDao {
    @Query("SELECT * FROM local_taxes ORDER BY id DESC")
    fun getAllTaxes(): Flow<List<Tax>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTax(tax: Tax): Long

    @Update
    suspend fun updateTax(tax: Tax)

    @Query("DELETE FROM local_taxes WHERE id = :id")
    suspend fun deleteTax(id: Long)

    @Query("SELECT * FROM local_taxes WHERE id = :id LIMIT 1")
    suspend fun getTaxById(id: Long): Tax?
}