package com.sst.data.model.repository

import com.sst.data.model.api.SstPlayApi
import com.sst.data.model.request.StartStream
import com.sst.data.model.response.DeviceDataResponse
import com.sst.data.model.response.Response


interface StreamRepository {
    suspend fun startStream(startStream: StartStream): DeviceDataResponse
    suspend fun disconnectDevice(deviceId: String): Response
}

class StreamRepositoryImpl(
    private val sstPlayApi: SstPlayApi
): StreamRepository {
    override suspend fun startStream(startStream: StartStream) =
        sstPlayApi.startStream(startStream)


    override suspend fun disconnectDevice(deviceId: String) =
        sstPlayApi.disconnectDevice(deviceId)

}