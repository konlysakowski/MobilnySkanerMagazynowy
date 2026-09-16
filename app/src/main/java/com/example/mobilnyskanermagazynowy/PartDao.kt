package com.example.mobilnyskanermagazynowy

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PartDao {
    @Query("SELECT * FROM parts_table ORDER BY name ASC")
    fun getAllParts(): Flow<List<PartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPart(part: PartItem) : Long

    @Query("UPDATE parts_table SET quantity = :newQty WHERE code = :partCode")
    suspend fun updateQuantity(partCode: String, newQty: Int) : Int

    @Query("SELECT * FROM parts_table WHERE code = :code LIMIT 1")
    suspend fun getPartByCode(code: String): PartItem?

    @Delete
    suspend fun deletePart(part: PartItem) : Int
}