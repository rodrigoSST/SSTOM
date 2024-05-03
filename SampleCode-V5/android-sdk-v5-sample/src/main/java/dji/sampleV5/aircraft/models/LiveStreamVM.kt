package dji.sampleV5.aircraft.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sst.data.model.repository.StreamRepository
import com.sst.data.model.request.StartStream
import dji.sampleV5.aircraft.comom.mapper.toUiModel
import dji.sampleV5.aircraft.model.DeviceDataResponse
import dji.sampleV5.aircraft.views.uiModel.DeviceUiModel
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.common.utils.CallbackUtils
import dji.v5.common.video.channel.VideoChannelType
import dji.v5.et.create
import dji.v5.et.get
import dji.v5.manager.datacenter.MediaDataCenter
import dji.v5.manager.interfaces.ILiveStreamManager
import dji.v5.manager.datacenter.livestream.*
import dji.v5.manager.datacenter.livestream.settings.AgoraSettings
import dji.v5.manager.datacenter.livestream.settings.GB28181Settings
import dji.v5.manager.datacenter.livestream.settings.RtmpSettings
import dji.v5.manager.datacenter.livestream.settings.RtspSettings
import dji.v5.manager.interfaces.ICameraStreamManager
import dji.v5.utils.common.ContextUtil
import dji.v5.utils.common.DjiSharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ClassName : LiveStreamVM
 * Description : 直播VM
 * Author : daniel.chen
 * CreateDate : 2022/3/23 11:04 上午
 * Copyright : ©2022 DJI All Rights Reserved.
 */
class LiveStreamVM(
    private val streamRepository: StreamRepository
) : DJIViewModel() {
    private val availableCameraUpdatedListener: ICameraStreamManager.AvailableCameraUpdatedListener
    private val liveStreamStatusListener: LiveStreamStatusListener
    private val RTMP_KEY = "livestream-rtmp"
    private val RTSP_KEY = "livestream-rtsp"
    private val GB28181_KEY = "livestream-gb28181"
    private val AGORA_KEY = "livestream-agora"
    val liveStreamStatus = MutableLiveData<LiveStreamStatus?>()
    val liveStreamError = MutableLiveData<IDJIError?>()
    val availableCameraList = MutableLiveData<List<ComponentIndexType>>()
    val streamManager: ILiveStreamManager = MediaDataCenter.getInstance().liveStreamManager
    val cameraManager: ICameraStreamManager = MediaDataCenter.getInstance().cameraStreamManager

    private var startTimeFrame = 0L
    private var counter = 0
    private var kbps = 0.0

    private val _info: MutableLiveData<String> = MutableLiveData()
    val info: LiveData<String> = _info
    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error

    private var repeatJob: Job? = null

    private val _device = MutableLiveData<DeviceUiModel>()
    val device: LiveData<DeviceUiModel> = _device

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    init {
        liveStreamStatusListener = object : LiveStreamStatusListener {
            override fun onLiveStreamStatusUpdate(status: LiveStreamStatus?) {
                status?.let {
                    liveStreamStatus.postValue(it)
                }
            }

            override fun onError(error: IDJIError?) {
                error?.let {
                    liveStreamError.postValue(it)
                }
            }
        }

        availableCameraUpdatedListener = ICameraStreamManager.AvailableCameraUpdatedListener { list ->
            availableCameraList.postValue(list)
        }

        addListener()
    }


    fun startStream(startStream: StartStream) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            try {
                _device.postValue(streamRepository.startStream(startStream).deviceData.toUiModel())
                _loading.postValue(false)
            } catch (e: Exception) {
                _error.postValue(e.message)
                _loading.postValue(false)
            }
        }
    }

    fun disconnectDevice(idDevice: String) {
        viewModelScope.launch(Dispatchers.IO) {
            streamRepository.disconnectDevice(idDevice)
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeListener()
    }

    private fun reset() {
        liveStreamError.postValue(null)
        liveStreamStatus.postValue(null)
    }

    fun startStream(callback: CommonCallbacks.CompletionCallback?) {
        streamManager.startStream(object : CommonCallbacks.CompletionCallback {
            override fun onSuccess() {
                CallbackUtils.onSuccess(callback)
                reset();
            }

            override fun onFailure(error: IDJIError) {
                CallbackUtils.onFailure(callback, error)
            }

        })
    }

    fun stopStream(callback: CommonCallbacks.CompletionCallback?) {
        streamManager.stopStream(object : CommonCallbacks.CompletionCallback {
            override fun onSuccess() {
                CallbackUtils.onSuccess(callback)
                reset();
            }

            override fun onFailure(error: IDJIError) {
                CallbackUtils.onFailure(callback, error)
            }

        })
    }

    fun isStreaming(): Boolean {
        return streamManager.isStreaming;
    }

    fun setCameraIndex(cameraIndex: ComponentIndexType) {
        streamManager.cameraIndex = cameraIndex
    }

    fun setLiveStreamConfig(liveStreamSettings: LiveStreamSettings) {
        streamManager.liveStreamSettings = liveStreamSettings;
    }

    fun setLiveStreamQuality(liveStreamQuality: StreamQuality) {
        streamManager.liveStreamQuality = liveStreamQuality
    }

    fun setLiveVideoBitRateMode(bitRateMode: LiveVideoBitrateMode) {
        streamManager.liveVideoBitrateMode = bitRateMode
    }

    fun setLiveVideoBitRate(bitrate: Int) {
        streamManager.liveVideoBitrate = bitrate
    }

    fun getVideoChannel(): VideoChannelType {
        return streamManager.videoChannelType
    }

    fun getLiveStreamQuality():StreamQuality{
        return streamManager.liveStreamQuality
    }

    fun getLiveStreamBitRateModes(): Array<LiveVideoBitrateMode> {
        return listOf(LiveVideoBitrateMode.AUTO, LiveVideoBitrateMode.MANUAL).toTypedArray()
    }

    fun getLiveVideoBitRateMode():LiveVideoBitrateMode{
        return streamManager.liveVideoBitrateMode
    }

    fun getLiveStreamQualities(): Array<StreamQuality> {
        return listOf(
            StreamQuality.FULL_HD,
            StreamQuality.HD,
            StreamQuality.SD
        ).toTypedArray()
    }

    fun setRTMPConfig(rtmpUrl: String) {
        val liveStreamConfig = LiveStreamSettings.Builder()
            .setLiveStreamType(LiveStreamType.RTMP)
            .setRtmpSettings(
                RtmpSettings.Builder()
                    .setUrl(rtmpUrl)
                    .build()
            )
            .build()
        DjiSharedPreferencesManager.putString(ContextUtil.getContext(), RTMP_KEY, rtmpUrl)
        setLiveStreamConfig(liveStreamConfig)
    }

    fun getRtmpUrl(): String {
        return DjiSharedPreferencesManager.getString(ContextUtil.getContext(), RTMP_KEY, "")
    }

    fun setRTSPConfig(userName: String, password: String, port: Int) {
        val rtspConfig = RtspSettings.Builder()
            .setUserName(userName)
            .setPassWord(password)
            .setPort(port)
            .build()
        val liveStreamConfig = LiveStreamSettings.Builder()
            .setLiveStreamType(LiveStreamType.RTSP)
            .setRtspSettings(rtspConfig)
            .build()
        val rtspSettings = userName + "^_^" + password + "^_^" + port.toString()
        DjiSharedPreferencesManager.putString(ContextUtil.getContext(), RTSP_KEY, rtspSettings)
        setLiveStreamConfig(liveStreamConfig)
    }

    fun getRtspSettings(): String {
        return DjiSharedPreferencesManager.getString(ContextUtil.getContext(), RTSP_KEY, "")
    }

    fun setGB28181(
        serverIP: String,
        serverPort: Int,
        serverID: String,
        agentID: String,
        channel: String,
        localPort: Int,
        password: String
    ) {
        val gb28181Config = GB28181Settings.Builder()
            .setServerIP(serverIP)
            .setServerPort(serverPort)
            .setServerID(serverID)
            .setAgentID(agentID)
            .setChannel(channel)
            .setLocalPort(localPort)
            .setPassword(password)
            .build()
        val liveStreamConfig = LiveStreamSettings.Builder()
            .setLiveStreamType(LiveStreamType.GB28181)
            .setGB28181Settings(gb28181Config)
            .build()
        val gb28181Settings =
            serverIP + "^_^" + serverPort.toString() + "^_^" + serverID + "^_^" + agentID + "^_^" + channel + "^_^" + localPort.toString() + "^_^" + password
        DjiSharedPreferencesManager.putString(
            ContextUtil.getContext(),
            GB28181_KEY,
            gb28181Settings
        )
        setLiveStreamConfig(liveStreamConfig)
    }

    fun getGb28181Settings(): String {
        return DjiSharedPreferencesManager.getString(ContextUtil.getContext(), GB28181_KEY, "")
    }

    fun setAgoraConfig(channelId: String, token: String, uid: String) {
        val agoraConfig = AgoraSettings.Builder()
            .setChannelId(channelId)
            .setToken(token)
            .setUid(uid)
            .setEnableSafety(false)
            .build()
        val liveStreamConfig = LiveStreamSettings.Builder()
            .setLiveStreamType(LiveStreamType.AGORA)
            .setAgoraSettings(agoraConfig)
            .build()
        val agoraSettings = channelId + "^_^" + token + "^_^" + uid
        DjiSharedPreferencesManager.putString(
            ContextUtil.getContext(),
            AGORA_KEY,
            agoraSettings
        )
        setLiveStreamConfig(liveStreamConfig)
    }

    fun getAgoraSettings(): String {
        return DjiSharedPreferencesManager.getString(ContextUtil.getContext(), AGORA_KEY, "")
    }

    fun addListener() {
        streamManager.addLiveStreamStatusListener(liveStreamStatusListener)
        cameraManager.addAvailableCameraUpdatedListener(availableCameraUpdatedListener)
    }

    fun removeListener() {
        streamManager.removeLiveStreamStatusListener(liveStreamStatusListener)
        cameraManager.removeAvailableCameraUpdatedListener(availableCameraUpdatedListener)
    }

    fun getFps(biteArray: ByteArray) {
        if (startTimeFrame == 0L) {
            startTimeFrame = System.currentTimeMillis()
            counter++
        } else {
            val difference: Long = System.currentTimeMillis() - startTimeFrame

            val seconds = difference / 1000.0

            if(seconds >= 1) {
                _info.postValue("$counter fps\n${String.format("%.2f", kbps)} kbps")
                counter = 0
                kbps = 0.0
                startTimeFrame = System.currentTimeMillis()
            }else{
                counter++
                kbps =+ getByteArraySize(biteArray)
            }
        }
        Log.i("FrameByFrame", "$counter fps | ${String.format("%.2f", kbps)} kbps")
    }

    private fun getByteArraySize(biteArray: ByteArray): Double {
        return biteArray.size / 1024.0
    }

    fun getLiveStreamTypes(): Array<LiveStreamType> {
        return listOf(
            LiveStreamType.RTMP,
            LiveStreamType.RTSP,
            LiveStreamType.GB28181,
            LiveStreamType.AGORA
        ).toTypedArray()
    }

    private fun sendLocationToServer(): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            while (NonCancellable.isActive) {
                val location = getAircraftLocation()
                /*val locationJson = Gson().toJson(
                    DeviceLocation(
                        location?.latitude,
                        location?.longitude
                    )
                )
                Log.i("FrameByFrame", "latitude:  ${location?.latitude} | longitude: ${location?.longitude}")
                publishMessage(RABBITMQ_QUEUE_LOCATION_NAME, locationJson.toByteArray())*/

                delay(5000L)
            }
        }
    }

    private fun cancelSendLocationToServer() {
        repeatJob?.cancel()
    }

    fun getAircraftLocation() = FlightControllerKey.KeyAircraftLocation3D.create().get()
}