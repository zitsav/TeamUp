package com.example.teamup.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_tokens")
data class AuthToken(
    @PrimaryKey val id: Int,
    val token: String
)