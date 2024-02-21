package dji.sampleV5.aircraft.data.repository

import dji.sampleV5.aircraft.api.DeviceStreamAPI
import dji.sampleV5.aircraft.model.DeviceData
import dji.sampleV5.aircraft.model.DeviceDataResponse
import dji.sampleV5.aircraft.model.DeviceStreamRequest

interface DeviceDataRepository {
    suspend fun connectServer(deviceData: DeviceData): DeviceDataResponse
    suspend fun disconnectServer(deviceStreamRequest: DeviceStreamRequest)
    suspend fun setDeviceData(deviceData: DeviceData): DeviceDataResponse
    suspend fun deviceStream(deviceStreamRequest: DeviceStreamRequest)
}
class DeviceDataRepositoryImpl(
    private val retrofit: DeviceStreamAPI
): DeviceDataRepository {
    override suspend fun connectServer(deviceData: DeviceData): DeviceDataResponse =
        retrofit.connectServer(deviceData)

    override suspend fun disconnectServer(deviceStreamRequest: DeviceStreamRequest) {
        retrofit.disconnectServer(deviceStreamRequest)
    }

    override suspend fun setDeviceData(deviceData: DeviceData): DeviceDataResponse =
        retrofit.setDeviceData(deviceData)

    override suspend fun deviceStream(deviceStreamRequest: DeviceStreamRequest) {
        retrofit.deviceStream(deviceStreamRequest)
    }
}