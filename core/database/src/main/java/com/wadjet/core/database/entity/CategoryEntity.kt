package com.wadjet.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val code: String,
    val name: String,
    val count: Int = 0,
    val cachedAt: Long = System.currentTimeMillis(),
)
