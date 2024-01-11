package dji.sampleV5.aircraft.api

import dji.sampleV5.aircraft.model.DeviceData
import dji.sampleV5.aircraft.model.DeviceDataResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface DeviceStreamAPI {
    @POST("device_data")
    suspend fun setDeviceData(
        @Body deviceData: DeviceData
    ): DeviceDataResponse
}