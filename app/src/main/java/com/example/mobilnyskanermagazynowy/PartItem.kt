package com.example.mobilnyskanermagazynowy

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parts_table")
data class PartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val name: String,
    val quantity: Int,
    val location: String
)