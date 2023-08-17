package com.example.teamup.dataclasses

data class CreateBoardRequest(
    val description: String,
    val title: String,
    val workspaceId: Int
)