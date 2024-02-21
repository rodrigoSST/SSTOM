package dji.sampleV5.aircraft.model

data class DeviceStreamRequest(
    val userId: String,
    val udpPort: String,
    val srtPort: String,
    val device: String = "Drone"
)