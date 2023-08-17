package com.example.teamup.interfaces

import com.example.teamup.dataclasses.CreateBoardRequest
import com.example.teamup.dataclasses.EditBoardRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface BoardApi {
    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @POST("/api/v1/board")
    fun createBoard(@Header("Authorization") accessToken: String, @Body request: CreateBoardRequest): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("/api/v1/board/{id}")
    fun updateBoard(@Header("Authorization") accessToken: String, @Path("id") id: Int, @Body request: EditBoardRequest): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @DELETE("/api/v1/board/{id}")
    fun deleteBoard(@Header("Authorization") accessToken: String, @Path("id") id: Int): Call<Void>
}