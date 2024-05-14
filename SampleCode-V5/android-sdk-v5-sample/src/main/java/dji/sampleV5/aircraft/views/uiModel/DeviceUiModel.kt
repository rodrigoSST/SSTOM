package dji.sampleV5.aircraft.views.uiModel

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeviceUiModel(
    val id: String,
    val deviceId: String,
    val model: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val streamId: String,
    val urlTransmit: String,
    val urlReceive: String,
    @DrawableRes val image: Int,
    val address: String
): Parcelable