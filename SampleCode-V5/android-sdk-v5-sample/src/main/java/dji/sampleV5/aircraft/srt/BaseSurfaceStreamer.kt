package dji.sampleV5.aircraft.srt

import android.content.Context
import android.view.Surface
import dji.v5.manager.interfaces.ICameraStreamManager
import io.github.thibaultbee.streampack.error.StreamPackError
import io.github.thibaultbee.streampack.internal.endpoints.IEndpoint
import io.github.thibaultbee.streampack.internal.muxers.IMuxer
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import io.github.thibaultbee.streampack.streamers.bases.BaseStreamer
import kotlinx.coroutines.runBlocking

open class BaseSurfaceStreamer(
    private val context: Context,
    muxer: IMuxer,
    endpoint: IEndpoint,
    cameraStreamManager: ICameraStreamManager,
    initialOnErrorListener: OnErrorListener? = null
) : BaseStreamer(
    context = context,
    videoCapture = SurfaceCapture(cameraStreamManager),
    audioCapture = null,
    manageVideoOrientation = true,
    muxer = muxer,
    endpoint = endpoint,
    initialOnErrorListener = initialOnErrorListener
), ISurfaceStreamer {
    private val cameraCapture = videoCapture as SurfaceCapture

    override fun startPreview(previewSurface: Surface) {
        require(videoConfig != null) { "Video has not been configured!" }
        runBlocking {
            try {
                cameraCapture.previewSurface = previewSurface
                cameraCapture.encoderSurface = videoEncoder?.inputSurface
                cameraCapture.startPreview()
            } catch (e: Exception) {
                stopPreview()
                throw StreamPackError(e)
            }
        }
    }

    /**
     * Stops capture.
     * It also stops stream if the stream is running.
     *
     * @see [startPreview]
     */
    override fun stopPreview() {
        stopStream()
        cameraCapture.stopPreview()
    }

    /**
     * Same as [BaseStreamer.release] but it also calls [stopPreview].
     */
    override fun release() {
        stopPreview()
        super.release()
    }
}