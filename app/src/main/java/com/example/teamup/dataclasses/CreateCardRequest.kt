package com.example.teamup.dataclasses

data class CreateCardRequest(
    val board_id: Int,
    val description: String,
    val title: String
)