package com.example.teamup.interfaces

import com.example.teamup.dataclasses.CreateListRequest
import com.example.teamup.dataclasses.UpdateListRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ListApi {
    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @POST("/api/v1/list")
    fun createList(@Header("Authorization") accessToken: String, @Body request: CreateListRequest): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("/api/v1/list/{id}")
    fun updateList(@Header("Authorization") accessToken: String, @Path("id") id: Int, @Body request: UpdateListRequest): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @DELETE("/api/v1/list/{id}")
    fun deleteList(@Header("Authorization") accessToken: String, @Path("id") id: Int): Call<Void>
}