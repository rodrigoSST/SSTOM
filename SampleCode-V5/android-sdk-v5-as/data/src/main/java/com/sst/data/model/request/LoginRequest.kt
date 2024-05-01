package com.sst.data.model.request

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("deviceID")
    val deviceId: String,
    @SerializedName("device_model")
    val model: String,
    @SerializedName("device_type")
    val type: String
)