package com.example.teamup.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int,
    val email: String,
    val name: String,
    val profile: String?
)