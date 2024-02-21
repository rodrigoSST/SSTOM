package dji.sampleV5.aircraft.api

import dji.sampleV5.aircraft.model.DeviceData
import dji.sampleV5.aircraft.model.DeviceDataResponse
import dji.sampleV5.aircraft.model.DeviceStreamRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface DeviceStreamAPI {
    @POST("connect_server")
    suspend fun connectServer(
        @Body deviceData: DeviceData
    ): DeviceDataResponse

    @POST("disconnect_server")
    suspend fun disconnectServer(
        @Body deviceStreamRequest: DeviceStreamRequest
    ): DeviceDataResponse

    @POST("device_data")
    suspend fun setDeviceData(
        @Body deviceData: DeviceData
    ): DeviceDataResponse

    @POST("device_stream")
    suspend fun deviceStream(
        @Body deviceStreamRequest: DeviceStreamRequest
    )
}