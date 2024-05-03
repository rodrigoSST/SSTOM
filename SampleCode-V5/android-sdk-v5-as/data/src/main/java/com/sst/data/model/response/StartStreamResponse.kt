package com.sst.data.model.response

import com.google.gson.annotations.SerializedName

data class StartStreamResponse(
    @SerializedName("device_data")
    val deviceData: DeviceDataResponse,
    val response: String
)
