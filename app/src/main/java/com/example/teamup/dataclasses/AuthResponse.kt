package com.example.teamup.dataclasses

data class AuthResponse(
    val email: String,
    val fcmToken: String?,
    val id: Int,
    val name: String,
    val profile: String?,
    val token: String
)