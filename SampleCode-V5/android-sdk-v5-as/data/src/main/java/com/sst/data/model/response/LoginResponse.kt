package com.sst.data.model.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val device: DeviceDataResponse,
    val response: String,
    @SerializedName("user_data")
    val userData: UserDataResponse
)
