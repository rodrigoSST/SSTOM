package dji.sampleV5.aircraft.comom.mapper

import com.sst.data.model.response.DeviceDataResponse
import dji.sampleV5.aircraft.views.uiModel.DeviceUiModel

fun DeviceDataResponse.toUiModel() = DeviceUiModel(
    id = idDevice,
    deviceId = deviceID,
    model = deviceModel ?: "",
    type = deviceType ?: "",
    latitude = lat,
    longitude = long,
    status = status ?: "",
    image = 0,
    address = "",
    streamId = streamId,
    urlTransmit = urlTransmit(this.ipTransmit, this.portTransmit.toString()),
    urlReceive = urlReceive(this.ipReceive, this.portReceive.toString(), this.streamId)
)

fun urlTransmit(
    ipTransmit: String, portTransmit: String
): String {
    return "srt://${ipTransmit}:${portTransmit}"
}

fun urlReceive(
    ipReceive: String, portReceive: String, streamId: String
): String {
    return "srt://${ipReceive}:${portReceive}?streamid=${streamId}"
}