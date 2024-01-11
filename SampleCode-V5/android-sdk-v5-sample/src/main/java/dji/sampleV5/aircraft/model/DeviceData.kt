package dji.sampleV5.aircraft.model

data class DeviceData(
    val deviceId: String,
    val device: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
