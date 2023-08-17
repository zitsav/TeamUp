package com.example.teamup.dataclasses

data class Workspace(
    val boards: List<Board>,
    val createdAt: String,
    val id: Int,
    val members: List<Member>,
    val title: String,
    val updatedAt: String
)