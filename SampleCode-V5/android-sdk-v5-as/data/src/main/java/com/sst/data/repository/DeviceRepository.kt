package com.sst.data.repository

import com.sst.data.api.SstPlayApi
import com.sst.data.model.response.DeviceDataResponse

interface DeviceRepository {
    suspend fun getDevices(userId: String): List<DeviceDataResponse>
}

class DeviceRepositoryImpl(
    private val sstPlayApi: SstPlayApi
): DeviceRepository {
    override suspend fun getDevices(userId: String): List<DeviceDataResponse> =
        sstPlayApi.getDevices(userId)

}