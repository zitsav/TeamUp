package com.example.teamup.interfaces

import com.example.teamup.dataclasses.AuthTokenResponse
import com.example.teamup.dataclasses.LoginRequest
import com.example.teamup.dataclasses.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @Headers("Accept: application/json")
    @POST("api/v1/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthTokenResponse>

    @Headers("Accept: application/json")
    @POST("api/v1/auth/register")
    fun register(@Body request: RegisterRequest): Call<Void>
}