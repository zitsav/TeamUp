package com.example.teamup.interfaces

import com.example.teamup.dataclasses.CreateCardRequest
import com.example.teamup.dataclasses.UpdateCardRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CardApi {
    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @POST("/api/v1/card")
    fun createCard(@Header("Authorization") accessToken: String, @Body request: CreateCardRequest): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("/api/v1/card/{id}")
    fun updateCard(@Header("Authorization") accessToken: String, @Path("id") id: Int, @Body request: UpdateCardRequest): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @DELETE("/api/v1/card/{id}")
    fun deleteCard(@Header("Authorization") accessToken: String, @Path("id") id: Int): Call<Void>
}