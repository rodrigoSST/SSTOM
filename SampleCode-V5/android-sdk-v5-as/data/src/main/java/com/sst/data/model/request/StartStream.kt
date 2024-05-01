package com.sst.data.model.request

import com.google.gson.annotations.SerializedName

data class StartStream(
    @SerializedName("id_device")
    val idDevice: String,
    val lat: Double,
    val long: Double,
)
