package com.example.teamup.network

import com.example.teamup.dataclasses.MessageResponse
import com.example.teamup.dataclasses.SearchUserRequest
import com.example.teamup.dataclasses.SearchUserResponse
import com.example.teamup.dataclasses.UpdateUserRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {
    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("/{id}")
    fun updateUser(
        @Header("Authorization") accessToken: String,
        @Path("id") id: Int,
        @Body request: UpdateUserRequest
    ): Call<MessageResponse>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("/search")
    fun searchUser(
        @Header("Authorization") accessToken: String,
        @Body request: SearchUserRequest
    ): Call<SearchUserResponse>
}