package dji.sampleV5.aircraft.comom.components

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.databinding.ViewInferenceStreamBinding
import io.antmedia.webrtcandroidframework.api.DefaultWebRTCListener
import io.antmedia.webrtcandroidframework.api.IWebRTCClient
import io.antmedia.webrtcandroidframework.api.IWebRTCListener
import kotlinx.coroutines.launch

class InferenceStreamView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleOwner {

    private val binding: ViewInferenceStreamBinding

    private val lifecycleRegistry: LifecycleRegistry by lazy {
        LifecycleRegistry(this)
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onDetachedFromWindow() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        super.onDetachedFromWindow()
    }

    private lateinit var activity: Activity
    private lateinit var webRTCClient: IWebRTCClient

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.InferenceStreamView, 0, defStyleAttr)
            .apply {
                try {
                    binding = ViewInferenceStreamBinding.inflate(
                        LayoutInflater.from(context),
                        this@InferenceStreamView,
                        true
                    )
                } finally {
                    recycle()
                }
            }
    }

    fun setActivity(activity: Activity) {
        this.activity = activity
        setupStreaming()
    }

    private fun setupStreaming() {
        webRTCClient = IWebRTCClient.builder()
            .setActivity(activity)
            .setAudioCallEnabled(false)
            .setVideoCallEnabled(false)
            .addRemoteVideoRenderer(binding.playerView)
            .setWebRTCListener(createWebRTCListener())
            .setServerUrl(WEBRTC_HOST)
            .setReconnectionEnabled(true)
            .build()
    }

    fun setInferenceModelText(model: String) {
        binding.txtModel.text = model
    }

    private fun createWebRTCListener(): IWebRTCListener {
        return object : DefaultWebRTCListener() {
            override fun onPlayStarted(streamId: String) {
                super.onPlayStarted(streamId)
                try {
                    binding.loadingAi.isVisible = false
                    binding.txtMessage.isVisible = false
                } catch (e: Exception) {
                    e.stackTrace
                }
            }

            override fun onPlayFinished(streamId: String) {
                super.onPlayFinished(streamId)
            }

            override fun onError(description: String?, streamId: String?) {
                super.onError(description, streamId)
                try {
                    description?.let { handleError(it) }
                } catch (e: Exception) {
                    e.stackTrace
                }
            }
        }
    }

    private fun handleError(error: String) {
        binding.loadingAi.isVisible = false
        binding.txtMessage.isVisible = true
        if (error == "no_stream_exist")
            binding.txtMessage.text = activity.getString(R.string.no_stream_to_play)
        else
            binding.txtMessage.text = error
    }

    fun startStreaming(streamId: String) {
        binding.loadingAi.isVisible = true
        lifecycleScope.launch {
            webRTCClient.play(streamId)
        }
    }

    fun stopStreaming(streamId: String) {
        lifecycleScope.launch {
            webRTCClient.stop(streamId)
        }
    }

    companion object {
        const val WEBRTC_HOST =
            "wss://ec2-3-88-125-209.compute-1.amazonaws.com:5443/WebRTCAppEE/websocket"
    }
}