package com.example.teamup.network

import com.example.teamup.dataclasses.CreateSubtaskRequest
import com.example.teamup.dataclasses.EditSubtaskRequest
import com.example.teamup.dataclasses.MessageResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SubtaskApi {
    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @POST("/api/v1/subtask")
    fun createSubtask(
        @Header("Authorization") accessToken: String,
        @Body request: CreateSubtaskRequest
    ): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("/api/v1/subtask/{id}")
    fun editSubtask(
        @Header("Authorization") accessToken: String,
        @Path("id") id: Int,
        @Body request: EditSubtaskRequest
    ): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @DELETE("/api/v1/subtask")
    fun deleteSubtask(
        @Header("Authorization") accessToken: String,
        @Path("id") id: Int
    ): Call<Void>
}