package dji.sampleV5.aircraft.views

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.media.MediaFormat
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sst.data.model.request.StartStream
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.databinding.FragmentLiveStreamingBinding
import dji.sampleV5.aircraft.enums.ViewType
import dji.sampleV5.aircraft.models.LiveStreamVM
import dji.sampleV5.aircraft.pages.DJIFragment
import dji.sampleV5.aircraft.srt.streamers.SurfaceSrtLiveStreamer
import dji.sampleV5.aircraft.util.ToastUtils
import dji.sdk.keyvalue.value.common.CameraLensType
import dji.v5.common.error.IDJIError
import dji.v5.common.video.channel.VideoChannelState
import dji.v5.common.video.channel.VideoChannelType
import dji.v5.common.video.interfaces.IVideoDecoder
import dji.v5.common.video.interfaces.VideoChannelStateChangeListener
import dji.v5.common.video.stream.PhysicalDevicePosition
import dji.v5.common.video.stream.StreamSource
import dji.v5.manager.datacenter.MediaDataCenter
import dji.v5.utils.common.JsonUtil
import dji.v5.utils.common.LogUtils
import dji.v5.ux.cameracore.widget.autoexposurelock.AutoExposureLockWidget
import dji.v5.ux.cameracore.widget.cameracontrols.CameraControlsWidget
import dji.v5.ux.cameracore.widget.cameracontrols.exposuresettings.ExposureSettingsPanel
import dji.v5.ux.cameracore.widget.cameracontrols.lenscontrol.LensControlWidget
import dji.v5.ux.cameracore.widget.focusexposureswitch.FocusExposureSwitchWidget
import dji.v5.ux.cameracore.widget.focusmode.FocusModeWidget
import dji.v5.ux.cameracore.widget.fpvinteraction.FPVInteractionWidget
import dji.v5.ux.core.base.SchedulerProvider.io
import dji.v5.ux.core.base.SchedulerProvider.ui
import dji.v5.ux.core.communication.BroadcastValues
import dji.v5.ux.core.communication.GlobalPreferenceKeys
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.core.communication.UXKeys
import dji.v5.ux.core.extension.hide
import dji.v5.ux.core.extension.show
import dji.v5.ux.core.extension.toggleVisibility
import dji.v5.ux.core.panel.systemstatus.SystemStatusListPanelWidget
import dji.v5.ux.core.panel.topbar.TopBarPanelWidget
import dji.v5.ux.core.util.CameraUtil
import dji.v5.ux.core.util.CommonUtils
import dji.v5.ux.core.util.DataProcessor
import dji.v5.ux.core.util.ViewUtil
import dji.v5.ux.core.widget.fpv.FPVStreamSourceListener
import dji.v5.ux.core.widget.fpv.FPVWidget
import dji.v5.ux.core.widget.hsi.HorizontalSituationIndicatorWidget
import dji.v5.ux.core.widget.hsi.PrimaryFlightDisplayWidget
import dji.v5.ux.core.widget.setting.SettingWidget
import dji.v5.ux.map.MapWidget
import dji.v5.ux.mapkit.core.maps.DJIMap
import dji.v5.ux.training.simulatorcontrol.SimulatorControlWidget
import dji.v5.ux.training.simulatorcontrol.SimulatorControlWidget.UIState.VisibilityUpdated
import dji.v5.ux.visualcamera.CameraNDVIPanelWidget
import dji.v5.ux.visualcamera.CameraVisiblePanelWidget
import dji.v5.ux.visualcamera.zoom.FocalZoomWidget
import io.github.thibaultbee.streampack.data.VideoConfig
import io.github.thibaultbee.streampack.error.StreamPackError
import io.github.thibaultbee.streampack.listeners.OnConnectionListener
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import io.github.thibaultbee.streampack.streamers.StreamerLifeCycleObserver
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.Consumer
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class LiveStreamingFragment : DJIFragment(), SurfaceHolder.Callback {

    private var _binding: FragmentLiveStreamingBinding? = null
    private val binding
        get() = _binding!!

    private val liveStreamVM: LiveStreamVM by viewModel()

    var fps: Int = -1
    var vbps: Int = -1
    var isStreaming: Boolean = false
    var resolution_w: Int = -1
    var resolution_h: Int = -1
    var packet_loss: Int = -1
    var packet_cache_len: Int = -1
    var rtt: Int = -1
    var error: IDJIError? = null

    var curWidth: Int = -1
    var curHeight: Int = -1

    private val TAG = LogUtils.getTag(this)
    private lateinit var primaryFpvWidget: FPVWidget
    private lateinit var fpvInteractionWidget: FPVInteractionWidget
    private lateinit var secondaryFPVWidget: FPVWidget
    private lateinit var systemStatusListPanelWidget: SystemStatusListPanelWidget
    private lateinit var simulatorControlWidget: SimulatorControlWidget
    private lateinit var lensControlWidget: LensControlWidget
    private lateinit var autoExposureLockWidget: AutoExposureLockWidget
    private lateinit var focusModeWidget: FocusModeWidget
    private lateinit var focusExposureSwitchWidget: FocusExposureSwitchWidget
    private lateinit var cameraControlsWidget: CameraControlsWidget
    private lateinit var horizontalSituationIndicatorWidget: HorizontalSituationIndicatorWidget
    private lateinit var exposureSettingsPanel: ExposureSettingsPanel
    private lateinit var pfvFlightDisplayWidget: PrimaryFlightDisplayWidget
    private lateinit var ndviCameraPanel: CameraNDVIPanelWidget
    private lateinit var visualCameraPanel: CameraVisiblePanelWidget
    private lateinit var focalZoomWidget: FocalZoomWidget
    private lateinit var mapWidget: MapWidget
    private lateinit var topBarPanel: TopBarPanelWidget
    private lateinit var fpvParentView: ConstraintLayout
    private lateinit var surfaceView: SurfaceView
    private lateinit var mDrawerLayout: DrawerLayout

    private var widgetWidth = 0
    private var widgetHeight = 0

    private val idDevice by lazy {
        arguments?.getString(EXTRA_ID_DEVICE)
    }

    private lateinit var inferenceModel: String

    private val analyticsConfig by lazy {
        arguments?.getString(EXTRA_ANALYTICS_CONFIG) ?: "config_none"
    }

    private lateinit var streamId: String

    private var compositeDisposable: CompositeDisposable? = null
    private val cameraSourceProcessor = DataProcessor.create(
        CameraSource(
            PhysicalDevicePosition.UNKNOWN,
            CameraLensType.UNKNOWN
        )
    )
    private var primaryChannelStateListener: VideoChannelStateChangeListener? = null
    private var secondaryChannelStateListener: VideoChannelStateChangeListener? = null

    private var videoDecoder: IVideoDecoder? = null

    private val errorListener = object : OnErrorListener {
        override fun onError(error: StreamPackError) {
            toast(String.format(getString(R.string.an_error_occurred), error))
            activity?.runOnUiThread {
                binding.contentLoading.isVisible = false
                binding.fbStartStop.setImageResource(R.drawable.ic_play)
                isStreaming = false
            }
        }
    }

    private val connectionListener = object : OnConnectionListener {
        override fun onFailed(message: String) {
            toast(String.format(getString(R.string.connection_failed), message))
            activity?.runOnUiThread {
                binding.contentLoading.isVisible = false
                binding.fbStartStop.setImageResource(R.drawable.ic_play)
                isStreaming = false
            }
        }

        override fun onLost(message: String) {
            toast(String.format(getString(R.string.connection_lost), message))
            activity?.runOnUiThread {
                binding.contentLoading.isVisible = false
                binding.fbStartStop.setImageResource(R.drawable.ic_play)
                isStreaming = false
            }
        }

        override fun onSuccess() {
            toast(getString(R.string.connected))
            activity?.runOnUiThread {
                binding.aiButton.isVisible = true
                binding.fbStartStop.setImageResource(R.drawable.ic_stop)
                isStreaming = true
            }
        }
    }

    private val streamer by lazy {
        SurfaceSrtLiveStreamer(
            requireContext(),
            initialOnErrorListener = errorListener,
            initialOnConnectionListener = connectionListener,
            cameraStreamManager = MediaDataCenter.getInstance().cameraStreamManager
        )
    }

    private val streamerLifeCycleObserver by lazy { StreamerLifeCycleObserver(streamer) }

    private var fullViewType = ViewType.FPV

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLiveStreamingBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setLayerType(View.LAYER_TYPE_NONE, null)

        fpvParentView = view.findViewById<ConstraintLayout>(R.id.fpv_holder)
        fpvParentView.setLayerType(View.LAYER_TYPE_NONE, null)
        topBarPanel = view.findViewById<TopBarPanelWidget>(R.id.panel_top_bar)
        primaryFpvWidget = view.findViewById<FPVWidget>(R.id.widget_primary_fpv)
        surfaceView = primaryFpvWidget.fpvSurfaceView
        surfaceView.holder.addCallback(this)
        fpvInteractionWidget = view.findViewById<FPVInteractionWidget>(R.id.widget_fpv_interaction)
        secondaryFPVWidget = view.findViewById<FPVWidget>(R.id.widget_secondary_fpv)
        systemStatusListPanelWidget =
            view.findViewById<SystemStatusListPanelWidget>(R.id.widget_panel_system_status_list)
        simulatorControlWidget =
            view.findViewById<SimulatorControlWidget>(R.id.widget_simulator_control)
        lensControlWidget = view.findViewById<LensControlWidget>(R.id.widget_lens_control)
        ndviCameraPanel = view.findViewById<CameraNDVIPanelWidget>(R.id.panel_ndvi_camera)
        visualCameraPanel = view.findViewById<CameraVisiblePanelWidget>(R.id.panel_visual_camera)
        autoExposureLockWidget =
            view.findViewById<AutoExposureLockWidget>(R.id.widget_auto_exposure_lock)
        focusModeWidget = view.findViewById<FocusModeWidget>(R.id.widget_focus_mode)
        focusExposureSwitchWidget =
            view.findViewById<FocusExposureSwitchWidget>(R.id.widget_focus_exposure_switch)
        exposureSettingsPanel =
            view.findViewById<ExposureSettingsPanel>(R.id.panel_camera_controls_exposure_settings)
        pfvFlightDisplayWidget =
            view.findViewById<PrimaryFlightDisplayWidget>(R.id.widget_fpv_flight_display_widget)
        focalZoomWidget = view.findViewById<FocalZoomWidget>(R.id.widget_focal_zoom)
        cameraControlsWidget = view.findViewById<CameraControlsWidget>(R.id.widget_camera_controls)
        horizontalSituationIndicatorWidget =
            view.findViewById<HorizontalSituationIndicatorWidget>(R.id.widget_horizontal_situation_indicator)

        mapWidget = view.findViewById<MapWidget>(dji.v5.ux.R.id.widget_map)
        cameraControlsWidget.exposureSettingsIndicatorWidget
            .setStateChangeResourceId(dji.v5.ux.R.id.panel_camera_controls_exposure_settings)
        mDrawerLayout = view.findViewById<DrawerLayout>(dji.v5.ux.R.id.root_view)

        MediaDataCenter.getInstance().videoStreamManager.addStreamSourcesListener { sources: List<StreamSource>? ->
            activity?.runOnUiThread { updateFPVWidgetSource(sources) }
        }
        primaryFpvWidget.setOnFPVStreamSourceListener(object : FPVStreamSourceListener {
            override fun onStreamSourceUpdated(
                devicePosition: PhysicalDevicePosition,
                lensType: CameraLensType
            ) {
                cameraSourceProcessor.onNext(
                    CameraSource(devicePosition, lensType)
                )
            }
        })

        secondaryFPVWidget.setSurfaceViewZOrderOnTop(true)
        secondaryFPVWidget.setSurfaceViewZOrderMediaOverlay(true)

        mapWidget.initAMap { map: DJIMap ->
            val uiSetting = map.uiSettings
            uiSetting?.setZoomControlsEnabled(false)
        }
        mapWidget.onCreate(savedInstanceState)

        initListener()
        setupObservers()
        activity?.let { binding.inferenceStream.setActivity(it) }

        liveStreamVM.getInferenceModels()
        inferenceModel = arguments?.getString(EXTRA_INFERENCE_MODEL) ?: "eco"
    }

    private fun toast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        inflateStreamer()

        mapWidget.onResume()
        compositeDisposable = CompositeDisposable()
        compositeDisposable?.add(systemStatusListPanelWidget.closeButtonPressed()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pressed: Boolean ->
                if (pressed) {
                    systemStatusListPanelWidget.hide()
                }
            })
        compositeDisposable?.add(simulatorControlWidget.getUIStateUpdates()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { simulatorControlWidgetState: SimulatorControlWidget.UIState? ->
                if (simulatorControlWidgetState is VisibilityUpdated) {
                    if ((simulatorControlWidgetState as VisibilityUpdated).isVisible) {
                        hideOtherPanels(simulatorControlWidget)
                    }
                }
            })
        compositeDisposable?.add(
            cameraSourceProcessor.toFlowable()
                .observeOn(io())
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .subscribeOn(io())
                .subscribe(Consumer<CameraSource> { result: CameraSource ->
                    activity?.runOnUiThread(
                        Runnable { onCameraSourceUpdated(result.devicePosition, result.lensType) })
                })
        )

        compositeDisposable?.add(ObservableInMemoryKeyedStore.getInstance()
            .addObserver(UXKeys.create(GlobalPreferenceKeys.GIMBAL_ADJUST_CLICKED))
            .observeOn(ui())
            .subscribe { broadcastValues: BroadcastValues? ->
                if (broadcastValues != null) {
                    this.isGimableAdjustClicked(
                        broadcastValues
                    )
                }
            })
        ViewUtil.setKeepScreen(requireActivity(), true)
    }

    @RequiresPermission(allOf = [Manifest.permission.CAMERA])
    private fun inflateStreamer() {
        lifecycle.addObserver(streamerLifeCycleObserver)
        configureStreamer()
    }

    private fun configureStreamer() {
        val videoConfig = VideoConfig(
            mimeType = MediaFormat.MIMETYPE_VIDEO_HEVC, resolution = Size(1280, 720), fps = 20
        )
        streamer.configure(videoConfig)
    }

    private fun isGimableAdjustClicked(broadcastValues: BroadcastValues) {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawers()
        }
        horizontalSituationIndicatorWidget.visibility = View.GONE
        /*if (gimbalFineTuneWidget != null) {
            gimbalFineTuneWidget.setVisibility(View.VISIBLE)
        }*/
    }

    override fun onPause() {
        if (compositeDisposable != null) {
            compositeDisposable?.dispose()
            compositeDisposable = null
        }
        mapWidget.onPause()
        super.onPause()
    }

    private fun initListener() {
        binding.fbStartStop.setOnClickListener {
            if (!isStreaming) {
                selectInferenceModelDialog()
            } else {
                stopSrtStreaming()
            }
        }

        /*binding.fbStartWaypoint.setOnClickListener {
            findNavController().navigate(
                R.id.action_navigation_livestreaming_to_navigation_waypoint
            )
        }*/

        binding.aiButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                playerStream()
            else
                stopPlayer()
        }

        secondaryFPVWidget.setOnClickListener { v: View? ->
            swapVideoSource()
        }

        initChannelStateListener()

        val systemStatusWidget = topBarPanel.systemStatusWidget
        systemStatusWidget?.setOnClickListener { v: View? -> systemStatusListPanelWidget.toggleVisibility() }

        val simulatorIndicatorWidget = topBarPanel.simulatorIndicatorWidget
        simulatorIndicatorWidget?.setOnClickListener { v: View? -> simulatorControlWidget.toggleVisibility() }

        widgetHeight = resources.getDimension(R.dimen.mini_map_height).toInt()
        widgetWidth = resources.getDimension(R.dimen.mini_map_width).toInt()

        binding.imgShowMap.setOnClickListener {
            mapWidget.show()
            binding.imgShowMap.hide()
        }

        binding.imgMapMinimize.setOnClickListener {
            mapWidget.hide()
            binding.imgShowMap.show()
        }

        binding.imgMapFullscreen.setOnClickListener {
            showMapFullScreen()
        }

        binding.widgetPrimaryFpv.setOnClickListener {
            showPrimaryFpvFullScreen()
        }

        binding.inferenceStream.setOnClickListener {
            showAiFullScreen()
        }

        binding.panelTopBar.settingWidget?.setOnClickListener {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.closeDrawers()
            } else {
                mDrawerLayout.openDrawer(GravityCompat.END)
            }
        }
    }

    private fun setupObservers() {
        liveStreamVM.error.observe(viewLifecycleOwner) {
            binding.contentLoading.isVisible = false
            ToastUtils.showToast(it)
        }

        liveStreamVM.device.observe(viewLifecycleOwner) {
            if (!isStreaming) {
                streamId = it.streamId
                thread(start = true) {
                    Thread.sleep(30000)
                    lifecycleScope.launch {
                        try {
                            binding.contentLoading.isVisible = false
                            isStreaming = true
                            streamer.startStream(it.urlTransmit)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            binding.contentLoading.isVisible = false
                            isStreaming = false
                            stopSrtStreaming()
                        }
                    }
                }
            }
        }

        liveStreamVM.disconnected.observe(viewLifecycleOwner) {
            toast(getString(R.string.disconnected))
            binding.contentLoading.isVisible = false
        }
    }

    //endregion
    private fun hideOtherPanels(widget: View?) {
        val panels = arrayOf<View>(
            simulatorControlWidget
        )
        for (panel in panels) {
            if (widget !== panel) {
                panel.visibility = View.GONE
            }
        }
    }

    private fun updateFPVWidgetSource(streamSources: List<StreamSource>?) {
        LogUtils.i(TAG, JsonUtil.toJson<StreamSource>(streamSources))
        if (streamSources == null) {
            return
        }

        if (streamSources.isEmpty()) {
            secondaryFPVWidget.visibility = View.GONE
            return
        }

        if (streamSources.size == 1) {
            secondaryFPVWidget.visibility = View.GONE
            return
        }
        secondaryFPVWidget.visibility = View.VISIBLE
    }

    private fun initChannelStateListener() {
        val primaryChannel =
            MediaDataCenter.getInstance().videoStreamManager.getAvailableVideoChannel(
                VideoChannelType.PRIMARY_STREAM_CHANNEL
            )

        val secondaryChannel =
            MediaDataCenter.getInstance().videoStreamManager.getAvailableVideoChannel(
                VideoChannelType.SECONDARY_STREAM_CHANNEL
            )
        if (primaryChannel != null) {
            primaryChannelStateListener =
                VideoChannelStateChangeListener { _: VideoChannelState?, to: VideoChannelState ->
                    val primaryStreamSource =
                        primaryChannel.streamSource
                    if (VideoChannelState.ON == to && primaryStreamSource != null) {
                        activity?.runOnUiThread {
                            primaryFpvWidget.updateVideoSource(
                                primaryStreamSource,
                                VideoChannelType.PRIMARY_STREAM_CHANNEL
                            )
                        }
                    }
                }
            primaryChannel.addVideoChannelStateChangeListener(primaryChannelStateListener)
        }
        if (secondaryChannel != null) {
            secondaryChannelStateListener =
                VideoChannelStateChangeListener { _: VideoChannelState?, to: VideoChannelState ->
                    val secondaryStreamSource =
                        secondaryChannel.streamSource
                    if (VideoChannelState.ON == to && secondaryStreamSource != null) {
                        activity?.runOnUiThread {
                            secondaryFPVWidget.updateVideoSource(
                                secondaryStreamSource,
                                VideoChannelType.SECONDARY_STREAM_CHANNEL
                            )
                        }
                    }
                }
            secondaryChannel.addVideoChannelStateChangeListener(secondaryChannelStateListener)
        }
    }

    private fun removeChannelStateListener() {
        val primaryChannel =
            MediaDataCenter.getInstance().videoStreamManager.getAvailableVideoChannel(
                VideoChannelType.PRIMARY_STREAM_CHANNEL
            )

        val secondaryChannel =
            MediaDataCenter.getInstance().videoStreamManager.getAvailableVideoChannel(
                VideoChannelType.SECONDARY_STREAM_CHANNEL
            )
        primaryChannel?.removeVideoChannelStateChangeListener(primaryChannelStateListener)
        secondaryChannel?.removeVideoChannelStateChangeListener(secondaryChannelStateListener)
    }

    private fun onCameraSourceUpdated(
        devicePosition: PhysicalDevicePosition,
        lensType: CameraLensType
    ) {
        LogUtils.i(TAG, devicePosition, lensType)
        val cameraIndex = CameraUtil.getCameraIndex(devicePosition)
        updateViewVisibility(devicePosition, lensType)
        updateInteractionEnabled()
        streamer.switchCamera(cameraIndex)
        //如果无需使能或者显示的，也就没有必要切换了。
        if (fpvInteractionWidget.isInteractionEnabled) {
            fpvInteractionWidget.updateCameraSource(cameraIndex, lensType)
            fpvInteractionWidget.updateGimbalIndex(CommonUtils.getGimbalIndex(devicePosition))
        }
        if (lensControlWidget.visibility == View.VISIBLE) {
            lensControlWidget.updateCameraSource(cameraIndex, lensType)
        }
        if (ndviCameraPanel.visibility == View.VISIBLE) {
            ndviCameraPanel.updateCameraSource(cameraIndex, lensType)
        }
        if (visualCameraPanel.visibility == View.VISIBLE) {
            visualCameraPanel.updateCameraSource(cameraIndex, lensType)
        }
        if (autoExposureLockWidget.visibility == View.VISIBLE) {
            autoExposureLockWidget.updateCameraSource(cameraIndex, lensType)
        }
        if (focusModeWidget.visibility == View.VISIBLE) {
            focusModeWidget.updateCameraSource(cameraIndex, lensType)
        }
        if (focusExposureSwitchWidget.visibility == View.VISIBLE) {
            focusExposureSwitchWidget.updateCameraSource(cameraIndex, lensType)
        }
        if (cameraControlsWidget.visibility == View.VISIBLE) {
            cameraControlsWidget.updateCameraSource(cameraIndex, lensType)
        }
        if (exposureSettingsPanel.visibility == View.VISIBLE) {
            exposureSettingsPanel.updateCameraSource(cameraIndex, lensType)
        }
        if (focalZoomWidget.visibility == View.VISIBLE) {
            focalZoomWidget.updateCameraSource(cameraIndex, lensType)
        }
        if (horizontalSituationIndicatorWidget.visibility == View.VISIBLE) {
            horizontalSituationIndicatorWidget.updateCameraSource(cameraIndex, lensType)
        }
    }

    private fun updateViewVisibility(
        devicePosition: PhysicalDevicePosition,
        lensType: CameraLensType
    ) {
        //只在fpv下显示
        pfvFlightDisplayWidget.visibility =
            if (devicePosition == PhysicalDevicePosition.NOSE) View.VISIBLE else View.INVISIBLE

        //fpv下不显示
        lensControlWidget.visibility =
            if (devicePosition == PhysicalDevicePosition.NOSE) View.INVISIBLE else View.VISIBLE
        ndviCameraPanel.visibility =
            if (devicePosition == PhysicalDevicePosition.NOSE) View.INVISIBLE else View.VISIBLE
        visualCameraPanel.visibility =
            if (devicePosition == PhysicalDevicePosition.NOSE) View.INVISIBLE else View.VISIBLE
        autoExposureLockWidget.visibility =
            if (devicePosition == PhysicalDevicePosition.NOSE) View.INVISIBLE else View.VISIBLE
        focusModeWidget.visibility =
            if (devicePosition == PhysicalDevicePosition.NOSE) View.INVISIBLE else View.VISIBLE
        focusExposureSwitchWidget.visibility =
            if (devicePosition == PhysicalDevicePosition.NOSE) View.INVISIBLE else View.VISIBLE
        /*cameraControlsWidget.visibility =
            if (devicePosition == PhysicalDevicePosition.NOSE) View.INVISIBLE else View.VISIBLE*/
        focalZoomWidget.visibility =
            if (devicePosition == PhysicalDevicePosition.NOSE) View.INVISIBLE else View.VISIBLE
        horizontalSituationIndicatorWidget.setSimpleModeEnable(devicePosition != PhysicalDevicePosition.NOSE)

        //有其他的显示逻辑，这里确保fpv下不显示
        if (devicePosition == PhysicalDevicePosition.NOSE) {
            exposureSettingsPanel.visibility = View.INVISIBLE
        }

        //只在部分len下显示
        ndviCameraPanel.visibility =
            if (CameraUtil.isSupportForNDVI(lensType)) View.VISIBLE else View.INVISIBLE
    }

    private fun swapVideoSource() {
        val primaryVideoChannel = primaryFpvWidget.videoChannelType
        val primaryStreamSource = primaryFpvWidget.getStreamSource()
        val secondaryVideoChannel = secondaryFPVWidget.videoChannelType
        val secondaryStreamSource = secondaryFPVWidget.getStreamSource()
        //两个source都存在的情况下才进行切换
        if (secondaryStreamSource != null && primaryStreamSource != null) {
            primaryFpvWidget.updateVideoSource(secondaryStreamSource, secondaryVideoChannel)
            secondaryFPVWidget.updateVideoSource(primaryStreamSource, primaryVideoChannel)

            //if (restartStreaming) startStreamFrameByFrame()
        }
    }

    private fun updateInteractionEnabled() {
        val newPrimaryStreamSource = primaryFpvWidget.getStreamSource()
        fpvInteractionWidget.isInteractionEnabled = false
        if (newPrimaryStreamSource != null) {
            fpvInteractionWidget.isInteractionEnabled =
                newPrimaryStreamSource.physicalDevicePosition != PhysicalDevicePosition.NOSE
        }
    }

    private fun selectInferenceModelDialog() {
        val selectedModel = liveStreamVM.inferenceModels.value?.toTypedArray()?.indexOfLast {
            it == inferenceModel
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_inference_model))
            .setSingleChoiceItems(
                liveStreamVM.inferenceModels.value?.toTypedArray(),
                selectedModel ?: -1
            ) { dialog, which ->
                inferenceModel =
                    liveStreamVM.inferenceModels.value?.toTypedArray()?.get(which) ?: inferenceModel
                startSrtStream()
                dialog.dismiss()
            }
            .setNegativeButton(getString(android.R.string.cancel)) { dialog, _ ->
                startSrtStream()
                dialog.dismiss()
            }
            .show()
    }

    private fun startSrtStream() {
        binding.contentLoading.isVisible = true

        lifecycleScope.launch {
            val location = liveStreamVM.getAircraftLocation()
            idDevice?.let {
                liveStreamVM.startStream(
                    StartStream(
                        it,
                        location?.latitude ?: 0.0,
                        location?.longitude ?: 0.0,
                        inferenceModel,
                        analyticsConfig
                    )
                )
            }
        }
    }

    private fun stopSrtStreaming() {
        idDevice?.let { liveStreamVM.disconnectDevice(it) }
        streamer.stopStream()
        streamer.disconnect()
        isStreaming = false
        binding.fbStartStop.setImageResource(R.drawable.ic_play)
        binding.aiButton.isVisible = false
        stopPlayer()
    }

    private fun playerStream() {
        binding.inferenceStream.isVisible = true
        binding.inferenceStream.startStreaming(streamId)
        binding.inferenceStream.setInferenceModelText(inferenceModel)
    }

    private fun stopPlayer() {
        binding.inferenceStream.isVisible = false
        binding.inferenceStream.stopStreaming(streamId)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        curWidth = surfaceView.width
        curHeight = surfaceView.height
        streamer.startPreview(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        curWidth = width
        curHeight = height
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        videoDecoder?.onPause()
        streamer.stopPreview()
    }

    private fun showPrimaryFpvFullScreen() {
        fullViewType = ViewType.FPV

        binding.inferenceStream.isClickable = true
        primaryFpvWidget.elevation = 0f

        binding.widgetFpvInteraction.isInteractionEnabled = true

        binding.inferenceStream.elevation = 50f

        animateView(mapWidget, mapWidget.width, widgetWidth, mapWidget.height, widgetHeight)
        animateView(
            primaryFpvWidget,
            primaryFpvWidget.width,
            binding.fpvHolder.width,
            primaryFpvWidget.height,
            binding.fpvHolder.height
        )

        animateView(
            binding.inferenceStream,
            binding.inferenceStream.width,
            widgetWidth,
            binding.inferenceStream.height,
            widgetHeight
        )

        binding.imgMapMinimize.show()
        binding.imgMapFullscreen.show()
        if (isStreaming)
            binding.aiButton.show()
    }

    private fun showMapFullScreen() {
        binding.widgetFpvInteraction.isInteractionEnabled = false

        if (fullViewType == ViewType.FPV) {
            binding.inferenceStream.isClickable = false
            primaryFpvWidget.elevation = 100f
            binding.inferenceStream.elevation = 50f
        } else {
            binding.inferenceStream.isClickable = true
            primaryFpvWidget.elevation = 50f
            binding.inferenceStream.elevation = 100f
        }

        fullViewType = ViewType.MAP

        animateView(
            mapWidget,
            mapWidget.width,
            binding.contentMap.width,
            mapWidget.height,
            binding.contentMap.height
        )
        animateView(
            primaryFpvWidget,
            primaryFpvWidget.width,
            widgetWidth,
            primaryFpvWidget.height,
            widgetHeight
        )

        animateView(
            binding.inferenceStream,
            binding.inferenceStream.width,
            widgetWidth,
            binding.inferenceStream.height,
            widgetHeight
        )

        binding.imgMapMinimize.hide()
        binding.imgMapFullscreen.hide()
    }

    private fun showAiFullScreen() {
        fullViewType = ViewType.AI
        binding.inferenceStream.isClickable = false
        primaryFpvWidget.elevation = 50f
        binding.widgetFpvInteraction.isInteractionEnabled = false

        binding.inferenceStream.elevation = 0f

        animateView(mapWidget, mapWidget.width, widgetWidth, mapWidget.height, widgetHeight)

        animateView(
            binding.inferenceStream,
            binding.inferenceStream.width,
            binding.fpvHolder.width,
            binding.inferenceStream.height,
            binding.fpvHolder.height
        )

        animateView(
            primaryFpvWidget,
            primaryFpvWidget.width,
            widgetWidth,
            primaryFpvWidget.height,
            widgetHeight
        )

        binding.imgMapMinimize.show()
        binding.imgMapFullscreen.show()
    }

    private fun animateView(
        view: View,
        startWidth: Int,
        endWidth: Int,
        startHeight: Int,
        endHeight: Int
    ) {
        val widthAnimator = ValueAnimator.ofInt(startWidth, endWidth)
        val heightAnimator = ValueAnimator.ofInt(startHeight, endHeight)

        widthAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.width = value
            view.layoutParams = layoutParams
        }

        heightAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.height = value
            view.layoutParams = layoutParams
        }

        widthAnimator.duration = 300
        heightAnimator.duration = 300

        widthAnimator.start()
        heightAnimator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        if (isStreaming) {
            stopSrtStreaming()
        }

        if (videoDecoder != null) {
            videoDecoder?.destroy()
            videoDecoder = null
        }

        mapWidget.onDestroy()
        MediaDataCenter.getInstance().videoStreamManager.clearAllStreamSourcesListeners()
        removeChannelStateListener()
    }

    private class CameraSource(
        var devicePosition: PhysicalDevicePosition,
        var lensType: CameraLensType
    )

    companion object {
        const val EXTRA_ID_DEVICE = "EXTRA_ID_DEVICE"
        const val EXTRA_INFERENCE_MODEL = "EXTRA_INFERENCE_MODEL"
        const val EXTRA_ANALYTICS_CONFIG = "EXTRA_ANALYTICS_CONFIG"
        const val WEBRTC_HOST =
            "wss://ec2-3-88-125-209.compute-1.amazonaws.com:5443/WebRTCAppEE/websocket"
    }
}
