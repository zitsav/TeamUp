package com.example.teamup.dataclasses

data class RegisterRequest(
    val name: String,
    val profile: String?,
    val email: String,
    val password: String
)