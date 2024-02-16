package dji.sampleV5.aircraft.srt.streamers

import android.content.Context
import dji.sampleV5.aircraft.srt.BaseSurfaceLiveStreamer
import io.github.thibaultbee.streampack.data.BitrateRegulatorConfig
import dji.sampleV5.aircraft.srt.internal.endpoints.SrtProducer
import dji.sampleV5.aircraft.srt.regulator.srt.SrtBitrateRegulator
import dji.sampleV5.aircraft.srt.streamers.interfaces.ISrtLiveStreamer
import dji.v5.manager.interfaces.ICameraStreamManager
import io.github.thibaultbee.streampack.internal.muxers.ts.TSMuxer
import io.github.thibaultbee.streampack.internal.muxers.ts.data.TsServiceInfo
import io.github.thibaultbee.streampack.internal.utils.Scheduler
import io.github.thibaultbee.streampack.internal.utils.defaultTsServiceInfo
import io.github.thibaultbee.streampack.listeners.OnConnectionListener
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import io.github.thibaultbee.streampack.regulator.IBitrateRegulatorFactory

class SurfaceSrtLiveStreamer(
    context: Context,
    tsServiceInfo: TsServiceInfo = context.defaultTsServiceInfo,
    bitrateRegulatorFactory: IBitrateRegulatorFactory? = null,
    bitrateRegulatorConfig: BitrateRegulatorConfig? = null,
    cameraStreamManager: ICameraStreamManager,
    initialOnErrorListener: OnErrorListener? = null,
    initialOnConnectionListener: OnConnectionListener? = null
) : BaseSurfaceLiveStreamer(
    context = context,
    muxer = TSMuxer().apply { addService(tsServiceInfo) },
    endpoint = SrtProducer(),
    initialOnErrorListener = initialOnErrorListener,
    cameraStreamManager = cameraStreamManager,
    initialOnConnectionListener = initialOnConnectionListener
), ISrtLiveStreamer {

    /**
     * Bitrate regulator. Calls regularly by [scheduler]. Don't call it otherwise or you might break regulation.
     */
    private val bitrateRegulator = bitrateRegulatorConfig?.let { config ->
        bitrateRegulatorFactory?.newBitrateRegulator(
            config,
            { settings.video.bitrate = it },
            { settings.audio.bitrate = it }
        ) as SrtBitrateRegulator
    }

    /**
     * Scheduler for bitrate regulation
     */
    private val scheduler = Scheduler(500) {
        bitrateRegulator?.update(srtProducer.stats, settings.video.bitrate, settings.audio.bitrate)
            ?: throw UnsupportedOperationException("Scheduler runs but no bitrate regulator set")
    }

    private val srtProducer = endpoint as SrtProducer

    /**
     * Get/set SRT stream ID.
     * **See:** [SRT Socket Options](https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md#srto_streamid)
     */
    override var streamId: String
        /**
         * Get SRT stream ID
         * @return stream ID
         */
        get() = srtProducer.streamId
        /**
         * @param value stream ID
         */
        set(value) {
            srtProducer.streamId = value
        }

    /**
     * Get/set SRT passphrase.
     * **See:** [SRT Socket Options](https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md#srto_passphrase)
     */
    override var passPhrase: String
        /**
         * Get SRT passphrase
         * @return passphrase
         */
        get() = srtProducer.passPhrase
        /**
         * @param value passphrase
         */
        set(value) {
            srtProducer.passPhrase = value
        }

    override var latency: Int
        /**
         * Get latency in milliseconds
         * @return latency
         */
        get() = srtProducer.latency
        /**
         * @param value latency in milliseconds
         */
        set(value) {
            srtProducer.latency = value
        }

    /**
     * Connect to an SRT server with correct Live streaming parameters.
     * To avoid creating an unresponsive UI, do not call on main thread.
     *
     * @param ip server ip
     * @param port server port
     * @throws Exception if connection has failed or configuration has failed
     */
    override suspend fun connect(ip: String, port: Int) {
        srtProducer.connect(ip, port)
    }

    /**
     * Same as [BaseCameraLiveStreamer.startStream] but also starts bitrate regulator.
     */
    override fun startStream() {
        if (bitrateRegulator != null) {
            scheduler.start()
        }
        super.startStream()
    }

    /**
     * Same as [BaseCameraLiveStreamer.startStream] but also starts bitrate regulator.
     */
    override suspend fun startStream(url: String) {
        if (bitrateRegulator != null) {
            scheduler.start()
        }
        super.startStream(url)
    }

    /**
     * Connect to an SRT server and start stream.
     * Same as calling [connect], then [startStream].
     * To avoid creating an unresponsive UI, do not call on main thread.
     *
     * @param ip server ip
     * @param port server port
     * @throws Exception if connection has failed or configuration has failed or [startStream] has failed too.
     */
    override suspend fun startStream(ip: String, port: Int) {
        connect(ip, port)
        startStream()
    }

    /**
     * Same as [BaseCameraLiveStreamer.stopStream] but also stops bitrate regulator.
     */
    override fun stopStream() {
        scheduler.cancel()
        super.stopStream()
    }
}