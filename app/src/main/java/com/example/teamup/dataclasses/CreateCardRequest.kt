package com.example.teamup.dataclasses

data class CreateCardRequest(
    val title: String,
    val description: String?,
    val image: String?,
    val board_id: Int
)