package com.sst.data.model.repository

import com.sst.data.model.api.SstPlayApi
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