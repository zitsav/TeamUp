package com.example.teamup.interfaces

import com.example.teamup.dataclasses.CreateCardRequest
import com.example.teamup.dataclasses.UpdateCardRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
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
    @POST("/createCard")
    fun createCard(@Body request: CreateCardRequest): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("/updateCard/{id}")
    fun updateCard(@Path("id") id: Int, @Body request: UpdateCardRequest): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @DELETE("/deleteCard/{id}")
    fun deleteCard(@Path("id") id: Int): Call<Void>
}