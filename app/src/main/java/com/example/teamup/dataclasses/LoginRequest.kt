package com.example.teamup.dataclasses

data class LoginRequest(
    val email: String,
    val password: String,
    val fcmToken: String?
)