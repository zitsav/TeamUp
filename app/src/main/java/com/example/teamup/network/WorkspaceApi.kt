package com.example.teamup.network

import com.example.teamup.dataclasses.AddWorkspaceUserRequest
import com.example.teamup.dataclasses.CreateWorkspaceRequest
import com.example.teamup.dataclasses.EditWorkspaceRequest
import com.example.teamup.dataclasses.GetAllWorkspaceResponse
import com.example.teamup.dataclasses.GetSingleWorkspaceResponse
import com.example.teamup.dataclasses.MessageResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface WorkspaceApi {
    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @POST("/api/v1/workspace")
    fun createWorkspace(
        @Header("Authorization") accessToken: String,
        @Body request: CreateWorkspaceRequest
    ): Call<MessageResponse>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @GET("/api/v1/workspace")
    fun getAllWorkspaces(
        @Header("Authorization") accessToken: String
    ): Call<GetAllWorkspaceResponse>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @GET("/api/v1/workspace/{id}")
    fun getWorkspaceById(
        @Header("Authorization") accessToken: String,
        @Path("id") id: Int
    ): Call<GetSingleWorkspaceResponse>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("/api/v1/workspace/{id}")
    fun editWorkspace(
        @Header("Authorization") accessToken: String,
        @Path("id") id: Int,
        @Body request: EditWorkspaceRequest
    ): Call<MessageResponse>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @DELETE("/api/v1/workspace/{id}")
    fun removeWorkspace(
        @Header("Authorization") accessToken: String,
        @Path("id") id: Int
    ): Call<MessageResponse>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @POST("/user")
    fun addWorkspaceMember(
        @Header("Authorization") accessToken: String,
        @Body request: AddWorkspaceUserRequest
    ): Call<MessageResponse>
}