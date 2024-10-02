package com.sst.data.repository

import com.sst.data.api.SstPlayApi
import com.sst.data.model.request.StartStream
import com.sst.data.model.response.Response
import com.sst.data.model.response.StartStreamResponse


interface StreamRepository {
    suspend fun startStream(startStream: StartStream): StartStreamResponse
    suspend fun disconnectDevice(deviceId: String): Response
    suspend fun getInferenceModels(): List<String>
}

class StreamRepositoryImpl(
    private val sstPlayApi: SstPlayApi
): StreamRepository {
    override suspend fun startStream(startStream: StartStream) =
        sstPlayApi.startStream(startStream)


    override suspend fun disconnectDevice(deviceId: String) =
        sstPlayApi.disconnectDevice(deviceId)

    override suspend fun getInferenceModels(): List<String> =
        sstPlayApi.getInferenceModels()
}