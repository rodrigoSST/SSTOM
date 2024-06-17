package com.sst.data.model.response

import com.google.gson.annotations.SerializedName

data class DeviceDataResponse(
    val deviceID: String,
    @SerializedName("device_model")
    val deviceModel: String,
    @SerializedName("device_name")
    val deviceName: String,
    @SerializedName("device_type")
    val deviceType: String,
    val email: String,
    @SerializedName("id_device")
    val idDevice: String,
    @SerializedName("ip_receive")
    val ipReceive: String,
    @SerializedName("ip_transmit")
    val ipTransmit: String,
    @SerializedName("passphrase_receive")
    val passphraseReceive: String,
    @SerializedName("passphrase_transmit")
    val passphraseTransmit: String,
    val password: String,
    @SerializedName("port_receive")
    val portReceive: Int,
    @SerializedName("port_transmit")
    val portTransmit: Int,
    @SerializedName("streamid")
    val streamId: String,
    val lat: Double,
    val long: Double,
    val status: String,
    @SerializedName("inference_model")
    val inferenceModel: String? = "eco",
    @SerializedName("analytics_config")
    val analyticsConfig: String? = "config_none"
)