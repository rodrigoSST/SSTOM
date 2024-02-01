package dji.sampleV5.aircraft.srt

import android.content.Context
import android.view.Surface
import io.github.thibaultbee.streampack.data.VideoConfig
import io.github.thibaultbee.streampack.internal.data.Frame
import io.github.thibaultbee.streampack.internal.sources.IVideoCapture
import java.nio.ByteBuffer

class SurfaceCapture(
    private val context: Context,
) : IVideoCapture {
    var previewSurface: Surface? = null
    override var encoderSurface: Surface? = null

    override val timestampOffset = 0L
    override val hasSurface = true
    override fun getFrame(buffer: ByteBuffer): Frame {
        throw UnsupportedOperationException("Camera expects to run in Surface mode")
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
        isStreaming = true
    }

    override fun stopStream() {
        if (isStreaming) {
            checkStream()

            isStreaming = false
        }
    }

    override fun release() {
        //cameraController.release()
    }
}