package com.example.teamup.interfaces

import com.example.teamup.dataclasses.UpdateUserRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {
    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("/updateUser/{id}")
    fun updateUser(@Path("id") id: Int, @Body request: UpdateUserRequest): Call<Void>
}