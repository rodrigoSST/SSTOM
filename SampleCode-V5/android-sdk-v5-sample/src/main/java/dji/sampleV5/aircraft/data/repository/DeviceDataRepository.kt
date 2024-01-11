package dji.sampleV5.aircraft.data.repository

import dji.sampleV5.aircraft.api.DeviceStreamAPI
import dji.sampleV5.aircraft.model.DeviceData
import dji.sampleV5.aircraft.model.DeviceDataResponse

interface DeviceDataRepository {
    suspend fun setDeviceData(deviceData: DeviceData): DeviceDataResponse
}
class DeviceDataRepositoryImpl(
    private val retrofit: DeviceStreamAPI
): DeviceDataRepository {
    override suspend fun setDeviceData(deviceData: DeviceData): DeviceDataResponse =
        retrofit.setDeviceData(deviceData)
}