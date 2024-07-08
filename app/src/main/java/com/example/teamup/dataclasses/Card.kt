package com.example.teamup.dataclasses

data class Card(
    val assignedUsers: List<AssignedUser>,
    val boardId: Int,
    val color: String,
    val deadline: String?,
    val description: String?,
    val id: Int,
    val image: String?,
    val position: Int,
    val subtasks: List<Subtask>,
    val title: String
)