package dji.sampleV5.aircraft.srt

import android.media.MediaFormat
import android.os.SystemClock
import android.view.Surface
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.manager.interfaces.ICameraStreamManager
import io.github.thibaultbee.streampack.data.VideoConfig
import io.github.thibaultbee.streampack.internal.data.Frame
import io.github.thibaultbee.streampack.internal.sources.IVideoCapture
import java.nio.ByteBuffer

class SurfaceCapture(
    private val cameraStreamManager: ICameraStreamManager
) : IVideoCapture {
    var previewSurface: Surface? = null
    override var encoderSurface: Surface? = null

    override val timestampOffset = 0L
    override val hasSurface = true

    lateinit var byteBuffer: ByteBuffer

    override fun getFrame(buffer: ByteBuffer): Frame {
        return Frame(byteBuffer, MediaFormat.MIMETYPE_VIDEO_HEVC, System.currentTimeMillis())
    }

    private var fps: Int = 30
    private var isStreaming = false
    private var isPreviewing = false

    override fun configure(config: VideoConfig) {
        this.fps = config.fps
    }

    suspend fun startPreview(restartStream: Boolean = false) {
        var targets = mutableListOf<Surface>()
        previewSurface?.let { targets.add(it) }
        encoderSurface?.let { targets.add(it) }

        targets = mutableListOf()
        previewSurface?.let { targets.add(it) }
        if (restartStream) {
            encoderSurface?.let { targets.add(it) }
        }

        isPreviewing = true
    }

    fun stopPreview() {
        isPreviewing = false
    }

    private fun checkStream() =
        require(encoderSurface != null) { "encoder surface must not be null" }

    override fun startStream() {
        checkStream()

        cameraStreamManager.putCameraStreamSurface(
            ComponentIndexType.LEFT_OR_MAIN,
            encoderSurface!!,
            1280,
            720,
            ICameraStreamManager.ScaleType.CENTER_INSIDE
        )

        isStreaming = true
    }

    override fun stopStream() {
        if (isStreaming) {
            checkStream()

            cameraStreamManager.removeCameraStreamSurface(encoderSurface!!)

            isStreaming = false
        }
    }

    override fun release() {
        //cameraController.release()
    }

    private fun getOffsetFromRealtimeTimestampSource(): Long {
        // Measure the offset of the REALTIME clock w.r.t. the MONOTONIC clock. Do
        // CLOCK_OFFSET_CALIBRATION_ATTEMPTS measurements and choose the offset computed with the
        // smallest delay between measurements. When the camera returns a timestamp ts, the
        // timestamp in MONOTONIC timebase will now be (ts + cameraTimeOffsetToMonoClock).
        var offset = Long.MAX_VALUE
        var lowestGap = Long.MAX_VALUE
        for (i in 0 until 3) {
            val startMonoTs = System.nanoTime()
            val realTs = SystemClock.elapsedRealtimeNanos()
            val endMonoTs = System.nanoTime()
            val gapMonoTs = endMonoTs - startMonoTs
            if (gapMonoTs < lowestGap) {
                lowestGap = gapMonoTs
                offset = (startMonoTs + endMonoTs) / 2 - realTs
            }
        }
        return offset / 1000
    }
}