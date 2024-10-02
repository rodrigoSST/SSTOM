package com.sst.data.api

import com.sst.data.model.request.LoginRequest
import com.sst.data.model.request.StartStream
import com.sst.data.model.response.DeviceDataResponse
import com.sst.data.model.response.LoginResponse
import com.sst.data.model.response.Response
import com.sst.data.model.response.StartStreamResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SstPlayApi {
    @POST("/api/login")
    suspend fun doLogin(
        @Body login: LoginRequest
    ): LoginResponse

    @POST("/api/start_stream")
    suspend fun startStream(
        @Body startStream: StartStream
    ): StartStreamResponse

    @GET("/api/disconnect_device/{deviceId}")
    suspend fun disconnectDevice(
        @Path("deviceId") deviceId: String,
    ): Response

    @GET("/api/get_devices/{userId}")
    suspend fun getDevices(
        @Path("userId") userId: String
    ): List<DeviceDataResponse>

    @GET("/api/models")
    suspend fun getInferenceModels(): List<String>
}