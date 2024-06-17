package com.sst.data.model.request

import com.google.gson.annotations.SerializedName

data class StartStream(
    @SerializedName("id_device")
    val idDevice: String,
    val lat: Double,
    val long: Double,
    @SerializedName("inference_model")
    val inferenceModel: String,
    @SerializedName("analytics_config")
    val analyticsConfig: String
)
