package com.sst.data.model.response

import com.google.gson.annotations.SerializedName

data class UserDataResponse(
    @SerializedName("access_level")
    val accessLevel: Int,
    val email: String,
    @SerializedName("id_user")
    val idUser: String,
    val password: String,
    @SerializedName("user_name")
    val userName: String
)