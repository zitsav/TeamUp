package com.example.teamup.dataclasses

data class Card(
    val boardId: Int,
    val createdAt: String,
    val description: String,
    val id: Int,
    val lists: List<Lists>,
    val title: String,
    val updatedAt: String
)