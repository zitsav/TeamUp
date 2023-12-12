package com.example.teamup.dataclasses

data class Board(
    var cards: List<Card>,
    val createdAt: String,
    val description: String,
    val id: Int,
    val title: String,
    val updatedAt: String,
    val workspaceId: Int
)