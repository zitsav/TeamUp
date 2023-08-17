package com.example.teamup.dataclasses

import com.google.gson.annotations.SerializedName

data class AuthTokenResponse(
    @SerializedName("name") val name: String,
    @SerializedName("profile") val profile: String?,
    @SerializedName("token") val token: String
)