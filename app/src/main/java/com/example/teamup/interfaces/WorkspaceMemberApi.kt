package com.example.teamup.interfaces

import com.example.teamup.dataclasses.WorkspaceMemberRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Headers
import retrofit2.http.POST

interface WorkspaceMemberApi {
    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @POST("/addWorkspaceMember")
    fun addWorkspaceMember(@Body request: WorkspaceMemberRequest): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @DELETE("/removeWorkspaceMember")
    fun removeWorkspaceMember(@Body request: WorkspaceMemberRequest): Call<Void>
}