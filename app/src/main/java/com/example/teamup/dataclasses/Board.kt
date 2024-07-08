package com.example.teamup.dataclasses

data class Board(
    val cards: List<Card>,
    val id: Int,
    val lastPosition: Int,
    val title: String,
    val workspaceId: Int
)