package com.example.teamup.dataclasses

data class Workspace(
    val boards: List<Board>,
    val icon: String?,
    val id: Int,
    val members: List<Member>,
    val title: String
)